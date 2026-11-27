/*
 * File:        SeedData.java
 * Module:      Testing
 * Purpose:     Writes test reports and a test administrator straight into the Firestore emulator, so browser
 *              tests do not depend on the application's own save code.
 * Responsible: Reuel Matthew Arcilla (QA)
 */
package com.paayo.support;

import com.google.cloud.firestore.Firestore;
import com.paayo.util.FirestoreTypes;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/** Field names follow the proposal's schema: reports, admins. */
public final class SeedData {
    /** A tiny grey picture as a data: URI, so seeded "photos" load without any network (NFR-15). */
    public static final String PHOTO = "data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' "
            + "width='400' height='300'><rect width='400' height='300' fill='%23999999'/></svg>";

    private static final String[] CATEGORIES = {"SANITATION", "ROADWORKS", "ELECTRICAL"};
    private static final String[] STATUSES = {"REPORTED", "IN_PROGRESS", "RESOLVED"};

    private SeedData() {
    }

    /** Writes one report document with a fixed id. imageUrl may be null (placeholder card). */
    public static void report(Firestore firestore, String id, String title, String category, String status,
                              String imageUrl, LocalDateTime createdAt) throws Exception {
        Map<String, Object> doc = new HashMap<>();
        doc.put("title", title);
        doc.put("category", category);
        doc.put("location", "Seed street");
        doc.put("description", "Seed description");
        doc.put("imageUrl", imageUrl);
        doc.put("status", status);
        doc.put("createdAt", FirestoreTypes.toTimestamp(createdAt));
        doc.put("updatedAt", FirestoreTypes.toTimestamp(createdAt));
        firestore.collection("reports").document(id).set(doc).get();
    }

    /**
     * TC-12 data: 30 reports, 10 per category, all three statuses, every second one with a photo.
     * "Seed report 00" is the newest, "Seed report 29" the oldest.
     */
    public static void thirtyReports(Firestore firestore) throws Exception {
        LocalDateTime now = FirestoreTypes.now();
        for (int i = 0; i < 30; i++) {
            report(firestore, String.format("seed-%02d", i), String.format("Seed report %02d", i),
                    CATEGORIES[i % 3], STATUSES[(i / 3) % 3], (i % 2 == 0) ? PHOTO : null, now.minusMinutes(i));
        }
    }

    /** Writes an administrator with a BCrypt password hash (document id "admin-qa"). */
    public static void admin(Firestore firestore, String username, String email, String rawPassword)
            throws Exception {
        Map<String, Object> doc = new HashMap<>();
        doc.put("adminId", "admin-qa");
        doc.put("username", username);
        doc.put("email", email);
        doc.put("passwordHash", new BCryptPasswordEncoder().encode(rawPassword));
        doc.put("resetTokenHash", null);
        doc.put("resetTokenExpiresAt", null);
        firestore.collection("admins").document("admin-qa").set(doc).get();
    }
}
