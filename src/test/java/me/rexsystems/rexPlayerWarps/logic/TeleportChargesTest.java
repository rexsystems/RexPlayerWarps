package me.rexsystems.rexPlayerWarps.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TeleportChargesTest {

    @Test
    void refundsWhenTeleportFlagIsFalse() {
        assertTrue(TeleportCharges.shouldRefund(false, true));
        assertFalse(TeleportCharges.shouldRefund(true, true));
        assertFalse(TeleportCharges.shouldRefund(false, false));
    }
}
