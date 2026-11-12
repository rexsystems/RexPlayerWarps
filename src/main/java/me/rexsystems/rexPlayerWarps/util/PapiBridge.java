package me.rexsystems.rexPlayerWarps.util;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

public final class PapiBridge {

    private static volatile boolean enabled;

    private PapiBridge() {
    }

    public static void setEnabled(boolean value) {
        enabled = value;
    }

    public static boolean enabled() {
        return enabled;
    }

    public static String apply(Player player, String text) {
        if (!enabled || player == null || text == null || text.isEmpty()) {
            return text == null ? "" : text;
        }
        return PlaceholderAPI.setPlaceholders(player, text);
    }
}
