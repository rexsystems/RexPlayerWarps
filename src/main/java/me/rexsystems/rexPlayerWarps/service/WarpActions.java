package me.rexsystems.rexPlayerWarps.service;

import me.rexsystems.rexPlayerWarps.RexPlayerWarps;
import me.rexsystems.rexPlayerWarps.logic.Cents;
import me.rexsystems.rexPlayerWarps.logic.WarpLimits;
import me.rexsystems.rexPlayerWarps.model.WarpAccess;
import me.rexsystems.rexPlayerWarps.model.WarpRecord;
import me.rexsystems.rexPlayerWarps.model.WhitelistEntry;
import me.rexsystems.rexPlayerWarps.storage.DuplicateWarpException;
import me.rexsystems.rexPlayerWarps.storage.WarpLimitException;
import me.rexsystems.rexPlayerWarps.util.SchedulerUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public final class WarpActions {

    private final RexPlayerWarps plugin;

    public WarpActions(RexPlayerWarps plugin) {
        this.plugin = plugin;
    }

    public static List<String> granted(Player player) {
        List<String> nodes = new ArrayList<>();
        for (PermissionAttachmentInfo info : player.getEffectivePermissions()) {
            if (info.getValue()) {
                nodes.add(info.getPermission());
            }
        }
        return nodes;
    }

    public void create(Player player, String name) {
        if (!ready(player)) {
            return;
        }
        if (!player.hasPermission("rexplayerwarps.create")) {
            plugin.texts().send(player, "no-permission");
            return;
        }
        if (!plugin.settings().worldAllowed(player.getWorld().getName())) {
            plugin.texts().send(player, "world-blocked", Map.of("warp", name == null ? "" : name));
            return;
        }
        int max = plugin.settings().nameLength();
        if (!WarpRules.validName(name, max)) {
            plugin.texts().send(player, "name-invalid", Map.of("min", "1", "max", Integer.toString(max)));
            return;
        }
        if (plugin.cache().byName(name) != null) {
            plugin.texts().send(player, "name-taken", Map.of("warp", name));
            return;
        }
        int limit = WarpLimits.highest(granted(player));
        int count = plugin.cache().countOwned(player.getUniqueId());
        if (count >= limit) {
            plugin.texts().send(player, "limit", Map.of("limit", Integer.toString(limit), "count", Integer.toString(count)));
            return;
        }
        String category = plugin.settings().defaultCategory();
        if (!plugin.categories().exists(category) && !plugin.categories().ids().isEmpty()) {
            category = plugin.categories().ids().get(0);
        }
        long price = Cents.from(plugin.settings().defaultPrice());
        long min = Cents.from(plugin.settings().priceMin());
        long maxPrice = Cents.from(plugin.settings().priceMax());
        if (price < min) {
            price = min;
        }
        if (price > maxPrice) {
            price = maxPrice;
        }
        String description = plugin.settings().defaultDescription();
        if (description.length() > plugin.settings().descriptionLength()) {
            description = description.substring(0, plugin.settings().descriptionLength());
        }
        WarpRecord draft = new WarpRecord.Builder()
                .name(name)
                .ownerId(player.getUniqueId())
                .ownerName(player.getName())
                .world(player.getWorld().getName())
                .x(player.getLocation().getX())
                .y(player.getLocation().getY())
                .z(player.getLocation().getZ())
                .yaw(player.getLocation().getYaw())
                .pitch(player.getLocation().getPitch())
                .description(description)
                .icon(plugin.settings().defaultIcon())
                .category(category)
                .priceCents(price)
                .access(plugin.settings().defaultAccess())
                .balanceCents(0)
                .visits(0)
                .createdAt(System.currentTimeMillis())
                .build();
        int allowed = limit;
        SchedulerUtils.runAsync(plugin, () -> {
            try {
                long id = plugin.storage().insert(draft, allowed);
                plugin.cache().put(draft.toBuilder().id(id).build());
                SchedulerUtils.runForPlayer(plugin, player, () -> plugin.texts().send(player, "created", Map.of("warp", name)));
            } catch (DuplicateWarpException duplicate) {
                SchedulerUtils.runForPlayer(plugin, player, () -> plugin.texts().send(player, "name-taken", Map.of("warp", name)));
            } catch (WarpLimitException limited) {
                SchedulerUtils.runForPlayer(plugin, player, () -> plugin.texts().send(player, "limit", Map.of(
                        "limit", Integer.toString(allowed),
                        "count", Integer.toString(plugin.cache().countOwned(player.getUniqueId()))
                )));
            } catch (Exception failure) {
                plugin.getLogger().warning("Could not create warp: " + failure.getMessage());
                SchedulerUtils.runForPlayer(plugin, player, () -> plugin.texts().send(player, "usage"));
            }
        });
    }

    public void delete(Player player, String name) {
        WarpRecord warp = editTarget(player, name);
        if (warp == null) {
            return;
        }
        boolean admin = player.hasPermission("rexplayerwarps.admin") && !warp.ownerId().equals(player.getUniqueId());
        if (warp.balanceCents() > 0 && !admin) {
            plugin.texts().send(player, "delete-balance", moneyMap(warp));
            return;
        }
        SchedulerUtils.runAsync(plugin, () -> {
            try {
                plugin.storage().delete(warp.id());
                plugin.cache().remove(warp.id());
                SchedulerUtils.runForPlayer(plugin, player, () -> plugin.texts().send(player, "deleted", Map.of("warp", warp.name())));
            } catch (Exception failure) {
                plugin.getLogger().warning("Could not delete warp: " + failure.getMessage());
            }
        });
    }

    public void setDescription(Player player, String name, String value) {
        WarpRecord warp = editTarget(player, name);
        if (warp == null) {
            return;
        }
        String next = value == null ? "" : value.trim();
        if (next.equalsIgnoreCase(plugin.settings().descriptionClear())) {
            next = "";
        }
        if (next.length() > plugin.settings().descriptionLength()) {
            plugin.texts().send(player, "description-long", Map.of("max", Integer.toString(plugin.settings().descriptionLength()), "warp", warp.name()));
            return;
        }
        save(player, warp.toBuilder().description(next).ownerName(playerName(player, warp)).build(), "description");
    }

    public void setIcon(Player player, String name, String icon) {
        WarpRecord warp = editTarget(player, name);
        if (warp == null) {
            return;
        }
        Material material = Material.matchMaterial(icon == null ? "" : icon.trim());
        if (material == null || !material.isItem()) {
            plugin.texts().send(player, "icon-invalid", Map.of("icon", icon == null ? "" : icon, "warp", warp.name()));
            return;
        }
        save(player, warp.toBuilder().icon(material.name()).ownerName(playerName(player, warp)).build(), "icon");
    }

    public void setCategory(Player player, String name, String category) {
        WarpRecord warp = editTarget(player, name);
        if (warp == null) {
            return;
        }
        if (category == null || !plugin.categories().exists(category)) {
            plugin.texts().send(player, "category-unknown", Map.of("category", category == null ? "" : category, "warp", warp.name()));
            return;
        }
        save(player, warp.toBuilder().category(category.toLowerCase(Locale.ROOT)).ownerName(playerName(player, warp)).build(), "category");
    }

    public void setPrice(Player player, String name, String raw) {
        WarpRecord warp = editTarget(player, name);
        if (warp == null) {
            return;
        }
        double parsed;
        try {
            parsed = Double.parseDouble(raw);
        } catch (NumberFormatException | NullPointerException failure) {
            plugin.texts().send(player, "price-invalid");
            return;
        }
        if (parsed < 0) {
            plugin.texts().send(player, "price-range", rangeMap());
            return;
        }
        long cents = Cents.from(parsed);
        long min = Cents.from(plugin.settings().priceMin());
        long max = Cents.from(plugin.settings().priceMax());
        if (cents < min || cents > max) {
            plugin.texts().send(player, "price-range", rangeMap());
            return;
        }
        save(player, warp.toBuilder().priceCents(cents).ownerName(playerName(player, warp)).build(), "price");
    }

    public void setAccess(Player player, String name, String raw) {
        WarpRecord warp = editTarget(player, name);
        if (warp == null) {
            return;
        }
        WarpAccess access = WarpAccess.parse(raw);
        if (access == null) {
            plugin.texts().send(player, "access-invalid");
            return;
        }
        save(player, warp.toBuilder().access(access).ownerName(playerName(player, warp)).build(), "access");
    }

    public void list(Player player) {
        if (!ready(player)) {
            return;
        }
        if (!player.hasPermission("rexplayerwarps.use")) {
            plugin.texts().send(player, "no-permission");
            return;
        }
        List<WarpRecord> owned = plugin.cache().owned(player.getUniqueId());
        int limit = WarpLimits.highest(granted(player));
        plugin.texts().send(player, "list-header", Map.of("count", Integer.toString(owned.size()), "limit", Integer.toString(limit)));
        if (owned.isEmpty()) {
            plugin.texts().send(player, "list-empty");
            return;
        }
        for (WarpRecord warp : owned) {
            plugin.texts().send(player, "list-line", moneyMap(warp));
        }
    }

    public void favorite(Player player, WarpRecord warp, Runnable after) {
        if (!ready(player)) {
            return;
        }
        if (!player.hasPermission("rexplayerwarps.use")) {
            plugin.texts().send(player, "no-permission");
            return;
        }
        if (warp == null || !WarpRules.canSee(player, warp, plugin.cache(), plugin.settings())) {
            plugin.texts().send(player, "not-found", Map.of("warp", warp == null ? "" : warp.name()));
            return;
        }
        boolean next = !plugin.cache().favorite(warp.id(), player.getUniqueId());
        SchedulerUtils.runAsync(plugin, () -> {
            try {
                plugin.storage().setFavorite(warp.id(), player.getUniqueId(), next);
                plugin.cache().setFavorite(warp.id(), player.getUniqueId(), next);
                SchedulerUtils.runForPlayer(plugin, player, () -> {
                    plugin.texts().send(player, next ? "favorite-on" : "favorite-off", Map.of("warp", warp.name()));
                    if (after != null) {
                        after.run();
                    }
                });
            } catch (Exception failure) {
                plugin.getLogger().warning("Could not update a favorite: " + failure.getMessage());
            }
        });
    }

    public void favorite(Player player, String name) {
        if (!ready(player)) {
            return;
        }
        WarpRecord warp = plugin.cache().byName(name);
        if (warp == null) {
            plugin.texts().send(player, "not-found", Map.of("warp", name == null ? "" : name));
            return;
        }
        favorite(player, warp, null);
    }

    public void rate(Player player, String name, int score) {
        if (!ready(player)) {
            return;
        }
        if (!player.hasPermission("rexplayerwarps.use")) {
            plugin.texts().send(player, "no-permission");
            return;
        }
        if (score < 1 || score > 5) {
            plugin.texts().send(player, "rate-range");
            return;
        }
        WarpRecord warp = plugin.cache().byName(name);
        if (warp == null || !WarpRules.canSee(player, warp, plugin.cache(), plugin.settings())) {
            plugin.texts().send(player, "not-found", Map.of("warp", name == null ? "" : name));
            return;
        }
        SchedulerUtils.runAsync(plugin, () -> {
            try {
                plugin.storage().rate(warp.id(), player.getUniqueId(), score);
                plugin.cache().rate(warp.id(), player.getUniqueId(), score);
                SchedulerUtils.runForPlayer(plugin, player, () -> plugin.texts().send(player, "rated", Map.of(
                        "warp", warp.name(),
                        "score", Integer.toString(score)
                )));
            } catch (Exception failure) {
                plugin.getLogger().warning("Could not save a rating: " + failure.getMessage());
            }
        });
    }

    public void collect(Player player, String name) {
        if (!ready(player)) {
            return;
        }
        if (!player.hasPermission("rexplayerwarps.use")) {
            plugin.texts().send(player, "no-permission");
            return;
        }
        WarpRecord warp = plugin.cache().byName(name);
        if (warp == null) {
            plugin.texts().send(player, "not-found", Map.of("warp", name == null ? "" : name));
            return;
        }
        if (!warp.ownerId().equals(player.getUniqueId())) {
            plugin.texts().send(player, "not-owner", Map.of("owner", warp.ownerName(), "warp", warp.name()));
            return;
        }
        SchedulerUtils.runAsync(plugin, () -> {
            try {
                long taken = plugin.storage().claimBalance(warp.id());
                if (taken <= 0) {
                    plugin.cache().setBalance(warp.id(), 0);
                    SchedulerUtils.runForPlayer(plugin, player, () -> plugin.texts().send(player, "collect-empty", Map.of("warp", warp.name())));
                    return;
                }
                plugin.cache().setBalance(warp.id(), 0);
                SchedulerUtils.runForPlayer(plugin, player, () -> plugin.economy().deposit(player, taken, paid -> {
                    if (paid) {
                        plugin.texts().send(player, "collected", Map.of("warp", warp.name(), "balance", plugin.texts().money(taken)));
                        plugin.texts().sound(player, "collect");
                        return;
                    }
                    SchedulerUtils.runAsync(plugin, () -> {
                        try {
                            plugin.storage().addBalance(warp.id(), taken);
                            WarpRecord current = plugin.cache().byId(warp.id());
                            long restored = (current == null ? 0 : current.balanceCents()) + taken;
                            plugin.cache().setBalance(warp.id(), restored);
                        } catch (Exception failure) {
                            plugin.getLogger().warning("Could not restore a warp bank: " + failure.getMessage());
                        }
                        SchedulerUtils.runForPlayer(plugin, player, () -> {
                            if (!plugin.economy().available()) {
                                plugin.texts().send(player, "economy-missing");
                            }
                            plugin.texts().send(player, "collect-failed", Map.of("warp", warp.name(), "balance", plugin.texts().money(taken)));
                        });
                    });
                }));
            } catch (Exception failure) {
                plugin.getLogger().warning("Could not collect a warp bank: " + failure.getMessage());
            }
        });
    }

    public void whitelistAdd(Player player, String name, String target) {
        WarpRecord warp = editTarget(player, name);
        if (warp == null) {
            return;
        }
        if (target == null || target.isBlank() || target.length() > 16) {
            plugin.texts().send(player, "whitelist-usage");
            return;
        }
        if (plugin.cache().whitelisted(warp.id(), null, target)) {
            plugin.texts().send(player, "whitelist-have", Map.of("warp", warp.name(), "player", target));
            return;
        }
        Player online = Bukkit.getPlayerExact(target);
        UUID uuid = online == null ? null : online.getUniqueId();
        String storedName = online == null ? target : online.getName();
        WhitelistEntry entry = new WhitelistEntry(uuid, storedName);
        SchedulerUtils.runAsync(plugin, () -> {
            try {
                plugin.storage().addWhitelist(warp.id(), entry);
                List<WhitelistEntry> entries = new ArrayList<>(plugin.cache().whitelist(warp.id()));
                entries.removeIf(existing -> existing.nameKey().equals(entry.nameKey()));
                entries.add(entry);
                plugin.cache().setWhitelist(warp.id(), entries);
                SchedulerUtils.runForPlayer(plugin, player, () -> plugin.texts().send(player, "whitelist-added", Map.of(
                        "warp", warp.name(),
                        "player", storedName
                )));
            } catch (Exception failure) {
                plugin.getLogger().warning("Could not update a whitelist: " + failure.getMessage());
            }
        });
    }

    public void whitelistRemove(Player player, String name, String target) {
        WarpRecord warp = editTarget(player, name);
        if (warp == null) {
            return;
        }
        if (target == null || target.isBlank()) {
            plugin.texts().send(player, "whitelist-usage");
            return;
        }
        String key = target.toLowerCase(Locale.ROOT);
        if (!plugin.cache().whitelisted(warp.id(), null, target)) {
            plugin.texts().send(player, "whitelist-missing", Map.of("warp", warp.name(), "player", target));
            return;
        }
        SchedulerUtils.runAsync(plugin, () -> {
            try {
                plugin.storage().removeWhitelist(warp.id(), key);
                List<WhitelistEntry> entries = new ArrayList<>(plugin.cache().whitelist(warp.id()));
                entries.removeIf(existing -> existing.nameKey().equals(key));
                plugin.cache().setWhitelist(warp.id(), entries);
                SchedulerUtils.runForPlayer(plugin, player, () -> plugin.texts().send(player, "whitelist-removed", Map.of(
                        "warp", warp.name(),
                        "player", target
                )));
            } catch (Exception failure) {
                plugin.getLogger().warning("Could not update a whitelist: " + failure.getMessage());
            }
        });
    }

    public void whitelistList(Player player, String name) {
        WarpRecord warp = editTarget(player, name);
        if (warp == null) {
            return;
        }
        List<WhitelistEntry> entries = plugin.cache().whitelist(warp.id());
        plugin.texts().send(player, "whitelist-header", Map.of("warp", warp.name()));
        if (entries.isEmpty()) {
            plugin.texts().send(player, "whitelist-empty");
            return;
        }
        for (WhitelistEntry entry : entries) {
            plugin.texts().send(player, "whitelist-line", Map.of("player", entry.name(), "warp", warp.name()));
        }
    }

    private void save(Player player, WarpRecord warp, String field) {
        SchedulerUtils.runAsync(plugin, () -> {
            try {
                plugin.storage().updateFields(warp);
                plugin.cache().replaceRecord(warp);
                SchedulerUtils.runForPlayer(plugin, player, () -> plugin.texts().send(player, "updated", Map.of(
                        "warp", warp.name(),
                        "field", field
                )));
            } catch (Exception failure) {
                plugin.getLogger().warning("Could not update a warp: " + failure.getMessage());
            }
        });
    }

    private WarpRecord editTarget(Player player, String name) {
        if (!ready(player)) {
            return null;
        }
        WarpRecord warp = plugin.cache().byName(name);
        if (warp == null) {
            plugin.texts().send(player, "not-found", Map.of("warp", name == null ? "" : name));
            return null;
        }
        if (!WarpRules.canEdit(player, warp)) {
            plugin.texts().send(player, "not-owner", Map.of("owner", warp.ownerName(), "warp", warp.name()));
            return null;
        }
        if (!player.hasPermission("rexplayerwarps.create") && !player.hasPermission("rexplayerwarps.admin")
                && !warp.ownerId().equals(player.getUniqueId())) {
            plugin.texts().send(player, "no-permission");
            return null;
        }
        return warp;
    }

    private boolean ready(Player player) {
        if (plugin.storage().open() && plugin.cache().ready()) {
            return true;
        }
        plugin.texts().send(player, "storage-starting");
        return false;
    }

    private String playerName(Player player, WarpRecord warp) {
        return warp.ownerId().equals(player.getUniqueId()) ? player.getName() : warp.ownerName();
    }

    private Map<String, String> moneyMap(WarpRecord warp) {
        Map<String, String> map = new HashMap<>();
        map.put("warp", warp.name());
        map.put("owner", warp.ownerName());
        map.put("price", plugin.texts().money(warp.priceCents()));
        map.put("balance", plugin.texts().money(warp.balanceCents()));
        map.put("visits", Integer.toString(warp.visits()));
        map.put("access", warp.access().label());
        map.put("category", warp.category());
        return map;
    }

    private Map<String, String> rangeMap() {
        return Map.of(
                "min", plugin.texts().money(Cents.from(plugin.settings().priceMin())),
                "max", plugin.texts().money(Cents.from(plugin.settings().priceMax()))
        );
    }
}
