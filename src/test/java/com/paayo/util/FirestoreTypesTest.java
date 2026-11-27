/*
 * File:        FirestoreTypesTest.java
 * Module:      Testing
 * Purpose:     Unit tests for the Asia/Manila date conversions (NFR-16).
 * Responsible: Reuel Matthew Arcilla (QA)
 */
package com.paayo.util;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.cloud.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FirestoreTypesTest {
    private static final ZoneId MANILA = ZoneId.of("Asia/Manila");

    @Test
    @DisplayName("null date-times stay null in both directions")
    void nullsStayNull() {
        assertThat(FirestoreTypes.toTimestamp(null)).isNull();
        assertThat(FirestoreTypes.toLocalDateTime(null)).isNull();
    }

    @Test
    @DisplayName("08:00 in Manila (UTC+8) is 00:00 UTC")
    void manilaTimeIsConvertedToUtcInstant() {
        LocalDateTime manila = LocalDateTime.of(2026, 11, 25, 8, 0, 0);

        Timestamp ts = FirestoreTypes.toTimestamp(manila);

        assertThat(ts.getSeconds()).isEqualTo(Instant.parse("2026-11-25T00:00:00Z").getEpochSecond());
    }

    @Test
    @DisplayName("a timestamp is shown as Manila time")
    void timestampIsShownInManilaTime() {
        Timestamp ts = Timestamp.ofTimeSecondsAndNanos(Instant.parse("2026-11-25T16:30:00Z").getEpochSecond(), 0);

        // 16:30 UTC is 00:30 the next day in Manila
        assertThat(FirestoreTypes.toLocalDateTime(ts)).isEqualTo(LocalDateTime.of(2026, 11, 26, 0, 30, 0));
    }

    @Test
    @DisplayName("converting there and back keeps seconds and nanoseconds")
    void roundTripKeepsPrecision() {
        LocalDateTime original = LocalDateTime.of(2026, 12, 4, 23, 59, 59, 123_456_000);

        LocalDateTime back = FirestoreTypes.toLocalDateTime(FirestoreTypes.toTimestamp(original));

        assertThat(back).isEqualTo(original);
    }

    @Test
    @DisplayName("now() is the current Manila time")
    void nowIsManilaTime() {
        LocalDateTime before = LocalDateTime.now(MANILA);
        LocalDateTime now = FirestoreTypes.now();
        LocalDateTime after = LocalDateTime.now(MANILA);

        assertThat(now).isBetween(before.minus(Duration.ofSeconds(1)), after.plus(Duration.ofSeconds(1)));
    }

    @Test
    @DisplayName("today() is the current Manila date")
    void todayIsManilaDate() {
        LocalDate before = LocalDate.now(MANILA);
        LocalDate today = FirestoreTypes.today();
        LocalDate after = LocalDate.now(MANILA);

        // before/after differ only if the test runs exactly at Manila midnight
        assertThat(today).isIn(before, after);
    }
}
