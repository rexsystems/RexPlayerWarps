package me.rexsystems.rexPlayerWarps.storage;

import me.rexsystems.rexPlayerWarps.logic.RatingBook;
import me.rexsystems.rexPlayerWarps.model.WarpRecord;
import me.rexsystems.rexPlayerWarps.model.WhitelistEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class StoredSnapshot {

    private final List<WarpRecord> warps = new ArrayList<>();
    private final RatingBook ratings = new RatingBook();
    private final Map<Long, Set<UUID>> favorites = new HashMap<>();
    private final Map<Long, List<WhitelistEntry>> whitelist = new HashMap<>();
    private final Map<UUID, Map<Long, Long>> recent = new HashMap<>();

    public List<WarpRecord> warps() {
        return warps;
    }

    public RatingBook ratings() {
        return ratings;
    }

    public Map<Long, Set<UUID>> favorites() {
        return favorites;
    }

    public Map<Long, List<WhitelistEntry>> whitelist() {
        return whitelist;
    }

    public Map<UUID, Map<Long, Long>> recent() {
        return recent;
    }
}
