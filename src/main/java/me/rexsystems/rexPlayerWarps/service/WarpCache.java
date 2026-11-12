package me.rexsystems.rexPlayerWarps.service;

import me.rexsystems.rexPlayerWarps.logic.RatingBook;
import me.rexsystems.rexPlayerWarps.model.WarpRecord;
import me.rexsystems.rexPlayerWarps.model.WhitelistEntry;
import me.rexsystems.rexPlayerWarps.storage.StoredSnapshot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class WarpCache {

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Map<Long, WarpRecord> byId = new HashMap<>();
    private final Map<String, Long> byName = new HashMap<>();
    private final RatingBook ratings = new RatingBook();
    private final Map<Long, Set<UUID>> favorites = new HashMap<>();
    private final Map<Long, List<WhitelistEntry>> whitelist = new HashMap<>();
    private final Map<UUID, Map<Long, Long>> recent = new HashMap<>();
    private volatile boolean ready;

    public boolean ready() {
        return ready;
    }

    public void replace(StoredSnapshot snapshot) {
        lock.writeLock().lock();
        try {
            byId.clear();
            byName.clear();
            ratings.clear();
            favorites.clear();
            whitelist.clear();
            recent.clear();
            for (WarpRecord warp : snapshot.warps()) {
                byId.put(warp.id(), warp);
                byName.put(warp.nameKey(), warp.id());
            }
            snapshot.ratings().copyInto(ratings);
            for (Map.Entry<Long, Set<UUID>> entry : snapshot.favorites().entrySet()) {
                favorites.put(entry.getKey(), new HashSet<>(entry.getValue()));
            }
            for (Map.Entry<Long, List<WhitelistEntry>> entry : snapshot.whitelist().entrySet()) {
                whitelist.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }
            for (Map.Entry<UUID, Map<Long, Long>> entry : snapshot.recent().entrySet()) {
                recent.put(entry.getKey(), new HashMap<>(entry.getValue()));
            }
            ready = true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void put(WarpRecord warp) {
        lock.writeLock().lock();
        try {
            byId.put(warp.id(), warp);
            byName.put(warp.nameKey(), warp.id());
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void replaceRecord(WarpRecord warp) {
        lock.writeLock().lock();
        try {
            WarpRecord previous = byId.get(warp.id());
            if (previous != null) {
                byName.remove(previous.nameKey());
            }
            byId.put(warp.id(), warp);
            byName.put(warp.nameKey(), warp.id());
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void remove(long id) {
        lock.writeLock().lock();
        try {
            WarpRecord previous = byId.remove(id);
            if (previous != null) {
                byName.remove(previous.nameKey());
            }
            ratings.removeWarp(id);
            favorites.remove(id);
            whitelist.remove(id);
            for (Map<Long, Long> visits : recent.values()) {
                visits.remove(id);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public WarpRecord byId(long id) {
        lock.readLock().lock();
        try {
            return byId.get(id);
        } finally {
            lock.readLock().unlock();
        }
    }

    public WarpRecord byName(String name) {
        if (name == null) {
            return null;
        }
        lock.readLock().lock();
        try {
            Long id = byName.get(name.toLowerCase(Locale.ROOT));
            return id == null ? null : byId.get(id);
        } finally {
            lock.readLock().unlock();
        }
    }

    public int countOwned(UUID owner) {
        lock.readLock().lock();
        try {
            int count = 0;
            for (WarpRecord warp : byId.values()) {
                if (owner.equals(warp.ownerId())) {
                    count++;
                }
            }
            return count;
        } finally {
            lock.readLock().unlock();
        }
    }

    public long bank(UUID owner) {
        lock.readLock().lock();
        try {
            long cents = 0;
            for (WarpRecord warp : byId.values()) {
                if (owner.equals(warp.ownerId())) {
                    cents += warp.balanceCents();
                }
            }
            return cents;
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<WarpRecord> all() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(byId.values());
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<WarpRecord> owned(UUID owner) {
        lock.readLock().lock();
        try {
            List<WarpRecord> owned = new ArrayList<>();
            for (WarpRecord warp : byId.values()) {
                if (owner.equals(warp.ownerId())) {
                    owned.add(warp);
                }
            }
            owned.sort(Comparator.comparing(WarpRecord::name, String.CASE_INSENSITIVE_ORDER));
            return owned;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void rate(long warpId, UUID playerId, int score) {
        lock.writeLock().lock();
        try {
            ratings.rate(warpId, playerId, score);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public double rating(long warpId) {
        lock.readLock().lock();
        try {
            return ratings.average(warpId);
        } finally {
            lock.readLock().unlock();
        }
    }

    public int ratingCount(long warpId) {
        lock.readLock().lock();
        try {
            return ratings.count(warpId);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setFavorite(long warpId, UUID playerId, boolean favorite) {
        lock.writeLock().lock();
        try {
            Set<UUID> players = favorites.computeIfAbsent(warpId, ignored -> new HashSet<>());
            if (favorite) {
                players.add(playerId);
            } else {
                players.remove(playerId);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean favorite(long warpId, UUID playerId) {
        lock.readLock().lock();
        try {
            Set<UUID> players = favorites.get(warpId);
            return players != null && players.contains(playerId);
        } finally {
            lock.readLock().unlock();
        }
    }

    public int favoriteCount(long warpId) {
        lock.readLock().lock();
        try {
            Set<UUID> players = favorites.get(warpId);
            return players == null ? 0 : players.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setWhitelist(long warpId, List<WhitelistEntry> entries) {
        lock.writeLock().lock();
        try {
            whitelist.put(warpId, new ArrayList<>(entries));
        } finally {
            lock.writeLock().unlock();
        }
    }

    public List<WhitelistEntry> whitelist(long warpId) {
        lock.readLock().lock();
        try {
            List<WhitelistEntry> entries = whitelist.get(warpId);
            return entries == null ? List.of() : List.copyOf(entries);
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean whitelisted(long warpId, UUID playerId, String name) {
        lock.readLock().lock();
        try {
            List<WhitelistEntry> entries = whitelist.get(warpId);
            if (entries == null) {
                return false;
            }
            String key = name == null ? "" : name.toLowerCase(Locale.ROOT);
            for (WhitelistEntry entry : entries) {
                if (playerId != null && playerId.equals(entry.playerId())) {
                    return true;
                }
                if (!key.isEmpty() && key.equals(entry.nameKey())) {
                    return true;
                }
            }
            return false;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void addVisit(long warpId, UUID playerId, long priceCents, long when) {
        lock.writeLock().lock();
        try {
            WarpRecord warp = byId.get(warpId);
            if (warp != null) {
                byId.put(warpId, warp.toBuilder()
                        .visits(warp.visits() + 1)
                        .balanceCents(warp.balanceCents() + priceCents)
                        .build());
            }
            recent.computeIfAbsent(playerId, ignored -> new HashMap<>()).put(warpId, when);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void setBalance(long warpId, long balanceCents) {
        lock.writeLock().lock();
        try {
            WarpRecord warp = byId.get(warpId);
            if (warp != null) {
                byId.put(warpId, warp.toBuilder().balanceCents(balanceCents).build());
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public List<Long> recentIds(UUID playerId, int limit) {
        lock.readLock().lock();
        try {
            Map<Long, Long> visits = recent.get(playerId);
            if (visits == null || visits.isEmpty()) {
                return List.of();
            }
            return visits.entrySet().stream()
                    .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                    .limit(Math.max(1, limit))
                    .map(Map.Entry::getKey)
                    .toList();
        } finally {
            lock.readLock().unlock();
        }
    }
}
