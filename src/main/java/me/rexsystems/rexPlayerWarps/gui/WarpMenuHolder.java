package me.rexsystems.rexPlayerWarps.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;
import java.util.Map;

public final class WarpMenuHolder implements InventoryHolder {

    private Inventory inventory;
    private final Map<Integer, Long> warps = new HashMap<>();
    private final Map<Integer, String> buttons = new HashMap<>();

    public void inventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void warp(int slot, long id) {
        warps.put(slot, id);
    }

    public Long warp(int slot) {
        return warps.get(slot);
    }

    public void button(int slot, String id) {
        buttons.put(slot, id);
    }

    public String button(int slot) {
        return buttons.get(slot);
    }
}
