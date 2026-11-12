package me.rexsystems.rexPlayerWarps.economy;

import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.function.Consumer;

public interface WarpEconomy {

    boolean available();

    void withdraw(Player player, long cents, Consumer<Boolean> done);

    void deposit(Player player, long cents, Consumer<Boolean> done);

    void depositAccount(UUID playerId, String name, long cents, Consumer<Boolean> done);
}
