package me.rexsystems.rexPlayerWarps.papi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.rexsystems.rexPlayerWarps.RexPlayerWarps;
import me.rexsystems.rexPlayerWarps.logic.WarpLimits;
import me.rexsystems.rexPlayerWarps.service.WarpActions;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class WarpPlaceholders extends PlaceholderExpansion {

    private final RexPlayerWarps plugin;

    public WarpPlaceholders(RexPlayerWarps plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "rexplayerwarps";
    }

    @Override
    public @NotNull String getAuthor() {
        return "RexSystems";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) {
            return "";
        }
        if (!plugin.cache().ready()) {
            return "";
        }
        if (params.equalsIgnoreCase("count")) {
            return Integer.toString(plugin.cache().countOwned(player.getUniqueId()));
        }
        if (params.equalsIgnoreCase("bank")) {
            return plugin.texts().money(plugin.cache().bank(player.getUniqueId()));
        }
        if (params.equalsIgnoreCase("limit")) {
            Player online = player.getPlayer();
            if (online == null) {
                return "0";
            }
            return Integer.toString(WarpLimits.highest(WarpActions.granted(online)));
        }
        return null;
    }
}
