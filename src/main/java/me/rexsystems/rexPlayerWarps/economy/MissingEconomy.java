package me.rexsystems.rexPlayerWarps.economy;

import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.function.Consumer;

public final class MissingEconomy implements WarpEconomy {

    @Override
    public boolean available() {
        return false;
    }

    @Override
    public void withdraw(Player player, long cents, Consumer<Boolean> done) {
        done.accept(cents <= 0);
    }

    @Override
    public void deposit(Player player, long cents, Consumer<Boolean> done) {
        done.accept(cents <= 0);
    }

    @Override
    public void depositAccount(UUID playerId, String name, long cents, Consumer<Boolean> done) {
        done.accept(cents <= 0);
    }
}
