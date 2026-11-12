package me.rexsystems.rexPlayerWarps;

import me.rexsystems.rexPlayerWarps.command.WarpCommand;
import me.rexsystems.rexPlayerWarps.config.Categories;
import me.rexsystems.rexPlayerWarps.config.ConfigAutoUpdater;
import me.rexsystems.rexPlayerWarps.config.PluginConfig;
import me.rexsystems.rexPlayerWarps.economy.CommandEconomy;
import me.rexsystems.rexPlayerWarps.economy.MissingEconomy;
import me.rexsystems.rexPlayerWarps.economy.VaultEconomy;
import me.rexsystems.rexPlayerWarps.economy.WarpEconomy;
import me.rexsystems.rexPlayerWarps.gui.MenuListener;
import me.rexsystems.rexPlayerWarps.gui.MenuService;
import me.rexsystems.rexPlayerWarps.listen.PlayerListener;
import me.rexsystems.rexPlayerWarps.papi.WarpPlaceholders;
import me.rexsystems.rexPlayerWarps.service.TeleportManager;
import me.rexsystems.rexPlayerWarps.service.Texts;
import me.rexsystems.rexPlayerWarps.service.WarpActions;
import me.rexsystems.rexPlayerWarps.service.WarpCache;
import me.rexsystems.rexPlayerWarps.storage.SqlWarpStorage;
import me.rexsystems.rexPlayerWarps.storage.StoredSnapshot;
import me.rexsystems.rexPlayerWarps.util.PapiBridge;
import me.rexsystems.rexPlayerWarps.util.SchedulerUtils;
import org.bukkit.command.PluginCommand;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Level;

public final class RexPlayerWarps extends JavaPlugin {

    private static final int BSTATS_PLUGIN_ID = 35273;

    private PluginConfig settings;
    private Categories categories;
    private SqlWarpStorage storage;
    private WarpCache cache;
    private WarpEconomy economy = new MissingEconomy();
    private Texts texts;
    private TeleportManager teleports;
    private WarpActions actions;
    private MenuService menus;
    private WarpPlaceholders placeholders;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        File categoriesFile = new File(getDataFolder(), "categories.yml");
        if (!categoriesFile.exists()) {
            saveResource("categories.yml", false);
        }
        new ConfigAutoUpdater(this).update();
        settings = new PluginConfig();
        settings.load(getConfig());
        categories = new Categories();
        categories.load(categoriesFile);
        cache = new WarpCache();
        storage = new SqlWarpStorage(this);
        texts = new Texts(this);
        economy = wireEconomy();
        teleports = new TeleportManager(this);
        actions = new WarpActions(this);
        menus = new MenuService(this);

        PluginCommand command = getCommand("rexplayerwarps");
        if (command != null) {
            WarpCommand executor = new WarpCommand(this);
            command.setExecutor(executor);
            command.setTabCompleter(executor);
        }
        getServer().getPluginManager().registerEvents(new MenuListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            PapiBridge.setEnabled(true);
            placeholders = new WarpPlaceholders(this);
            placeholders.register();
        }
        SchedulerUtils.runAsync(this, () -> {
            try {
                storage.open(settings);
                StoredSnapshot snapshot = storage.loadAll();
                SchedulerUtils.runSync(this, () -> {
                    cache.replace(snapshot);
                    getLogger().info("Storage ready (" + settings.storageType() + ").");
                });
            } catch (Exception failure) {
                getLogger().log(Level.SEVERE, "Storage failed", failure);
            }
        });
        new Metrics(this, BSTATS_PLUGIN_ID);
        getLogger().info("Enabled.");
    }

    @Override
    public void onDisable() {
        if (teleports != null) {
            teleports.shutdown();
        }
        if (placeholders != null) {
            placeholders.unregister();
            placeholders = null;
        }
        PapiBridge.setEnabled(false);
        if (storage != null) {
            storage.close();
        }
    }

    public void reloadPlugin() {
        reloadConfig();
        new ConfigAutoUpdater(this).update();
        settings.load(getConfig());
        File categoriesFile = new File(getDataFolder(), "categories.yml");
        if (!categoriesFile.exists()) {
            saveResource("categories.yml", false);
        }
        categories.load(categoriesFile);
        economy = wireEconomy();
    }

    private WarpEconomy wireEconomy() {
        if (settings.commandEconomy()) {
            return new CommandEconomy(this);
        }
        WarpEconomy vault = VaultEconomy.hook(this);
        return vault == null ? new MissingEconomy() : vault;
    }

    public PluginConfig settings() {
        return settings;
    }

    public Categories categories() {
        return categories;
    }

    public SqlWarpStorage storage() {
        return storage;
    }

    public WarpCache cache() {
        return cache;
    }

    public WarpEconomy economy() {
        return economy;
    }

    public Texts texts() {
        return texts;
    }

    public TeleportManager teleports() {
        return teleports;
    }

    public WarpActions actions() {
        return actions;
    }

    public MenuService menus() {
        return menus;
    }
}
