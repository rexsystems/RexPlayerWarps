package me.rexsystems.rexPlayerWarps.logic;

import java.util.Locale;
import java.util.Set;

public final class UnsafeFeet {

    private UnsafeFeet() {
    }

    public static boolean unsafe(Set<String> blocks, boolean checkVoid, double y, int minHeight, String feet, String below) {
        if (checkVoid && y < minHeight) {
            return true;
        }
        return listed(blocks, feet) || listed(blocks, below);
    }

    private static boolean listed(Set<String> blocks, String material) {
        if (material == null || blocks == null || material.isBlank()) {
            return false;
        }
        return blocks.contains(material.toUpperCase(Locale.ROOT));
    }
}
