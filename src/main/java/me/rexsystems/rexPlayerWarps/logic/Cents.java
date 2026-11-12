package me.rexsystems.rexPlayerWarps.logic;

import java.util.Locale;

public final class Cents {

    private Cents() {
    }

    public static long from(double amount) {
        if (Double.isNaN(amount) || Double.isInfinite(amount)) {
            return 0L;
        }
        return Math.round(amount * 100.0);
    }

    public static String format(long cents) {
        boolean negative = cents < 0;
        long abs = Math.abs(cents);
        String text = (abs / 100) + "." + String.format(Locale.US, "%02d", abs % 100);
        return negative ? "-" + text : text;
    }

    public static String format(long cents, String style) {
        if (style != null && style.equalsIgnoreCase("short")) {
            return shortFormat(cents);
        }
        return format(cents);
    }

    private static String shortFormat(long cents) {
        double amount = cents / 100.0;
        double abs = Math.abs(amount);
        String suffix = "";
        double scaled = abs;
        if (abs >= 1_000_000_000d) {
            scaled = abs / 1_000_000_000d;
            suffix = "B";
        } else if (abs >= 1_000_000d) {
            scaled = abs / 1_000_000d;
            suffix = "M";
        } else if (abs >= 1_000d) {
            scaled = abs / 1_000d;
            suffix = "K";
        } else {
            return format(cents);
        }
        String body = String.format(Locale.US, "%.1f", scaled);
        if (body.endsWith(".0")) {
            body = body.substring(0, body.length() - 2);
        }
        return (amount < 0 ? "-" : "") + body + suffix;
    }
}
