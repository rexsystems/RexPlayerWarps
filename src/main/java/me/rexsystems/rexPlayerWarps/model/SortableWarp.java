package me.rexsystems.rexPlayerWarps.model;

public record SortableWarp(
        long id,
        String name,
        String ownerName,
        String category,
        long priceCents,
        double rating,
        int visits,
        int favorites,
        long createdAt
) {
}
