package me.rexsystems.rexPlayerWarps.service;

import me.rexsystems.rexPlayerWarps.config.PluginConfig;
import me.rexsystems.rexPlayerWarps.model.WarpRecord;
import org.bukkit.entity.Player;

public final class WarpRules {

    private WarpRules() {
    }

    public static boolean canSee(Player player, WarpRecord warp, WarpCache cache, PluginConfig settings) {
        boolean owner = warp.ownerId().equals(player.getUniqueId());
        boolean admin = player.hasPermission("rexplayerwarps.admin");
        if (!settings.worldAllowed(warp.world()) && !owner && !admin) {
            return false;
        }
        if (owner || admin) {
            return true;
        }
        return allowed(player, warp, cache);
    }

    public static boolean canTeleport(Player player, WarpRecord warp, WarpCache cache, PluginConfig settings) {
        if (!settings.worldAllowed(warp.world())) {
            return false;
        }
        if (player.hasPermission("rexplayerwarps.admin") || warp.ownerId().equals(player.getUniqueId())) {
            return true;
        }
        return allowed(player, warp, cache);
    }

    public static boolean canEdit(Player player, WarpRecord warp) {
        return warp.ownerId().equals(player.getUniqueId()) || player.hasPermission("rexplayerwarps.admin");
    }

    public static long priceFor(Player player, WarpRecord warp, PluginConfig settings) {
        if (!settings.ownerPays() && warp.ownerId().equals(player.getUniqueId())) {
            return 0L;
        }
        return Math.max(0L, warp.priceCents());
    }

    private static boolean allowed(Player player, WarpRecord warp, WarpCache cache) {
        return switch (warp.access()) {
            case PUBLIC -> true;
            case PRIVATE -> false;
            case WHITELIST -> cache.whitelisted(warp.id(), player.getUniqueId(), player.getName());
        };
    }

    public static boolean validName(String name, int maxLength) {
        if (name == null || name.isBlank() || name.length() > maxLength) {
            return false;
        }
        for (int index = 0; index < name.length(); index++) {
            char character = name.charAt(index);
            if (!Character.isLetterOrDigit(character) && character != '_' && character != '-') {
                return false;
            }
        }
        return true;
    }
}
