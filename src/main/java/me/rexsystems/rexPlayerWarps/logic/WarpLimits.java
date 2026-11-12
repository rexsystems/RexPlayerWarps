package me.rexsystems.rexPlayerWarps.logic;

import java.util.Collection;
import java.util.Locale;

public final class WarpLimits {

    public static final String PREFIX = "rexplayerwarps.warps.";

    private WarpLimits() {
    }

    public static int highest(Collection<String> nodes) {
        int best = 0;
        if (nodes == null) {
            return best;
        }
        for (String node : nodes) {
            if (node == null) {
                continue;
            }
            String trimmed = node.trim().toLowerCase(Locale.ROOT);
            if (!trimmed.startsWith(PREFIX)) {
                continue;
            }
            String tail = trimmed.substring(PREFIX.length());
            if (tail.isEmpty() || tail.indexOf('.') >= 0) {
                continue;
            }
            int value;
            try {
                value = Integer.parseInt(tail);
            } catch (NumberFormatException ignored) {
                continue;
            }
            if (value > best) {
                best = value;
            }
        }
        return best;
    }
}
