package me.rexsystems.rexPlayerWarps.logic;

import me.rexsystems.rexPlayerWarps.model.SortableWarp;
import me.rexsystems.rexPlayerWarps.model.WarpSortKey;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WarpOrderingTest {

    @Test
    void ratingSortPutsTheHigherScoreFirst() {
        List<SortableWarp> warps = new ArrayList<>();
        warps.add(warp(1, "Apple", 2.0));
        warps.add(warp(2, "Mango", 4.5));
        warps.sort(WarpOrdering.comparator(WarpSortKey.RATING));
        assertEquals("Mango", warps.get(0).name());
        assertEquals("Apple", warps.get(1).name());
    }

    @Test
    void nameSortIsAlphabetical() {
        List<SortableWarp> warps = new ArrayList<>();
        warps.add(warp(1, "Mango", 5.0));
        warps.add(warp(2, "apple", 1.0));
        warps.sort(WarpOrdering.comparator(WarpSortKey.NAME));
        assertEquals("apple", warps.get(0).name());
        assertEquals("Mango", warps.get(1).name());
    }

    private static SortableWarp warp(long id, String name, double rating) {
        return new SortableWarp(id, name, "Owner", "general", 0, rating, 0, 0, 0);
    }
}
