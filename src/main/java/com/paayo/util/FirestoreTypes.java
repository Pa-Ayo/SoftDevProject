/*
 * File:        FirestoreTypes.java
 * Module:      Database
 * Purpose:     Converts between Java date types (Asia/Manila) and Firestore timestamps (NFR-16).
 * Responsible: Felix Joshua H. Abing
 */
package com.paayo.util;

import com.google.cloud.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/** Date and time conversions shared by the repositories and services. */
public final class FirestoreTypes {
    private static final ZoneId MANILA_ZONE = ZoneId.of("Asia/Manila");

    private FirestoreTypes() {
    }

    /** Returns the current date and time in Manila. */
    public static LocalDateTime now() {
        return LocalDateTime.now(MANILA_ZONE);
    }

    /** Returns the current date in Manila. */
    public static LocalDate today() {
        return LocalDate.now(MANILA_ZONE);
    }

    /** Converts a Manila date-time to a Firestore timestamp; null stays null. */
    public static Timestamp toTimestamp(LocalDateTime ldt) {
        if (ldt == null) {
            return null;
        }
        Instant instant = ldt.atZone(MANILA_ZONE).toInstant();
        return Timestamp.ofTimeSecondsAndNanos(instant.getEpochSecond(), instant.getNano());
    }

    /** Converts a Firestore timestamp to a Manila date-time; null stays null. */
    public static LocalDateTime toLocalDateTime(Timestamp ts) {
        if (ts == null) {
            return null;
        }
        Instant instant = Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos());
        return LocalDateTime.ofInstant(instant, MANILA_ZONE);
    }
}
