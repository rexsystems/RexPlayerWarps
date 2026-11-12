package me.rexsystems.rexPlayerWarps.service;

import me.rexsystems.rexPlayerWarps.RexPlayerWarps;
import me.rexsystems.rexPlayerWarps.economy.WarpEconomy;
import me.rexsystems.rexPlayerWarps.logic.TeleportCharges;
import me.rexsystems.rexPlayerWarps.logic.UnsafeFeet;
import me.rexsystems.rexPlayerWarps.model.WarpRecord;
import me.rexsystems.rexPlayerWarps.util.MessageService;
import me.rexsystems.rexPlayerWarps.util.SchedulerUtils;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public final class TeleportManager {

    private final RexPlayerWarps plugin;
    private final Map<UUID, Session> sessions = new ConcurrentHashMap<>();
    private final Map<UUID, Pending> pending = new ConcurrentHashMap<>();
    private volatile boolean closing;

    public TeleportManager(RexPlayerWarps plugin) {
        this.plugin = plugin;
    }

    public boolean busy(UUID playerId) {
        return sessions.containsKey(playerId);
    }

    public void request(Player player, WarpRecord warp) {
        if (!player.hasPermission("rexplayerwarps.use")) {
            plugin.texts().send(player, "no-permission");
            return;
        }
        if (busy(player.getUniqueId())) {
            plugin.texts().send(player, "already-teleporting");
            return;
        }
        if (!plugin.settings().worldAllowed(warp.world())) {
            plugin.texts().send(player, "world-blocked", Map.of("warp", warp.name()));
            return;
        }
        if (!WarpRules.canTeleport(player, warp, plugin.cache(), plugin.settings())) {
            plugin.texts().send(player, "cannot-visit", Map.of("warp", warp.name()));
            return;
        }
        World world = Bukkit.getWorld(warp.world());
        if (world == null) {
            plugin.texts().send(player, "world-missing", Map.of("warp", warp.name()));
            return;
        }
        Location location = new Location(world, warp.x(), warp.y(), warp.z(), warp.yaw(), warp.pitch());
        long price = WarpRules.priceFor(player, warp, plugin.settings());
        SchedulerUtils.runAtLocation(plugin, location, () -> {
            String feet = location.getBlock().getType().name();
            String below = location.clone().subtract(0, 1, 0).getBlock().getType().name();
            boolean unsafe = UnsafeFeet.unsafe(plugin.settings().unsafeBlocks(), plugin.settings().voidCheck(),
                    location.getY(), world.getMinHeight(), feet, below);
            String block = blockName(feet, below, unsafe);
            SchedulerUtils.runForPlayer(plugin, player, () -> continueRequest(player, warp, price, unsafe, block));
        });
    }

    public void moved(Player player) {
        if (!plugin.settings().cancelOnMove()) {
            return;
        }
        Session session = sessions.get(player.getUniqueId());
        if (session == null || session.finished.get()) {
            return;
        }
        if (moved(player, session.origin)) {
            cancel(player, session, "cancel-move");
        }
    }

    public void damaged(Player player) {
        if (!plugin.settings().cancelOnDamage()) {
            return;
        }
        Session session = sessions.get(player.getUniqueId());
        if (session == null || session.finished.get()) {
            return;
        }
        cancel(player, session, "cancel-damage");
    }

    public void quit(Player player) {
        pending.remove(player.getUniqueId());
        Session session = sessions.get(player.getUniqueId());
        if (session == null) {
            return;
        }
        if (session.finished.compareAndSet(false, true)) {
            if (session.task != null) {
                session.task.cancel();
            }
            hide(player, session);
            sessions.remove(player.getUniqueId());
        }
    }

    public void shutdown() {
        closing = true;
        for (UUID playerId : sessions.keySet()) {
            Player player = Bukkit.getPlayer(playerId);
            Session session = sessions.get(playerId);
            if (session != null && session.finished.compareAndSet(false, true)) {
                if (session.task != null) {
                    session.task.cancel();
                }
                if (player != null) {
                    hide(player, session);
                }
            }
        }
        sessions.clear();
        pending.clear();
    }

    private void continueRequest(Player player, WarpRecord warp, long price, boolean unsafe, String block) {
        if (!player.isOnline() || closing || busy(player.getUniqueId())) {
            return;
        }
        boolean needPrice = plugin.settings().priceConfirm() && price > 0;
        boolean needUnsafe = plugin.settings().unsafeConfirm() && unsafe;
        if (!needPrice && !needUnsafe) {
            begin(player, warp, price);
            return;
        }
        long now = System.currentTimeMillis();
        Pending existing = pending.get(player.getUniqueId());
        if (existing != null && existing.warpId == warp.id() && existing.priceCents == price
                && existing.unsafe == unsafe && existing.expiresAt >= now) {
            pending.remove(player.getUniqueId());
            begin(player, warp, price);
            return;
        }
        pending.put(player.getUniqueId(), new Pending(warp.id(), price, unsafe, now + plugin.settings().confirmSeconds() * 1000L));
        String key = needPrice && needUnsafe ? "confirm-both" : needPrice ? "confirm-price" : "confirm-unsafe";
        plugin.texts().send(player, key, Map.of(
                "warp", warp.name(),
                "price", plugin.texts().money(price),
                "block", block
        ));
        plugin.texts().sound(player, "error");
    }

    private void begin(Player player, WarpRecord warp, long price) {
        pending.remove(player.getUniqueId());
        Session session = new Session();
        session.playerId = player.getUniqueId();
        session.playerName = player.getName();
        session.warpId = warp.id();
        session.warpName = warp.name();
        session.ownerName = warp.ownerName();
        session.priceCents = price;
        session.origin = player.getLocation().clone();
        session.delaySeconds = plugin.settings().delaySeconds();
        session.endsAt = System.currentTimeMillis() + session.delaySeconds * 1000L;
        session.economy = plugin.economy();
        if (sessions.putIfAbsent(player.getUniqueId(), session) != null) {
            plugin.texts().send(player, "already-teleporting");
            return;
        }
        if (session.delaySeconds <= 0) {
            commit(player, session);
            return;
        }
        if (plugin.settings().bossBarEnabled()) {
            session.bar = BossBar.bossBar(net.kyori.adventure.text.Component.empty(), 1f, barColor(), barOverlay());
            player.showBossBar(session.bar);
        }
        plugin.texts().send(player, "teleporting", Map.of(
                "warp", warp.name(),
                "seconds", Integer.toString(session.delaySeconds)
        ));
        showCountdown(player, session, session.delaySeconds);
        session.task = player.getScheduler().runAtFixedRate(plugin, scheduled -> tick(player, session, scheduled), () -> {
            if (session.finished.compareAndSet(false, true)) {
                hide(player, session);
                sessions.remove(session.playerId);
            }
        }, 10L, 10L);
    }

    private void tick(Player player, Session session, io.papermc.paper.threadedregions.scheduler.ScheduledTask scheduled) {
        if (session.finished.get() || closing) {
            scheduled.cancel();
            return;
        }
        if (!player.isOnline()) {
            if (session.finished.compareAndSet(false, true)) {
                hide(player, session);
                sessions.remove(session.playerId);
            }
            scheduled.cancel();
            return;
        }
        if (plugin.settings().cancelOnMove() && moved(player, session.origin)) {
            scheduled.cancel();
            cancel(player, session, "cancel-move");
            return;
        }
        long remaining = session.endsAt - System.currentTimeMillis();
        if (remaining <= 0) {
            scheduled.cancel();
            commit(player, session);
            return;
        }
        int seconds = (int) Math.max(1, (remaining + 999) / 1000);
        showCountdown(player, session, seconds);
    }

    private void cancel(Player player, Session session, String message) {
        if (!session.finished.compareAndSet(false, true)) {
            return;
        }
        if (session.task != null) {
            session.task.cancel();
        }
        hide(player, session);
        sessions.remove(player.getUniqueId());
        if (player.isOnline()) {
            MessageService.clearTitle(player);
            plugin.texts().send(player, message, Map.of("warp", session.warpName));
            plugin.texts().sound(player, "cancel");
        }
    }

    private void commit(Player player, Session session) {
        if (!session.finished.compareAndSet(false, true)) {
            return;
        }
        if (session.task != null) {
            session.task.cancel();
        }
        hide(player, session);
        MessageService.clearTitle(player);
        if (!player.isOnline() || closing) {
            sessions.remove(session.playerId);
            return;
        }
        WarpRecord warp = plugin.cache().byId(session.warpId);
        if (warp == null) {
            sessions.remove(session.playerId);
            plugin.texts().send(player, "warp-removed");
            return;
        }
        if (!plugin.settings().worldAllowed(warp.world())) {
            sessions.remove(session.playerId);
            plugin.texts().send(player, "world-blocked", Map.of("warp", warp.name()));
            return;
        }
        if (!WarpRules.canTeleport(player, warp, plugin.cache(), plugin.settings())) {
            sessions.remove(session.playerId);
            plugin.texts().send(player, "cannot-visit", Map.of("warp", warp.name()));
            return;
        }
        World world = Bukkit.getWorld(warp.world());
        if (world == null) {
            sessions.remove(session.playerId);
            plugin.texts().send(player, "world-missing", Map.of("warp", warp.name()));
            return;
        }
        Location destination = new Location(world, warp.x(), warp.y(), warp.z(), warp.yaw(), warp.pitch());
        long price = session.priceCents;
        session.economy.withdraw(player, price, paid -> {
            if (player.isOnline()) {
                SchedulerUtils.runForPlayer(plugin, player, () -> afterPayment(player, session, warp, destination, price, paid));
            } else if (paid && price > 0) {
                session.economy.depositAccount(session.playerId, session.playerName, price, ignored -> sessions.remove(session.playerId));
            } else {
                sessions.remove(session.playerId);
            }
        });
    }

    private void afterPayment(Player player, Session session, WarpRecord warp, Location destination, long price, boolean paid) {
        if (!player.isOnline() || closing) {
            if (paid && price > 0) {
                session.economy.depositAccount(session.playerId, session.playerName, price, ignored -> sessions.remove(session.playerId));
            } else {
                sessions.remove(session.playerId);
            }
            return;
        }
        if (!paid) {
            sessions.remove(session.playerId);
            if (price > 0 && !session.economy.available()) {
                plugin.texts().send(player, "economy-missing");
            } else {
                plugin.texts().send(player, "broke", Map.of("warp", warp.name(), "price", plugin.texts().money(price)));
            }
            plugin.texts().sound(player, "error");
            return;
        }
        boolean charged = price > 0;
        try {
            player.teleportAsync(destination, PlayerTeleportEvent.TeleportCause.PLUGIN).whenComplete((success, error) -> {
                boolean teleported = error == null && Boolean.TRUE.equals(success);
                if (!player.isOnline()) {
                    sessions.remove(session.playerId);
                    if (TeleportCharges.shouldRefund(teleported, charged)) {
                        session.economy.depositAccount(session.playerId, session.playerName, price, ignored -> {
                        });
                    }
                    return;
                }
                SchedulerUtils.runForPlayer(plugin, player, () -> finishTeleport(player, session, charged, teleported, price));
            });
        } catch (Throwable failure) {
            finishTeleport(player, session, charged, false, price);
        }
    }

    private void finishTeleport(Player player, Session session, boolean charged, boolean teleported, long price) {
        sessions.remove(session.playerId);
        Map<String, String> map = Map.of(
                "warp", session.warpName,
                "owner", session.ownerName,
                "price", plugin.texts().money(price)
        );
        if (TeleportCharges.shouldRefund(teleported, charged)) {
            session.economy.deposit(player, price, refunded -> {
                plugin.texts().send(player, "teleport-failed", map);
                plugin.texts().send(player, refunded ? "refunded" : "refund-failed", map);
                plugin.texts().sound(player, "error");
            });
            return;
        }
        if (!teleported) {
            plugin.texts().send(player, "teleport-failed", map);
            plugin.texts().sound(player, "error");
            return;
        }
        plugin.texts().send(player, "arrived", map);
        plugin.texts().title(player, "arrived", map);
        plugin.texts().sound(player, "teleport");
        spawnParticle(player);
        long when = System.currentTimeMillis();
        long credit = charged ? price : 0L;
        SchedulerUtils.runAsync(plugin, () -> {
            try {
                plugin.storage().creditVisit(session.warpId, session.playerId, credit, when);
                plugin.cache().addVisit(session.warpId, session.playerId, credit, when);
            } catch (Exception failure) {
                plugin.getLogger().warning("Could not record a warp visit: " + failure.getMessage());
            }
        });
    }

    private void showCountdown(Player player, Session session, int seconds) {
        Map<String, String> map = Map.of(
                "warp", session.warpName,
                "owner", session.ownerName,
                "seconds", Integer.toString(seconds)
        );
        plugin.texts().title(player, "countdown", map);
        if (plugin.settings().actionBarEnabled()) {
            MessageService.actionBar(player, plugin.settings().actionBarText(), plugin.texts().placeholders(player, map));
        }
        if (session.bar != null) {
            long total = Math.max(1, session.delaySeconds) * 1000L;
            long left = Math.max(0, session.endsAt - System.currentTimeMillis());
            session.bar.progress(Math.min(1f, left / (float) total));
            session.bar.name(MessageService.component(player, plugin.settings().bossBarText(), plugin.texts().placeholders(player, map)));
        }
    }

    private void hide(Player player, Session session) {
        if (session.bar != null && player != null) {
            player.hideBossBar(session.bar);
            session.bar = null;
        }
    }

    private void spawnParticle(Player player) {
        if (!plugin.settings().particleEnabled() || plugin.settings().particleCount() <= 0) {
            return;
        }
        try {
            Particle particle = Particle.valueOf(plugin.settings().particleType().trim().toUpperCase(Locale.ROOT));
            player.getWorld().spawnParticle(particle, player.getLocation(), plugin.settings().particleCount());
        } catch (IllegalArgumentException ignored) {
            // Unknown particle names in config are skipped.
        }
    }

    private BossBar.Color barColor() {
        try {
            return BossBar.Color.valueOf(plugin.settings().bossBarColor().trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return BossBar.Color.YELLOW;
        }
    }

    private BossBar.Overlay barOverlay() {
        String style = plugin.settings().bossBarStyle().trim().toUpperCase(Locale.ROOT);
        return switch (style) {
            case "SEGMENTED_6" -> BossBar.Overlay.NOTCHED_6;
            case "SEGMENTED_10" -> BossBar.Overlay.NOTCHED_10;
            case "SEGMENTED_12" -> BossBar.Overlay.NOTCHED_12;
            case "SEGMENTED_20" -> BossBar.Overlay.NOTCHED_20;
            default -> BossBar.Overlay.PROGRESS;
        };
    }

    private static boolean moved(Player player, Location origin) {
        Location now = player.getLocation();
        if (origin.getWorld() == null || now.getWorld() == null) {
            return true;
        }
        if (!origin.getWorld().getUID().equals(now.getWorld().getUID())) {
            return true;
        }
        return origin.getBlockX() != now.getBlockX()
                || origin.getBlockY() != now.getBlockY()
                || origin.getBlockZ() != now.getBlockZ();
    }

    private String blockName(String feet, String below, boolean unsafe) {
        if (!unsafe) {
            return "";
        }
        if (plugin.settings().unsafeBlocks().contains(feet)) {
            return feet.toLowerCase(Locale.ROOT).replace('_', ' ');
        }
        if (plugin.settings().unsafeBlocks().contains(below)) {
            return below.toLowerCase(Locale.ROOT).replace('_', ' ');
        }
        return "void";
    }

    private static final class Pending {
        private final long warpId;
        private final long priceCents;
        private final boolean unsafe;
        private final long expiresAt;

        private Pending(long warpId, long priceCents, boolean unsafe, long expiresAt) {
            this.warpId = warpId;
            this.priceCents = priceCents;
            this.unsafe = unsafe;
            this.expiresAt = expiresAt;
        }
    }

    private static final class Session {
        private UUID playerId;
        private String playerName;
        private long warpId;
        private String warpName;
        private String ownerName;
        private long priceCents;
        private Location origin;
        private int delaySeconds;
        private long endsAt;
        private WarpEconomy economy;
        private final AtomicBoolean finished = new AtomicBoolean();
        private io.papermc.paper.threadedregions.scheduler.ScheduledTask task;
        private BossBar bar;
    }
}
