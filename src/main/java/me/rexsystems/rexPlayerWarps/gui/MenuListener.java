package me.rexsystems.rexPlayerWarps.gui;

import me.rexsystems.rexPlayerWarps.RexPlayerWarps;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

public final class MenuListener implements Listener {

    private final RexPlayerWarps plugin;

    public MenuListener(RexPlayerWarps plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof WarpMenuHolder holder)) {
            return;
        }
        // Clicks outside the menu use a null inventory. Those are left uncancelled.
        if (event.getClickedInventory() == null) {
            return;
        }
        if (event.getClickedInventory() != event.getView().getTopInventory()) {
            if (event.isShiftClick()) {
                event.setCancelled(true);
            }
            return;
        }
        event.setCancelled(true);
        if (event.getWhoClicked() instanceof Player player) {
            plugin.menus().click(player, holder, event.getSlot(), event.getClick());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDrag(InventoryDragEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof WarpMenuHolder)) {
            return;
        }
        int top = event.getView().getTopInventory().getSize();
        for (int raw : event.getRawSlots()) {
            if (raw < top) {
                event.setCancelled(true);
                return;
            }
        }
    }
}
