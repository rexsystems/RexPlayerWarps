package me.rexsystems.rexPlayerWarps.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.rexsystems.rexPlayerWarps.RexPlayerWarps;
import me.rexsystems.rexPlayerWarps.config.PluginConfig;
import me.rexsystems.rexPlayerWarps.model.WarpAccess;
import me.rexsystems.rexPlayerWarps.model.WarpRecord;
import me.rexsystems.rexPlayerWarps.model.WhitelistEntry;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class SqlWarpStorage {

    private final RexPlayerWarps plugin;
    private final Object writeLock = new Object();
    private HikariDataSource dataSource;
    private boolean mysql;

    public SqlWarpStorage(RexPlayerWarps plugin) {
        this.plugin = plugin;
    }

    public void open(PluginConfig settings) throws SQLException {
        close();
        mysql = settings.mysql();
        HikariConfig pool = new HikariConfig();
        pool.setPoolName("RexPlayerWarps");
        if (mysql) {
            loadDriver("com.mysql.cj.jdbc.Driver");
            pool.setDriverClassName("com.mysql.cj.jdbc.Driver");
            pool.setJdbcUrl("jdbc:mysql://" + settings.mysqlHost() + ":" + settings.mysqlPort() + "/"
                    + settings.mysqlDatabase() + "?useUnicode=true&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true");
            pool.setUsername(settings.mysqlUsername());
            pool.setPassword(settings.mysqlPassword());
            pool.setMaximumPoolSize(settings.mysqlPoolSize());
        } else {
            loadDriver("org.sqlite.JDBC");
            File file = settings.sqliteFile(plugin.getDataFolder());
            File parent = file.getParentFile();
            if (parent != null && !parent.exists() && !parent.mkdirs()) {
                throw new SQLException("Could not create " + parent.getAbsolutePath());
            }
            pool.setDriverClassName("org.sqlite.JDBC");
            pool.setJdbcUrl("jdbc:sqlite:" + file.getAbsolutePath());
            pool.setMaximumPoolSize(1);
        }
        pool.setMinimumIdle(1);
        dataSource = new HikariDataSource(pool);
        if (!mysql) {
            try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
                statement.execute("PRAGMA journal_mode=WAL");
                statement.execute("PRAGMA foreign_keys=ON");
            }
        }
        createTables();
    }

    public void close() {
        HikariDataSource pool = dataSource;
        dataSource = null;
        if (pool != null && !pool.isClosed()) {
            pool.close();
        }
    }

    public boolean open() {
        return dataSource != null && !dataSource.isClosed();
    }

    public long insert(WarpRecord record, int limit) throws SQLException, DuplicateWarpException, WarpLimitException {
        synchronized (writeLock) {
            try (Connection connection = connection()) {
                connection.setAutoCommit(false);
                try {
                    int owned = countOwned(connection, record.ownerId());
                    if (owned >= limit) {
                        connection.rollback();
                        throw new WarpLimitException();
                    }
                    String sql = mysql
                            ? "INSERT INTO warps (name, name_key, owner_uuid, owner_name, world, x, y, z, yaw, pitch, description, icon, category, price_cents, access, balance_cents, visits, created_at) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"
                            : "INSERT INTO warps (name, name_key, owner_uuid, owner_name, world, x, y, z, yaw, pitch, description, icon, category, price_cents, access, balance_cents, visits, created_at) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                    try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                        bindWarp(statement, record);
                        statement.executeUpdate();
                        try (ResultSet keys = statement.getGeneratedKeys()) {
                            if (!keys.next()) {
                                connection.rollback();
                                throw new SQLException("Insert did not return an id");
                            }
                            long id = keys.getLong(1);
                            connection.commit();
                            return id;
                        }
                    }
                } catch (SQLException failure) {
                    connection.rollback();
                    if (duplicate(failure)) {
                        throw new DuplicateWarpException(record.name());
                    }
                    throw failure;
                }
            }
        }
    }

    public void delete(long id) throws SQLException {
        synchronized (writeLock) {
            try (Connection connection = connection()) {
                connection.setAutoCommit(false);
                try {
                    deleteChildren(connection, id);
                    try (PreparedStatement statement = connection.prepareStatement("DELETE FROM warps WHERE id = ?")) {
                        statement.setLong(1, id);
                        statement.executeUpdate();
                    }
                    connection.commit();
                } catch (SQLException failure) {
                    connection.rollback();
                    throw failure;
                }
            }
        }
    }

    public void updateFields(WarpRecord record) throws SQLException {
        synchronized (writeLock) {
            String sql = "UPDATE warps SET description = ?, icon = ?, category = ?, price_cents = ?, access = ?, owner_name = ? WHERE id = ?";
            try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, record.description());
                statement.setString(2, record.icon());
                statement.setString(3, record.category());
                statement.setLong(4, record.priceCents());
                statement.setString(5, record.access().name());
                statement.setString(6, record.ownerName());
                statement.setLong(7, record.id());
                statement.executeUpdate();
            }
        }
    }

    public void rate(long warpId, UUID playerId, int score) throws SQLException {
        synchronized (writeLock) {
            String sql = mysql
                    ? "INSERT INTO ratings (warp_id, player_uuid, score) VALUES (?,?,?) ON DUPLICATE KEY UPDATE score = VALUES(score)"
                    : "INSERT INTO ratings (warp_id, player_uuid, score) VALUES (?,?,?) ON CONFLICT(warp_id, player_uuid) DO UPDATE SET score = excluded.score";
            try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, warpId);
                statement.setString(2, playerId.toString());
                statement.setInt(3, score);
                statement.executeUpdate();
            }
        }
    }

    public boolean setFavorite(long warpId, UUID playerId, boolean favorite) throws SQLException {
        synchronized (writeLock) {
            try (Connection connection = connection()) {
                if (favorite) {
                    String sql = mysql
                            ? "INSERT IGNORE INTO favorites (warp_id, player_uuid) VALUES (?,?)"
                            : "INSERT OR IGNORE INTO favorites (warp_id, player_uuid) VALUES (?,?)";
                    try (PreparedStatement statement = connection.prepareStatement(sql)) {
                        statement.setLong(1, warpId);
                        statement.setString(2, playerId.toString());
                        statement.executeUpdate();
                    }
                } else {
                    try (PreparedStatement statement = connection.prepareStatement("DELETE FROM favorites WHERE warp_id = ? AND player_uuid = ?")) {
                        statement.setLong(1, warpId);
                        statement.setString(2, playerId.toString());
                        statement.executeUpdate();
                    }
                }
                return favorite;
            }
        }
    }

    public boolean favoriteExists(long warpId, UUID playerId) throws SQLException {
        synchronized (writeLock) {
            try (Connection connection = connection();
                 PreparedStatement statement = connection.prepareStatement("SELECT 1 FROM favorites WHERE warp_id = ? AND player_uuid = ?")) {
                statement.setLong(1, warpId);
                statement.setString(2, playerId.toString());
                try (ResultSet rows = statement.executeQuery()) {
                    return rows.next();
                }
            }
        }
    }

    public void addWhitelist(long warpId, WhitelistEntry entry) throws SQLException {
        synchronized (writeLock) {
            String sql = mysql
                    ? "INSERT INTO whitelist (warp_id, name_key, player_uuid, player_name) VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE player_uuid = VALUES(player_uuid), player_name = VALUES(player_name)"
                    : "INSERT INTO whitelist (warp_id, name_key, player_uuid, player_name) VALUES (?,?,?,?) ON CONFLICT(warp_id, name_key) DO UPDATE SET player_uuid = excluded.player_uuid, player_name = excluded.player_name";
            try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, warpId);
                statement.setString(2, entry.nameKey());
                statement.setString(3, entry.playerId() == null ? "" : entry.playerId().toString());
                statement.setString(4, entry.name());
                statement.executeUpdate();
            }
        }
    }

    public boolean removeWhitelist(long warpId, String nameKey) throws SQLException {
        synchronized (writeLock) {
            try (Connection connection = connection();
                 PreparedStatement statement = connection.prepareStatement("DELETE FROM whitelist WHERE warp_id = ? AND name_key = ?")) {
                statement.setLong(1, warpId);
                statement.setString(2, nameKey);
                return statement.executeUpdate() > 0;
            }
        }
    }

    public long claimBalance(long warpId) throws SQLException {
        synchronized (writeLock) {
            try (Connection connection = connection()) {
                connection.setAutoCommit(false);
                try {
                    long balance;
                    try (PreparedStatement statement = connection.prepareStatement("SELECT balance_cents FROM warps WHERE id = ?")) {
                        statement.setLong(1, warpId);
                        try (ResultSet rows = statement.executeQuery()) {
                            if (!rows.next()) {
                                connection.rollback();
                                return 0L;
                            }
                            balance = rows.getLong(1);
                        }
                    }
                    if (balance <= 0L) {
                        connection.rollback();
                        return 0L;
                    }
                    try (PreparedStatement statement = connection.prepareStatement("UPDATE warps SET balance_cents = 0 WHERE id = ? AND balance_cents = ?")) {
                        statement.setLong(1, warpId);
                        statement.setLong(2, balance);
                        if (statement.executeUpdate() != 1) {
                            connection.rollback();
                            return 0L;
                        }
                    }
                    connection.commit();
                    return balance;
                } catch (SQLException failure) {
                    connection.rollback();
                    throw failure;
                }
            }
        }
    }

    public void addBalance(long warpId, long cents) throws SQLException {
        synchronized (writeLock) {
            try (Connection connection = connection();
                 PreparedStatement statement = connection.prepareStatement("UPDATE warps SET balance_cents = balance_cents + ? WHERE id = ?")) {
                statement.setLong(1, cents);
                statement.setLong(2, warpId);
                statement.executeUpdate();
            }
        }
    }

    public void creditVisit(long warpId, UUID playerId, long priceCents, long visitedAt) throws SQLException {
        synchronized (writeLock) {
            try (Connection connection = connection()) {
                connection.setAutoCommit(false);
                try {
                    try (PreparedStatement statement = connection.prepareStatement(
                            "UPDATE warps SET visits = visits + 1, balance_cents = balance_cents + ? WHERE id = ?")) {
                        statement.setLong(1, priceCents);
                        statement.setLong(2, warpId);
                        statement.executeUpdate();
                    }
                    String recent = mysql
                            ? "INSERT INTO recent (player_uuid, warp_id, visited_at) VALUES (?,?,?) ON DUPLICATE KEY UPDATE visited_at = VALUES(visited_at)"
                            : "INSERT INTO recent (player_uuid, warp_id, visited_at) VALUES (?,?,?) ON CONFLICT(player_uuid, warp_id) DO UPDATE SET visited_at = excluded.visited_at";
                    try (PreparedStatement statement = connection.prepareStatement(recent)) {
                        statement.setString(1, playerId.toString());
                        statement.setLong(2, warpId);
                        statement.setLong(3, visitedAt);
                        statement.executeUpdate();
                    }
                    connection.commit();
                } catch (SQLException failure) {
                    connection.rollback();
                    throw failure;
                }
            }
        }
    }

    public StoredSnapshot loadAll() throws SQLException {
        synchronized (writeLock) {
            StoredSnapshot snapshot = new StoredSnapshot();
            try (Connection connection = connection()) {
                try (PreparedStatement statement = connection.prepareStatement(
                        "SELECT id, name, owner_uuid, owner_name, world, x, y, z, yaw, pitch, description, icon, category, price_cents, access, balance_cents, visits, created_at FROM warps");
                     ResultSet rows = statement.executeQuery()) {
                    while (rows.next()) {
                        snapshot.warps().add(readWarp(rows));
                    }
                }
                try (PreparedStatement statement = connection.prepareStatement("SELECT warp_id, player_uuid, score FROM ratings");
                     ResultSet rows = statement.executeQuery()) {
                    while (rows.next()) {
                        snapshot.ratings().rate(rows.getLong(1), UUID.fromString(rows.getString(2)), rows.getInt(3));
                    }
                }
                try (PreparedStatement statement = connection.prepareStatement("SELECT warp_id, player_uuid FROM favorites");
                     ResultSet rows = statement.executeQuery()) {
                    while (rows.next()) {
                        snapshot.favorites().computeIfAbsent(rows.getLong(1), ignored -> new HashSet<>())
                                .add(UUID.fromString(rows.getString(2)));
                    }
                }
                try (PreparedStatement statement = connection.prepareStatement("SELECT warp_id, player_uuid, player_name FROM whitelist");
                     ResultSet rows = statement.executeQuery()) {
                    while (rows.next()) {
                        String rawId = rows.getString(2);
                        UUID playerId = rawId == null || rawId.isBlank() ? null : UUID.fromString(rawId);
                        snapshot.whitelist().computeIfAbsent(rows.getLong(1), ignored -> new ArrayList<>())
                                .add(new WhitelistEntry(playerId, rows.getString(3)));
                    }
                }
                try (PreparedStatement statement = connection.prepareStatement("SELECT player_uuid, warp_id, visited_at FROM recent");
                     ResultSet rows = statement.executeQuery()) {
                    while (rows.next()) {
                        snapshot.recent().computeIfAbsent(UUID.fromString(rows.getString(1)), ignored -> new HashMap<>())
                                .put(rows.getLong(2), rows.getLong(3));
                    }
                }
            }
            return snapshot;
        }
    }

    private void createTables() throws SQLException {
        String id = mysql ? "BIGINT NOT NULL AUTO_INCREMENT" : "INTEGER PRIMARY KEY AUTOINCREMENT";
        String primary = mysql ? "PRIMARY KEY (id)" : "";
        String warpTable = "CREATE TABLE IF NOT EXISTS warps ("
                + "id " + id + ","
                + "name VARCHAR(32) NOT NULL,"
                + "name_key VARCHAR(32) NOT NULL UNIQUE,"
                + "owner_uuid VARCHAR(36) NOT NULL,"
                + "owner_name VARCHAR(16) NOT NULL,"
                + "world VARCHAR(64) NOT NULL,"
                + "x DOUBLE NOT NULL,"
                + "y DOUBLE NOT NULL,"
                + "z DOUBLE NOT NULL,"
                + "yaw FLOAT NOT NULL,"
                + "pitch FLOAT NOT NULL,"
                + "description VARCHAR(512) NOT NULL,"
                + "icon VARCHAR(64) NOT NULL,"
                + "category VARCHAR(64) NOT NULL,"
                + "price_cents BIGINT NOT NULL,"
                + "access VARCHAR(16) NOT NULL,"
                + "balance_cents BIGINT NOT NULL,"
                + "visits INT NOT NULL,"
                + "created_at BIGINT NOT NULL"
                + (mysql ? "," + primary : "")
                + ")";
        try (Connection connection = connection(); Statement statement = connection.createStatement()) {
            statement.execute(warpTable);
            statement.execute("CREATE TABLE IF NOT EXISTS ratings ("
                    + "warp_id BIGINT NOT NULL,"
                    + "player_uuid VARCHAR(36) NOT NULL,"
                    + "score INT NOT NULL,"
                    + "PRIMARY KEY (warp_id, player_uuid)"
                    + ")");
            statement.execute("CREATE TABLE IF NOT EXISTS favorites ("
                    + "warp_id BIGINT NOT NULL,"
                    + "player_uuid VARCHAR(36) NOT NULL,"
                    + "PRIMARY KEY (warp_id, player_uuid)"
                    + ")");
            statement.execute("CREATE TABLE IF NOT EXISTS whitelist ("
                    + "warp_id BIGINT NOT NULL,"
                    + "name_key VARCHAR(16) NOT NULL,"
                    + "player_uuid VARCHAR(36) NOT NULL,"
                    + "player_name VARCHAR(16) NOT NULL,"
                    + "PRIMARY KEY (warp_id, name_key)"
                    + ")");
            statement.execute("CREATE TABLE IF NOT EXISTS recent ("
                    + "player_uuid VARCHAR(36) NOT NULL,"
                    + "warp_id BIGINT NOT NULL,"
                    + "visited_at BIGINT NOT NULL,"
                    + "PRIMARY KEY (player_uuid, warp_id)"
                    + ")");
            try {
                statement.execute("CREATE INDEX IF NOT EXISTS idx_warps_owner ON warps (owner_uuid)");
            } catch (SQLException ignored) {
                try {
                    statement.execute("CREATE INDEX idx_warps_owner ON warps (owner_uuid)");
                } catch (SQLException duplicate) {
                    // The index already exists from an earlier start.
                }
            }
        }
    }

    private int countOwned(Connection connection, UUID ownerId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM warps WHERE owner_uuid = ?")) {
            statement.setString(1, ownerId.toString());
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? rows.getInt(1) : 0;
            }
        }
    }

    private void deleteChildren(Connection connection, long id) throws SQLException {
        for (String table : List.of("ratings", "favorites", "whitelist", "recent")) {
            try (PreparedStatement statement = connection.prepareStatement("DELETE FROM " + table + " WHERE warp_id = ?")) {
                statement.setLong(1, id);
                statement.executeUpdate();
            }
        }
    }

    private void bindWarp(PreparedStatement statement, WarpRecord record) throws SQLException {
        statement.setString(1, record.name());
        statement.setString(2, record.nameKey());
        statement.setString(3, record.ownerId().toString());
        statement.setString(4, record.ownerName());
        statement.setString(5, record.world());
        statement.setDouble(6, record.x());
        statement.setDouble(7, record.y());
        statement.setDouble(8, record.z());
        statement.setFloat(9, record.yaw());
        statement.setFloat(10, record.pitch());
        statement.setString(11, record.description());
        statement.setString(12, record.icon());
        statement.setString(13, record.category());
        statement.setLong(14, record.priceCents());
        statement.setString(15, record.access().name());
        statement.setLong(16, record.balanceCents());
        statement.setInt(17, record.visits());
        statement.setLong(18, record.createdAt());
    }

    private WarpRecord readWarp(ResultSet rows) throws SQLException {
        WarpAccess access = WarpAccess.parse(rows.getString("access"));
        return new WarpRecord(
                rows.getLong("id"),
                rows.getString("name"),
                UUID.fromString(rows.getString("owner_uuid")),
                rows.getString("owner_name"),
                rows.getString("world"),
                rows.getDouble("x"),
                rows.getDouble("y"),
                rows.getDouble("z"),
                rows.getFloat("yaw"),
                rows.getFloat("pitch"),
                rows.getString("description"),
                rows.getString("icon"),
                rows.getString("category"),
                rows.getLong("price_cents"),
                access == null ? WarpAccess.PUBLIC : access,
                rows.getLong("balance_cents"),
                rows.getInt("visits"),
                rows.getLong("created_at")
        );
    }

    private Connection connection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("Storage is not open");
        }
        return dataSource.getConnection();
    }

    private void loadDriver(String className) throws SQLException {
        try {
            Class.forName(className, true, plugin.getClass().getClassLoader());
        } catch (ClassNotFoundException failure) {
            throw new SQLException("Missing JDBC driver " + className, failure);
        }
    }

    private static boolean duplicate(SQLException exception) {
        String state = exception.getSQLState();
        if ("23000".equals(state) || "23505".equals(state)) {
            return true;
        }
        String message = exception.getMessage();
        if (message == null) {
            return false;
        }
        String lower = message.toLowerCase(Locale.ROOT);
        return lower.contains("unique") || lower.contains("duplicate");
    }
}
