package me.rexsystems.rexPlayerWarps.model;

import java.util.Locale;
import java.util.UUID;

public final class WhitelistEntry {

    private final UUID playerId;
    private final String name;

    public WhitelistEntry(UUID playerId, String name) {
        this.playerId = playerId;
        this.name = name == null ? "" : name;
    }

    public UUID playerId() {
        return playerId;
    }

    public String name() {
        return name;
    }

    public String nameKey() {
        return name.toLowerCase(Locale.ROOT);
    }
}
