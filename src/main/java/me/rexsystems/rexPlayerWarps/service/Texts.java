package me.rexsystems.rexPlayerWarps.service;

import me.rexsystems.rexPlayerWarps.RexPlayerWarps;
import me.rexsystems.rexPlayerWarps.logic.Cents;
import me.rexsystems.rexPlayerWarps.util.MessageService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Texts {

    private final RexPlayerWarps plugin;

    public Texts(RexPlayerWarps plugin) {
        this.plugin = plugin;
    }

    public void send(CommandSender sender, String key) {
        send(sender, key, Map.of());
    }

    public void send(CommandSender sender, String key, Map<String, String> extra) {
        String raw = plugin.settings().text("messages." + key);
        MessageService.send(sender, raw, placeholders(sender, extra));
    }

    public void help(CommandSender sender) {
        List<String> lines = plugin.settings().textList("messages.help");
        Map<String, String> map = placeholders(sender, Map.of());
        for (String line : lines) {
            MessageService.send(sender, line, map);
        }
    }

    public void sound(Player player, String key) {
        var sound = plugin.settings().sound(key);
        MessageService.sound(player, sound.sound(), sound.volume(), sound.pitch());
    }

    public void title(Player player, String key, Map<String, String> extra) {
        var title = plugin.settings().title(key);
        if (!title.enabled()) {
            return;
        }
        MessageService.title(player, title.title(), title.subtitle(), title.fadeIn(), title.stay(), title.fadeOut(),
                placeholders(player, extra));
    }

    public Map<String, String> placeholders(CommandSender sender, Map<String, String> extra) {
        Map<String, String> map = new HashMap<>();
        map.put("prefix", plugin.settings().text("messages.prefix"));
        if (sender instanceof Player player) {
            map.put("player", player.getName());
        }
        if (extra != null) {
            map.putAll(extra);
        }
        return map;
    }

    public String money(long cents) {
        return Cents.format(cents, plugin.settings().numberFormat());
    }
}
