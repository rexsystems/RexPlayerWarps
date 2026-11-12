package me.rexsystems.rexPlayerWarps.model;

import java.util.Locale;

public enum WarpAccess {
    PUBLIC,
    PRIVATE,
    WHITELIST;

    public static WarpAccess parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    public String label() {
        String lower = name().toLowerCase(Locale.ROOT);
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
}
