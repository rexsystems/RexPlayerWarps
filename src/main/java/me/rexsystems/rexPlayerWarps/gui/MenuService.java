package me.rexsystems.rexPlayerWarps.gui;

import me.rexsystems.rexPlayerWarps.RexPlayerWarps;
import me.rexsystems.rexPlayerWarps.config.PluginConfig;
import me.rexsystems.rexPlayerWarps.logic.WarpOrdering;
import me.rexsystems.rexPlayerWarps.model.SortableWarp;
import me.rexsystems.rexPlayerWarps.model.WarpRecord;
import me.rexsystems.rexPlayerWarps.model.WarpSortKey;
import me.rexsystems.rexPlayerWarps.service.WarpRules;
import me.rexsystems.rexPlayerWarps.util.MessageService;
import me.rexsystems.rexPlayerWarps.util.SchedulerUtils;
import me.rexsystems.rexPlayerWarps.util.PapiBridge;
import me.rexsystems.rexPlayerWarps.util.TextParser;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class MenuService {

    private final RexPlayerWarps plugin;
    private final Map<UUID, BrowserState> states = new HashMap<>();

    public MenuService(RexPlayerWarps plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        open(player, null);
    }

    public void open(Player player, String search) {
        if (!player.hasPermission("rexplayerwarps.use")) {
            plugin.texts().send(player, "no-permission");
            return;
        }
        if (!plugin.cache().ready()) {
            plugin.texts().send(player, "storage-starting");
            return;
        }
        BrowserState state = states.computeIfAbsent(player.getUniqueId(), ignored -> new BrowserState());
        if (search != null) {
            state.search = search;
            state.page = 0;
            state.typing = false;
        }
        List<WarpSortKey> sorts = plugin.settings().sorts();
        if (state.sortIndex >= sorts.size()) {
            state.sortIndex = 0;
        }
        WarpSortKey sort = sorts.get(state.sortIndex);
        String category = selectedCategory(state);
        List<SortableWarp> rows = new ArrayList<>();
        Set<Long> recent = state.recent ? Set.copyOf(plugin.cache().recentIds(player.getUniqueId(), plugin.settings().recentSize())) : Set.of();
        for (WarpRecord warp : plugin.cache().all()) {
            if (!WarpRules.canSee(player, warp, plugin.cache(), plugin.settings())) {
                continue;
            }
            if (state.favoritesOnly && !plugin.cache().favorite(warp.id(), player.getUniqueId())) {
                continue;
            }
            if (state.recent && !recent.contains(warp.id())) {
                continue;
            }
            if (!category.isEmpty() && !warp.category().equalsIgnoreCase(category)) {
                continue;
            }
            if (!matches(warp, state.search)) {
                continue;
            }
            rows.add(new SortableWarp(
                    warp.id(),
                    warp.name(),
                    warp.ownerName(),
                    warp.category(),
                    warp.priceCents(),
                    plugin.cache().rating(warp.id()),
                    warp.visits(),
                    plugin.cache().favoriteCount(warp.id()),
                    warp.createdAt()
            ));
        }
        rows.sort(WarpOrdering.comparator(sort));
        int size = plugin.settings().guiRows() * 9;
        List<Integer> slots = new ArrayList<>(plugin.settings().warpSlots(size));
        Set<Integer> reserved = new HashSet<>();
        for (PluginConfig.ButtonSpec button : plugin.settings().buttons().values()) {
            if (button.slot() >= 0 && button.slot() < size) {
                reserved.add(button.slot());
            }
        }
        slots.removeIf(reserved::contains);
        int pageSize = Math.max(1, slots.size());
        int pages = Math.max(1, (int) Math.ceil(rows.size() / (double) pageSize));
        if (state.page >= pages) {
            state.page = pages - 1;
        }
        if (state.page < 0) {
            state.page = 0;
        }
        Map<String, String> view = viewMap(player, state, sort, category, pages);
        WarpMenuHolder holder = new WarpMenuHolder();
        Inventory inventory = Bukkit.createInventory(holder, size, MessageService.component(player, plugin.settings().guiTitle(), view));
        holder.inventory(inventory);
        PluginConfig.ItemSpec filler = plugin.settings().filler();
        Material fillerMaterial = material(filler.material(), null);
        if (fillerMaterial != null) {
            ItemStack fillerItem = item(player, fillerMaterial, filler.name(), filler.lore(), filler.modelData(), filler.glow(), view, null);
            for (int slot = 0; slot < size; slot++) {
                inventory.setItem(slot, fillerItem.clone());
            }
        }
        for (PluginConfig.ButtonSpec button : plugin.settings().buttons().values()) {
            if (button.slot() < 0 || button.slot() >= size) {
                continue;
            }
            Material buttonMaterial = material(button.material(), Material.STONE);
            if ("category".equals(button.id()) && !category.isEmpty()) {
                Material categoryIcon = material(plugin.categories().icon(category), buttonMaterial);
                if (categoryIcon != null) {
                    buttonMaterial = categoryIcon;
                }
            }
            inventory.setItem(button.slot(), item(player, buttonMaterial, button.name(), button.lore(), button.modelData(), button.glow(), view, null));
            holder.button(button.slot(), button.id());
        }
        if (!slots.isEmpty()) {
            int from = state.page * pageSize;
            int to = Math.min(rows.size(), from + pageSize);
            for (int index = from; index < to; index++) {
                SortableWarp row = rows.get(index);
                WarpRecord warp = plugin.cache().byId(row.id());
                if (warp == null) {
                    continue;
                }
                int slot = slots.get(index - from);
                boolean favorite = plugin.cache().favorite(warp.id(), player.getUniqueId());
                Map<String, String> map = warpMap(player, warp, favorite);
                String favoriteLine = plugin.settings().text(favorite ? "messages.lore-favorite-yes" : "messages.lore-favorite-no");
                Material icon = material(warp.icon(), Material.ENDER_PEARL);
                boolean glow = favorite && plugin.settings().favoriteGlow();
                inventory.setItem(slot, item(player, icon, plugin.settings().text("messages.warp-name"),
                        plugin.settings().textList("messages.warp-lore"), 0, glow, map, favoriteLine));
                holder.warp(slot, warp.id());
            }
        }
        player.openInventory(inventory);
    }

    public void click(Player player, WarpMenuHolder holder, int slot, ClickType click) {
        BrowserState state = states.computeIfAbsent(player.getUniqueId(), ignored -> new BrowserState());
        String button = holder.button(slot);
        if (button != null) {
            plugin.texts().sound(player, "click");
            switch (button) {
                case "previous" -> state.page--;
                case "next" -> state.page++;
                case "sort" -> {
                    state.sortIndex++;
                    state.page = 0;
                }
                case "search" -> {
                    state.typing = true;
                    player.closeInventory();
                    plugin.texts().send(player, "search-prompt", Map.of("cancel", plugin.settings().searchCancel()));
                    return;
                }
                case "recent" -> {
                    state.recent = !state.recent;
                    state.page = 0;
                }
                case "favorites" -> {
                    state.favoritesOnly = !state.favoritesOnly;
                    state.page = 0;
                }
                case "category" -> {
                    state.categoryIndex++;
                    state.page = 0;
                }
                case "close" -> {
                    player.closeInventory();
                    return;
                }
                default -> {
                    return;
                }
            }
            SchedulerUtils.runForPlayerLater(plugin, player, () -> open(player), 1L);
            return;
        }
        Long warpId = holder.warp(slot);
        if (warpId == null) {
            return;
        }
        WarpRecord warp = plugin.cache().byId(warpId);
        if (warp == null) {
            plugin.texts().send(player, "warp-removed");
            open(player);
            return;
        }
        if (matchesClick(plugin.settings().clickFavorite(), click)) {
            plugin.actions().favorite(player, warp, () -> {
                if (player.isOnline() && player.getOpenInventory().getTopInventory().getHolder() instanceof WarpMenuHolder) {
                    open(player);
                }
            });
            return;
        }
        if (matchesClick(plugin.settings().clickTeleport(), click)) {
            plugin.teleports().request(player, warp);
        }
    }

    public void acceptSearch(Player player, String text) {
        BrowserState state = states.computeIfAbsent(player.getUniqueId(), ignored -> new BrowserState());
        state.typing = false;
        String word = text == null ? "" : text.trim();
        if (word.equalsIgnoreCase(plugin.settings().searchCancel())) {
            state.search = "";
            state.page = 0;
            plugin.texts().send(player, "search-cleared");
            open(player);
            return;
        }
        if (word.length() > plugin.settings().searchLength()) {
            plugin.texts().send(player, "search-long", Map.of("max", Integer.toString(plugin.settings().searchLength())));
            return;
        }
        state.search = word;
        state.page = 0;
        plugin.texts().send(player, "search-set", Map.of("search", word));
        open(player);
    }

    public boolean typing(UUID playerId) {
        BrowserState state = states.get(playerId);
        return state != null && state.typing;
    }

    public void stopTyping(UUID playerId) {
        BrowserState state = states.get(playerId);
        if (state != null) {
            state.typing = false;
        }
    }

    public void forget(UUID playerId) {
        states.remove(playerId);
    }

    private String selectedCategory(BrowserState state) {
        List<String> ids = plugin.categories().ids();
        int span = ids.size() + 1;
        state.categoryIndex = Math.floorMod(state.categoryIndex, span);
        if (state.categoryIndex == 0) {
            return "";
        }
        return ids.get(state.categoryIndex - 1);
    }

    private Map<String, String> viewMap(Player player, BrowserState state, WarpSortKey sort, String category, int pages) {
        Map<String, String> map = new HashMap<>();
        map.put("page", Integer.toString(state.page + 1));
        map.put("pages", Integer.toString(pages));
        map.put("sort", plugin.settings().sortLabel(sort));
        map.put("search", state.search.isBlank() ? plugin.settings().text("messages.search-all") : state.search);
        map.put("category", category.isEmpty()
                ? plugin.settings().text("messages.category-all")
                : net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
                .serialize(TextParser.parse(plugin.categories().display(category))));
        map.put("player", player.getName());
        return map;
    }

    private Map<String, String> warpMap(Player player, WarpRecord warp, boolean favorite) {
        Map<String, String> map = new HashMap<>();
        int ratings = plugin.cache().ratingCount(warp.id());
        map.put("warp", warp.name());
        map.put("owner", warp.ownerName());
        map.put("description", warp.description());
        map.put("category", warp.category());
        map.put("price", plugin.texts().money(warp.priceCents()));
        map.put("rating", ratings == 0 ? plugin.settings().text("messages.rating-empty")
                : String.format(Locale.US, "%.1f", plugin.cache().rating(warp.id())));
        map.put("visits", Integer.toString(warp.visits()));
        map.put("favorites", Integer.toString(plugin.cache().favoriteCount(warp.id())));
        map.put("access", warp.access().label());
        map.put("player", player.getName());
        map.put("favorite", favorite ? "yes" : "no");
        return map;
    }

    private ItemStack item(Player player, Material material, String name, List<String> lore, int model, boolean glow,
                           Map<String, String> map, String favoriteLine) {
        ItemStack stack = new ItemStack(material == null ? Material.ENDER_PEARL : material);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(MessageService.component(player, name == null ? "" : name, map));
        List<Component> lines = new ArrayList<>();
        if (lore != null) {
            for (String line : lore) {
                if (line == null || line.isEmpty()) {
                    continue;
                }
                lines.add(line(player, line, map, favoriteLine));
            }
        }
        meta.lore(lines);
        if (model > 0) {
            meta.setCustomModelData(model);
        }
        if (glow) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        stack.setItemMeta(meta);
        return stack;
    }

    private Component line(Player player, String template, Map<String, String> map, String favoriteLine) {
        String rawFavorite = favoriteLine == null ? "" : favoriteLine;
        String marked = template.replace("{favorite}", "\u0000").replace("%favorite%", "\u0000");
        String applied = TextParser.apply(marked, map);
        if (player != null) {
            applied = PapiBridge.apply(player, applied);
        }
        applied = applied.replace("\u0000", rawFavorite);
        return TextParser.parse(applied);
    }

    private static boolean matches(WarpRecord warp, String search) {
        if (search == null || search.isBlank()) {
            return true;
        }
        String query = search.toLowerCase(Locale.ROOT);
        return warp.name().toLowerCase(Locale.ROOT).contains(query)
                || warp.ownerName().toLowerCase(Locale.ROOT).contains(query);
    }

    private static boolean matchesClick(String configured, ClickType type) {
        if (configured == null || type == null) {
            return false;
        }
        String expected = configured.trim().toUpperCase(Locale.ROOT);
        String actual = type.name();
        return actual.equals(expected) || actual.endsWith("_" + expected);
    }

    private static Material material(String name, Material fallback) {
        Material material = Material.matchMaterial(name == null ? "" : name.trim());
        if (material == null || !material.isItem() || material.isAir()) {
            return fallback;
        }
        return material;
    }
}
