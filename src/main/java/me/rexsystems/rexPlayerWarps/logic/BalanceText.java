package me.rexsystems.rexPlayerWarps.logic;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class BalanceText {

    private static final Pattern NUMBER = Pattern.compile("-?\\d+(?:\\.\\d+)?");

    private BalanceText() {
    }

    public static Double parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String cleaned = raw.replaceAll("<[^>]+>", "").replace(",", "").trim();
        Matcher matcher = NUMBER.matcher(cleaned);
        if (!matcher.find()) {
            return null;
        }
        try {
            return Double.parseDouble(matcher.group());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    public static String command(String template, String player, String amount) {
        String value = template == null ? "" : template;
        return value.replace("{player}", player == null ? "" : player).replace("{amount}", amount == null ? "0" : amount);
    }

    public static String plain(String name) {
        return name == null ? "" : name.toLowerCase(Locale.ROOT);
    }
}
