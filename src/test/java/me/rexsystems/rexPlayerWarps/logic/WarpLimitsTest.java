package me.rexsystems.rexPlayerWarps.logic;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WarpLimitsTest {

    @Test
    void highestWarpNodeWins() {
        int limit = WarpLimits.highest(List.of(
                "rexplayerwarps.use",
                "rexplayerwarps.warps.3",
                "rexplayerwarps.warps.10",
                "rexplayerwarps.warps.2"
        ));
        assertEquals(10, limit);
    }

    @Test
    void missingAndBrokenNodesDoNotCount() {
        assertEquals(0, WarpLimits.highest(List.of("rexplayerwarps.create")));
        assertEquals(4, WarpLimits.highest(List.of("RexPlayerWarps.Warps.4", "rexplayerwarps.warps.nope", "rexplayerwarps.warps.4.extra")));
    }
}
