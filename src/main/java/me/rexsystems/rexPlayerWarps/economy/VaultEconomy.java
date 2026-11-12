package me.rexsystems.rexPlayerWarps.economy;

import me.rexsystems.rexPlayerWarps.RexPlayerWarps;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import me.rexsystems.rexPlayerWarps.util.SchedulerUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.UUID;
import java.util.function.Consumer;

public final class VaultEconomy implements WarpEconomy {

    private final RexPlayerWarps plugin;
    private final Economy economy;

    private VaultEconomy(RexPlayerWarps plugin, Economy economy) {
        this.plugin = plugin;
        this.economy = economy;
    }

    public static WarpEconomy hook(RexPlayerWarps plugin) {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            return null;
        }
        RegisteredServiceProvider<Economy> registration = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (registration == null || registration.getProvider() == null) {
            plugin.getLogger().warning("Vault is installed but no economy provider is registered.");
            return null;
        }
        return new VaultEconomy(plugin, registration.getProvider());
    }

    @Override
    public boolean available() {
        return economy != null;
    }

    @Override
    public void withdraw(Player player, long cents, Consumer<Boolean> done) {
        if (cents <= 0) {
            done.accept(true);
            return;
        }
        EconomyResponse response = economy.withdrawPlayer(player, cents / 100.0);
        done.accept(response != null && response.transactionSuccess());
    }

    @Override
    public void deposit(Player player, long cents, Consumer<Boolean> done) {
        if (cents <= 0) {
            done.accept(true);
            return;
        }
        EconomyResponse response = economy.depositPlayer(player, cents / 100.0);
        done.accept(response != null && response.transactionSuccess());
    }

    @Override
    public void depositAccount(UUID playerId, String name, long cents, Consumer<Boolean> done) {
        if (cents <= 0) {
            done.accept(true);
            return;
        }
        SchedulerUtils.runSync(plugin, () -> {
            OfflinePlayer offline = Bukkit.getOfflinePlayer(playerId);
            EconomyResponse response = economy.depositPlayer(offline, cents / 100.0);
            done.accept(response != null && response.transactionSuccess());
        });
    }
}
