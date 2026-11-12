package me.rexsystems.rexPlayerWarps.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.Locale;
import java.util.Map;

public final class MessageService {

    private MessageService() {
    }

    public static Component parse(String input) {
        return TextParser.parse(input);
    }

    public static Component parse(String input, Map<String, String> placeholders) {
        return TextParser.parse(TextParser.apply(input, placeholders));
    }

    public static Component component(CommandSender sender, String input, Map<String, String> placeholders) {
        String applied = TextParser.apply(input, placeholders);
        if (sender instanceof Player player) {
            applied = PapiBridge.apply(player, applied);
        }
        return TextParser.parse(applied);
    }

    public static void send(CommandSender sender, String message) {
        send(sender, message, Map.of());
    }

    public static void send(CommandSender sender, String message, Map<String, String> placeholders) {
        if (sender == null || message == null || message.isBlank()) {
            return;
        }
        sender.sendMessage(component(sender, message, placeholders));
    }

    public static void actionBar(Player player, String message, Map<String, String> placeholders) {
        if (player == null || message == null || message.isBlank()) {
            return;
        }
        player.sendActionBar(component(player, message, placeholders));
    }

    public static void title(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut,
                             Map<String, String> placeholders) {
        if (player == null) {
            return;
        }
        Component titleComponent = component(player, title == null ? "" : title, placeholders);
        Component subtitleComponent = component(player, subtitle == null ? "" : subtitle, placeholders);
        player.showTitle(Title.title(
                titleComponent,
                subtitleComponent,
                Title.Times.times(ticks(fadeIn), ticks(stay), ticks(fadeOut))
        ));
    }

    public static void clearTitle(Player player) {
        if (player == null) {
            return;
        }
        player.clearTitle();
    }

    public static void sound(Player player, String name, float volume, float pitch) {
        if (player == null || name == null || name.isBlank()) {
            return;
        }
        String token = name.trim();
        try {
            Sound enumerated = Sound.valueOf(token.toUpperCase(Locale.ROOT).replace('.', '_').replace(':', '_'));
            player.playSound(player.getLocation(), enumerated, volume, pitch);
            return;
        } catch (IllegalArgumentException ignored) {
            // Fall through to the registry so namespaced keys from config still play.
        }
        String namespaced = token.contains(":") ? token.toLowerCase(Locale.ROOT) : "minecraft:" + token.toLowerCase(Locale.ROOT);
        NamespacedKey key = NamespacedKey.fromString(namespaced);
        if (key == null) {
            return;
        }
        Sound sound = Registry.SOUNDS.get(key);
        if (sound != null) {
            player.playSound(player.getLocation(), sound, volume, pitch);
        }
    }

    private static Duration ticks(int ticks) {
        return Duration.ofMillis(Math.max(0, ticks) * 50L);
    }
}
