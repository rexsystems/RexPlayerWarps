package me.rexsystems.rexPlayerWarps.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public enum WarpSortKey {
    NAME,
    OWNER,
    RATING,
    VISITS,
    NEWEST,
    OLDEST,
    PRICE,
    FAVORITES,
    CATEGORY;

    public static WarpSortKey from(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    public static List<WarpSortKey> parseList(List<String> raw) {
        List<WarpSortKey> out = new ArrayList<>();
        if (raw != null) {
            for (String entry : raw) {
                WarpSortKey key = from(entry);
                if (key != null && !out.contains(key)) {
                    out.add(key);
                }
            }
        }
        if (out.isEmpty()) {
            out.add(NAME);
        }
        return List.copyOf(out);
    }

    public String label() {
        String lower = name().toLowerCase(Locale.ROOT);
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
}
