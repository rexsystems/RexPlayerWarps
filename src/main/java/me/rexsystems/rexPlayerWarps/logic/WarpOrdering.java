package me.rexsystems.rexPlayerWarps.logic;

import me.rexsystems.rexPlayerWarps.model.SortableWarp;
import me.rexsystems.rexPlayerWarps.model.WarpSortKey;

import java.util.Comparator;

public final class WarpOrdering {

    private WarpOrdering() {
    }

    public static Comparator<SortableWarp> comparator(WarpSortKey key) {
        WarpSortKey selected = key == null ? WarpSortKey.NAME : key;
        Comparator<SortableWarp> primary = switch (selected) {
            case NAME -> Comparator.comparing(SortableWarp::name, String.CASE_INSENSITIVE_ORDER);
            case OWNER -> Comparator.comparing(SortableWarp::ownerName, String.CASE_INSENSITIVE_ORDER);
            case RATING -> Comparator.comparingDouble(SortableWarp::rating).reversed();
            case VISITS -> Comparator.comparingInt(SortableWarp::visits).reversed();
            case NEWEST -> Comparator.comparingLong(SortableWarp::createdAt).reversed();
            case OLDEST -> Comparator.comparingLong(SortableWarp::createdAt);
            case PRICE -> Comparator.comparingLong(SortableWarp::priceCents);
            case FAVORITES -> Comparator.comparingInt(SortableWarp::favorites).reversed();
            case CATEGORY -> Comparator.comparing(SortableWarp::category, String.CASE_INSENSITIVE_ORDER);
        };
        return primary.thenComparing(SortableWarp::name, String.CASE_INSENSITIVE_ORDER).thenComparingLong(SortableWarp::id);
    }
}
