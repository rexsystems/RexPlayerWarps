package me.rexsystems.rexPlayerWarps.logic;

public final class TeleportCharges {

    private TeleportCharges() {
    }

    public static boolean shouldRefund(boolean teleported, boolean charged) {
        return charged && !teleported;
    }
}
