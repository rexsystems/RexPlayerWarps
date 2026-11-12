package me.rexsystems.rexPlayerWarps.logic;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class RatingBook {

    private final Map<Long, Map<UUID, Integer>> rows = new HashMap<>();

    public void rate(long warpId, UUID playerId, int score) {
        rows.computeIfAbsent(warpId, ignored -> new HashMap<>()).put(playerId, score);
    }

    public int score(long warpId, UUID playerId) {
        Map<UUID, Integer> scores = rows.get(warpId);
        if (scores == null) {
            return 0;
        }
        return scores.getOrDefault(playerId, 0);
    }

    public int rows() {
        int count = 0;
        for (Map<UUID, Integer> scores : rows.values()) {
            count += scores.size();
        }
        return count;
    }

    public int count(long warpId) {
        Map<UUID, Integer> scores = rows.get(warpId);
        return scores == null ? 0 : scores.size();
    }

    public double average(long warpId) {
        Map<UUID, Integer> scores = rows.get(warpId);
        if (scores == null || scores.isEmpty()) {
            return 0.0;
        }
        int sum = 0;
        for (int score : scores.values()) {
            sum += score;
        }
        return sum / (double) scores.size();
    }

    public void removeWarp(long warpId) {
        rows.remove(warpId);
    }

    public void clear() {
        rows.clear();
    }

    public void copyInto(RatingBook target) {
        for (Map.Entry<Long, Map<UUID, Integer>> warp : rows.entrySet()) {
            for (Map.Entry<UUID, Integer> score : warp.getValue().entrySet()) {
                target.rate(warp.getKey(), score.getKey(), score.getValue());
            }
        }
    }
}
