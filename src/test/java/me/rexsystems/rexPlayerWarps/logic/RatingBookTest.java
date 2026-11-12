package me.rexsystems.rexPlayerWarps.logic;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RatingBookTest {

    @Test
    void aSecondRateUpdatesTheSameRow() {
        RatingBook book = new RatingBook();
        UUID player = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID other = UUID.fromString("22222222-2222-2222-2222-222222222222");
        book.rate(7L, player, 5);
        book.rate(7L, player, 2);
        assertEquals(1, book.rows());
        assertEquals(2, book.score(7L, player));
        book.rate(7L, other, 4);
        assertEquals(2, book.rows());
        assertEquals(2, book.score(7L, player));
    }
}
