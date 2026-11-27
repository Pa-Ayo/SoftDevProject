/*
 * File:        FirestoreSmokeIT.java
 * Module:      Testing
 * Purpose:     Proves the Spring context starts against the emulator and that every test starts on an empty database.
 * Responsible: Reuel Matthew Arcilla (QA)
 */
package com.paayo.support;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class FirestoreSmokeIT extends AbstractFirestoreIT {

    @Autowired
    private Firestore firestore;

    @Test
    void contextStartsAndFirestoreRoundTrips() throws Exception {
        firestore.collection("smoke").document("doc1").set(Map.of("hello", "world")).get();

        DocumentSnapshot snapshot = firestore.collection("smoke").document("doc1").get().get();

        assertThat(snapshot.exists()).isTrue();
        assertThat(snapshot.getString("hello")).isEqualTo("world");
    }

    @Test
    void clearAllEmptiesTheDatabase() throws Exception {
        firestore.collection("smoke").document("left-over").set(Map.of("x", 1)).get();

        FirestoreEmulator.clearAll();

        assertThat(firestore.collection("smoke").get().get().isEmpty()).isTrue();
    }
}
