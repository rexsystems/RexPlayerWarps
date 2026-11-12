package me.rexsystems.rexPlayerWarps.economy;

import me.rexsystems.rexPlayerWarps.RexPlayerWarps;
import me.rexsystems.rexPlayerWarps.logic.BalanceText;
import me.rexsystems.rexPlayerWarps.logic.Cents;
import me.rexsystems.rexPlayerWarps.util.PapiBridge;
import me.rexsystems.rexPlayerWarps.util.SchedulerUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.function.Consumer;

public final class CommandEconomy implements WarpEconomy {

    private final RexPlayerWarps plugin;

    public CommandEconomy(RexPlayerWarps plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean available() {
        return PapiBridge.enabled()
                && !plugin.settings().takeCommand().isBlank()
                && !plugin.settings().giveCommand().isBlank();
    }

    @Override
    public void withdraw(Player player, long cents, Consumer<Boolean> done) {
        if (cents <= 0) {
            done.accept(true);
            return;
        }
        if (!available()) {
            done.accept(false);
            return;
        }
        String raw = PapiBridge.apply(player, plugin.settings().balancePlaceholder());
        Double balance = BalanceText.parse(raw);
        if (balance == null || Cents.from(balance) < cents) {
            done.accept(false);
            return;
        }
        String command = BalanceText.command(plugin.settings().takeCommand(), player.getName(), Cents.format(cents));
        SchedulerUtils.runSync(plugin, () -> {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            if (player.isOnline()) {
                SchedulerUtils.runForPlayer(plugin, player, () -> done.accept(true));
            } else {
                done.accept(true);
            }
        });
    }

    @Override
    public void deposit(Player player, long cents, Consumer<Boolean> done) {
        if (cents <= 0) {
            done.accept(true);
            return;
        }
        if (plugin.settings().giveCommand().isBlank()) {
            done.accept(false);
            return;
        }
        String command = BalanceText.command(plugin.settings().giveCommand(), player.getName(), Cents.format(cents));
        SchedulerUtils.runSync(plugin, () -> {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            if (player.isOnline()) {
                SchedulerUtils.runForPlayer(plugin, player, () -> done.accept(true));
            } else {
                done.accept(true);
            }
        });
    }

    @Override
    public void depositAccount(UUID playerId, String name, long cents, Consumer<Boolean> done) {
        if (cents <= 0) {
            done.accept(true);
            return;
        }
        if (plugin.settings().giveCommand().isBlank() || name == null || name.isBlank()) {
            done.accept(false);
            return;
        }
        String command = BalanceText.command(plugin.settings().giveCommand(), name, Cents.format(cents));
        SchedulerUtils.runSync(plugin, () -> {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            done.accept(true);
        });
    }
}
