package me.rexsystems.rexPlayerWarps.command;

import me.rexsystems.rexPlayerWarps.RexPlayerWarps;
import me.rexsystems.rexPlayerWarps.model.WarpRecord;
import me.rexsystems.rexPlayerWarps.model.WhitelistEntry;
import me.rexsystems.rexPlayerWarps.service.WarpRules;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class WarpCommand implements CommandExecutor, TabCompleter {

    private static final List<String> FIELDS = List.of("description", "icon", "category", "price", "access");
    private static final List<String> ACCESS = List.of("public", "private", "whitelist");
    private static final List<String> SCORES = List.of("1", "2", "3", "4", "5");
    private static final List<String> WHITELIST = List.of("add", "remove", "list");

    private final RexPlayerWarps plugin;

    public WarpCommand(RexPlayerWarps plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            plugin.texts().help(sender);
            return true;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        if (sub.equals("reload")) {
            if (!sender.hasPermission("rexplayerwarps.reload")) {
                plugin.texts().send(sender, "no-permission");
                return true;
            }
            plugin.reloadPlugin();
            plugin.texts().send(sender, "reloaded");
            return true;
        }
        if (!(sender instanceof Player player)) {
            plugin.texts().send(sender, "players-only");
            return true;
        }
        switch (sub) {
            case "create" -> {
                if (args.length < 2) {
                    plugin.texts().send(player, "usage");
                    return true;
                }
                plugin.actions().create(player, args[1]);
            }
            case "delete" -> {
                if (args.length < 2) {
                    plugin.texts().send(player, "usage");
                    return true;
                }
                plugin.actions().delete(player, args[1]);
            }
            case "set" -> set(player, args);
            case "list" -> plugin.actions().list(player);
            case "open" -> plugin.menus().open(player, args.length >= 2 ? join(args, 1) : null);
            case "favorite" -> {
                if (args.length < 2) {
                    plugin.texts().send(player, "usage");
                    return true;
                }
                plugin.actions().favorite(player, args[1]);
            }
            case "rate" -> {
                if (args.length < 3) {
                    plugin.texts().send(player, "usage");
                    return true;
                }
                int score;
                try {
                    score = Integer.parseInt(args[2]);
                } catch (NumberFormatException failure) {
                    plugin.texts().send(player, "rate-range");
                    return true;
                }
                plugin.actions().rate(player, args[1], score);
            }
            case "collect" -> {
                if (args.length < 2) {
                    plugin.texts().send(player, "usage");
                    return true;
                }
                plugin.actions().collect(player, args[1]);
            }
            case "whitelist" -> whitelist(player, args);
            default -> plugin.texts().send(player, "usage");
        }
        return true;
    }

    private void set(Player player, String[] args) {
        if (args.length < 3) {
            plugin.texts().send(player, "field-unknown");
            return;
        }
        String field = args[2].toLowerCase(Locale.ROOT);
        String value = join(args, 3);
        if (value.isBlank() && !"description".equals(field)) {
            plugin.texts().send(player, "usage");
            return;
        }
        switch (field) {
            case "description" -> plugin.actions().setDescription(player, args[1], value);
            case "icon" -> plugin.actions().setIcon(player, args[1], value);
            case "category" -> plugin.actions().setCategory(player, args[1], value);
            case "price" -> plugin.actions().setPrice(player, args[1], value);
            case "access" -> plugin.actions().setAccess(player, args[1], value);
            default -> plugin.texts().send(player, "field-unknown");
        }
    }

    private void whitelist(Player player, String[] args) {
        if (args.length < 3) {
            plugin.texts().send(player, "whitelist-usage");
            return;
        }
        String action = args[2].toLowerCase(Locale.ROOT);
        switch (action) {
            case "add" -> {
                if (args.length < 4) {
                    plugin.texts().send(player, "whitelist-usage");
                    return;
                }
                plugin.actions().whitelistAdd(player, args[1], args[3]);
            }
            case "remove" -> {
                if (args.length < 4) {
                    plugin.texts().send(player, "whitelist-usage");
                    return;
                }
                plugin.actions().whitelistRemove(player, args[1], args[3]);
            }
            case "list" -> plugin.actions().whitelistList(player, args[1]);
            default -> plugin.texts().send(player, "whitelist-usage");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filter(args[0], subcommands(sender));
        }
        if (!(sender instanceof Player player) || !plugin.cache().ready()) {
            return List.of();
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        if (args.length == 2) {
            return switch (sub) {
                case "delete", "set", "collect", "whitelist" -> filter(args[1], editable(player));
                case "favorite", "rate", "open" -> filter(args[1], visible(player));
                default -> List.of();
            };
        }
        if (args.length == 3 && sub.equals("set")) {
            return filter(args[2], FIELDS);
        }
        if (args.length == 3 && sub.equals("rate")) {
            return filter(args[2], SCORES);
        }
        if (args.length == 3 && sub.equals("whitelist")) {
            return filter(args[2], WHITELIST);
        }
        if (args.length == 4 && sub.equals("set")) {
            return switch (args[2].toLowerCase(Locale.ROOT)) {
                case "category" -> filter(args[3], plugin.categories().ids());
                case "access" -> filter(args[3], ACCESS);
                default -> List.of();
            };
        }
        if (args.length == 4 && sub.equals("whitelist")) {
            String action = args[2].toLowerCase(Locale.ROOT);
            if (action.equals("add")) {
                List<String> names = new ArrayList<>();
                Bukkit.getOnlinePlayers().forEach(online -> names.add(online.getName()));
                return filter(args[3], names);
            }
            if (action.equals("remove")) {
                WarpRecord warp = plugin.cache().byName(args[1]);
                if (warp == null) {
                    return List.of();
                }
                List<String> names = new ArrayList<>();
                for (WhitelistEntry entry : plugin.cache().whitelist(warp.id())) {
                    names.add(entry.name());
                }
                return filter(args[3], names);
            }
        }
        return List.of();
    }

    private List<String> subcommands(CommandSender sender) {
        List<String> names = new ArrayList<>(List.of("help", "create", "delete", "set", "list", "open", "favorite", "rate", "collect", "whitelist"));
        if (sender.hasPermission("rexplayerwarps.reload")) {
            names.add("reload");
        }
        return names;
    }

    private List<String> editable(Player player) {
        List<String> names = new ArrayList<>();
        boolean admin = player.hasPermission("rexplayerwarps.admin");
        for (WarpRecord warp : plugin.cache().all()) {
            if (admin || warp.ownerId().equals(player.getUniqueId())) {
                names.add(warp.name());
            }
        }
        return names;
    }

    private List<String> visible(Player player) {
        List<String> names = new ArrayList<>();
        for (WarpRecord warp : plugin.cache().all()) {
            if (WarpRules.canSee(player, warp, plugin.cache(), plugin.settings())) {
                names.add(warp.name());
            }
        }
        return names;
    }

    private static List<String> filter(String token, List<String> options) {
        String lower = token == null ? "" : token.toLowerCase(Locale.ROOT);
        List<String> matches = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase(Locale.ROOT).startsWith(lower)) {
                matches.add(option);
            }
        }
        return matches;
    }

    private static String join(String[] args, int from) {
        StringBuilder builder = new StringBuilder();
        for (int index = from; index < args.length; index++) {
            if (index > from) {
                builder.append(' ');
            }
            builder.append(args[index]);
        }
        return builder.toString();
    }
}
