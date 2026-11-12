package me.rexsystems.rexPlayerWarps.config;

import me.rexsystems.rexPlayerWarps.model.WarpAccess;
import me.rexsystems.rexPlayerWarps.model.WarpSortKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class PluginConfig {

    private FileConfiguration config = null;
    private List<WarpSortKey> sorts = List.of(WarpSortKey.NAME);
    private Set<String> unsafeBlocks = Set.of();
    private final Map<String, ButtonSpec> buttons = new LinkedHashMap<>();

    public void load(FileConfiguration configuration) {
        this.config = configuration;
        this.sorts = WarpSortKey.parseList(configuration.getStringList("sorts"));
        Set<String> blocks = new HashSet<>();
        for (String block : configuration.getStringList("unsafe.blocks")) {
            if (block != null && !block.isBlank()) {
                blocks.add(block.trim().toUpperCase(Locale.ROOT));
            }
        }
        this.unsafeBlocks = Set.copyOf(blocks);
        this.buttons.clear();
        ConfigurationSection section = configuration.getConfigurationSection("gui.buttons");
        if (section != null) {
            for (String id : section.getKeys(false)) {
                ConfigurationSection button = section.getConfigurationSection(id);
                if (button == null) {
                    continue;
                }
                buttons.put(id.toLowerCase(Locale.ROOT), new ButtonSpec(
                        id.toLowerCase(Locale.ROOT),
                        button.getInt("slot", -1),
                        button.getString("material", "STONE"),
                        button.getString("name", " "),
                        button.getStringList("lore"),
                        button.getInt("custom-model-data", 0),
                        button.getBoolean("glow", false)
                ));
            }
        }
    }

    public String text(String path) {
        if (config == null) {
            return "";
        }
        return config.getString(path, "");
    }

    public List<String> textList(String path) {
        if (config == null) {
            return List.of();
        }
        return config.getStringList(path);
    }

    public boolean mysql() {
        return "mysql".equalsIgnoreCase(text("storage.type"));
    }

    public String storageType() {
        return mysql() ? "mysql" : "sqlite";
    }

    public File sqliteFile(File folder) {
        String name = text("storage.sqlite.file");
        if (name == null || name.isBlank()) {
            name = "warps.db";
        }
        File file = new File(name);
        if (file.isAbsolute()) {
            return file;
        }
        return new File(folder, name);
    }

    public String mysqlHost() {
        String host = text("storage.mysql.host");
        return host == null || host.isBlank() ? "127.0.0.1" : host;
    }

    public int mysqlPort() {
        return clamp(config == null ? 3306 : config.getInt("storage.mysql.port", 3306), 1, 65535);
    }

    public String mysqlDatabase() {
        String name = text("storage.mysql.database");
        return name == null || name.isBlank() ? "rexplayerwarps" : name;
    }

    public String mysqlUsername() {
        return text("storage.mysql.username");
    }

    public String mysqlPassword() {
        return text("storage.mysql.password");
    }

    public int mysqlPoolSize() {
        return clamp(config == null ? 4 : config.getInt("storage.mysql.pool-size", 4), 1, 20);
    }

    public boolean worldAllowed(String world) {
        if (world == null || config == null) {
            return false;
        }
        for (String blocked : config.getStringList("worlds.blacklist")) {
            if (world.equalsIgnoreCase(blocked)) {
                return false;
            }
        }
        List<String> allowed = config.getStringList("worlds.whitelist");
        if (allowed.isEmpty()) {
            return true;
        }
        for (String entry : allowed) {
            if (world.equalsIgnoreCase(entry)) {
                return true;
            }
        }
        return false;
    }

    public String defaultDescription() {
        return text("defaults.description");
    }

    public String defaultIcon() {
        String icon = text("defaults.icon");
        return icon == null || icon.isBlank() ? "ENDER_PEARL" : icon;
    }

    public String defaultCategory() {
        String category = text("defaults.category");
        return category == null || category.isBlank() ? "general" : category;
    }

    public double defaultPrice() {
        return config == null ? 0 : config.getDouble("defaults.price", 0);
    }

    public WarpAccess defaultAccess() {
        WarpAccess access = WarpAccess.parse(text("defaults.access"));
        return access == null ? WarpAccess.PUBLIC : access;
    }

    public int nameLength() {
        return clamp(config == null ? 24 : config.getInt("limits.name-length", 24), 1, 32);
    }

    public int descriptionLength() {
        return clamp(config == null ? 80 : config.getInt("limits.description-length", 80), 1, 512);
    }

    public double priceMin() {
        return Math.max(0, config == null ? 0 : config.getDouble("limits.price-min", 0));
    }

    public double priceMax() {
        double min = priceMin();
        double max = config == null ? 1000000 : config.getDouble("limits.price-max", 1000000);
        return Math.max(min, max);
    }

    public int searchLength() {
        return clamp(config == null ? 32 : config.getInt("limits.search-length", 32), 1, 64);
    }

    public int recentSize() {
        return clamp(config == null ? 27 : config.getInt("limits.recent-size", 27), 1, 200);
    }

    public String numberFormat() {
        String format = text("number-format");
        return format == null || format.isBlank() ? "full" : format;
    }

    public String descriptionClear() {
        String word = text("description-clear");
        return word == null || word.isBlank() ? "clear" : word;
    }

    public String searchCancel() {
        String word = text("search-cancel");
        return word == null || word.isBlank() ? "cancel" : word;
    }

    public boolean unsafeConfirm() {
        return config == null || config.getBoolean("unsafe.confirm", true);
    }

    public boolean voidCheck() {
        return config == null || config.getBoolean("unsafe.void", true);
    }

    public Set<String> unsafeBlocks() {
        return unsafeBlocks;
    }

    public int delaySeconds() {
        return Math.max(0, config == null ? 3 : config.getInt("teleport.delay-seconds", 3));
    }

    public boolean cancelOnMove() {
        return config == null || config.getBoolean("teleport.cancel-on-move", true);
    }

    public boolean cancelOnDamage() {
        return config == null || config.getBoolean("teleport.cancel-on-damage", true);
    }

    public boolean ownerPays() {
        return config != null && config.getBoolean("teleport.owner-pays", false);
    }

    public boolean priceConfirm() {
        return config == null || config.getBoolean("teleport.price-confirm", true);
    }

    public int confirmSeconds() {
        return clamp(config == null ? 15 : config.getInt("teleport.confirm-seconds", 15), 1, 120);
    }

    public boolean commandEconomy() {
        return "command".equalsIgnoreCase(text("economy.mode"));
    }

    public String balancePlaceholder() {
        return text("economy.command.balance-placeholder");
    }

    public String takeCommand() {
        return text("economy.command.take");
    }

    public String giveCommand() {
        return text("economy.command.give");
    }

    public List<WarpSortKey> sorts() {
        return sorts;
    }

    public String sortLabel(WarpSortKey key) {
        if (key == null) {
            return "";
        }
        String configured = text("sort-labels." + key.name().toLowerCase(Locale.ROOT));
        return configured == null || configured.isBlank() ? key.label() : configured;
    }

    public SoundSpec sound(String key) {
        if (config == null) {
            return SoundSpec.silent();
        }
        ConfigurationSection section = config.getConfigurationSection("sounds." + key);
        if (section == null) {
            return SoundSpec.silent();
        }
        return new SoundSpec(section.getString("sound", ""), (float) section.getDouble("volume", 1), (float) section.getDouble("pitch", 1));
    }

    public TitleSpec title(String key) {
        if (config == null) {
            return TitleSpec.off();
        }
        ConfigurationSection section = config.getConfigurationSection("titles." + key);
        if (section == null) {
            return TitleSpec.off();
        }
        return new TitleSpec(
                section.getBoolean("enabled", false),
                section.getString("title", ""),
                section.getString("subtitle", ""),
                section.getInt("fade-in", 0),
                section.getInt("stay", 20),
                section.getInt("fade-out", 0)
        );
    }

    public boolean actionBarEnabled() {
        return config != null && config.getBoolean("actionbar.countdown.enabled", false);
    }

    public String actionBarText() {
        return text("actionbar.countdown.text");
    }

    public boolean bossBarEnabled() {
        return config != null && config.getBoolean("bossbar.countdown.enabled", false);
    }

    public String bossBarText() {
        return text("bossbar.countdown.text");
    }

    public String bossBarColor() {
        String color = text("bossbar.countdown.color");
        return color == null || color.isBlank() ? "YELLOW" : color;
    }

    public String bossBarStyle() {
        String style = text("bossbar.countdown.style");
        return style == null || style.isBlank() ? "SOLID" : style;
    }

    public boolean particleEnabled() {
        return config != null && config.getBoolean("particles.teleport.enabled", false);
    }

    public String particleType() {
        String type = text("particles.teleport.type");
        return type == null || type.isBlank() ? "PORTAL" : type;
    }

    public int particleCount() {
        return clamp(config == null ? 0 : config.getInt("particles.teleport.count", 24), 0, 200);
    }

    public int guiRows() {
        return clamp(config == null ? 6 : config.getInt("gui.rows", 6), 1, 6);
    }

    public String guiTitle() {
        String title = text("gui.title");
        return title == null || title.isBlank() ? "Warps" : title;
    }

    public boolean favoriteGlow() {
        return config == null || config.getBoolean("gui.favorite-glow", true);
    }

    public String clickTeleport() {
        String click = text("gui.clicks.teleport");
        return click == null || click.isBlank() ? "LEFT" : click;
    }

    public String clickFavorite() {
        String click = text("gui.clicks.favorite");
        return click == null || click.isBlank() ? "RIGHT" : click;
    }

    public ItemSpec filler() {
        if (config == null) {
            return new ItemSpec("AIR", " ", List.of(), 0, false);
        }
        ConfigurationSection section = config.getConfigurationSection("gui.filler");
        if (section == null) {
            return new ItemSpec("AIR", " ", List.of(), 0, false);
        }
        return new ItemSpec(
                section.getString("material", "AIR"),
                section.getString("name", " "),
                section.getStringList("lore"),
                section.getInt("custom-model-data", 0),
                section.getBoolean("glow", false)
        );
    }

    public List<Integer> warpSlots(int inventorySize) {
        List<Integer> slots = new ArrayList<>();
        if (config == null) {
            return slots;
        }
        for (int slot : config.getIntegerList("gui.warp-slots")) {
            if (slot >= 0 && slot < inventorySize && !slots.contains(slot)) {
                slots.add(slot);
            }
        }
        return slots;
    }

    public ButtonSpec button(String id) {
        ButtonSpec spec = buttons.get(id.toLowerCase(Locale.ROOT));
        if (spec == null) {
            return new ButtonSpec(id, -1, "STONE", "", List.of(), 0, false);
        }
        return spec;
    }

    public Map<String, ButtonSpec> buttons() {
        return Collections.unmodifiableMap(buttons);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public record SoundSpec(String sound, float volume, float pitch) {
        public static SoundSpec silent() {
            return new SoundSpec("", 1f, 1f);
        }
    }

    public record TitleSpec(boolean enabled, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        public static TitleSpec off() {
            return new TitleSpec(false, "", "", 0, 0, 0);
        }
    }

    public record ItemSpec(String material, String name, List<String> lore, int modelData, boolean glow) {
    }

    public record ButtonSpec(String id, int slot, String material, String name, List<String> lore, int modelData, boolean glow) {
    }
}
