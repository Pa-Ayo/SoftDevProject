/*
 * File:        FirebaseConfigTest.java
 * Module:      Testing
 * Purpose:     Unit tests for FirebaseConfig start-up rules. No network or emulator needed.
 * Responsible: Reuel Matthew Arcilla (QA)
 */
package com.paayo.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.firebase.FirebaseApp;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FirebaseConfigTest {

    // FirebaseApp is a JVM-wide singleton; start and end every test without one.
    @BeforeEach
    void deleteFirebaseAppsBefore() {
        FirebaseApp.getApps().forEach(FirebaseApp::delete);
    }

    @AfterEach
    void deleteFirebaseAppsAfter() {
        FirebaseApp.getApps().forEach(FirebaseApp::delete);
    }

    @Test
    @DisplayName("fails with a clear message when neither emulator nor key is configured")
    void failsWhenNothingConfigured() {
        FirebaseConfig config = new FirebaseConfig("", "demo-pa-ayo-test", "");

        assertThatThrownBy(config::initializeFirebase)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("FIRESTORE_EMULATOR_HOST")
                .hasMessageContaining("FIREBASE_CREDENTIALS_PATH");
        assertThat(FirebaseApp.getApps()).isEmpty();
    }

    @Test
    @DisplayName("fails with a clear message when the key file does not exist")
    void failsWhenKeyFileMissing(@TempDir Path tmp) {
        String missing = tmp.resolve("no-such-key.json").toString();
        FirebaseConfig config = new FirebaseConfig(missing, "", "");

        assertThatThrownBy(config::initializeFirebase)
                .isInstanceOf(UncheckedIOException.class)
                .hasMessageContaining("FIREBASE_CREDENTIALS_PATH");
        assertThat(FirebaseApp.getApps()).isEmpty();
    }

    @Test
    @DisplayName("emulator mode starts the Firebase app without credentials")
    void emulatorModeInitializes() {
        FirebaseConfig config = new FirebaseConfig("", "demo-pa-ayo-test", "127.0.0.1:8085");

        config.initializeFirebase();

        assertThat(FirebaseApp.getApps()).hasSize(1);
    }

    @Test
    @DisplayName("calling initializeFirebase twice does not create a second app")
    void initializeIsIdempotent() {
        FirebaseConfig config = new FirebaseConfig("", "demo-pa-ayo-test", "127.0.0.1:8085");

        config.initializeFirebase();
        config.initializeFirebase();

        assertThat(FirebaseApp.getApps()).hasSize(1);
    }

    @Test
    @DisplayName("emulator host wins over a key path")
    void emulatorHostWinsOverKeyPath(@TempDir Path tmp) {
        String missing = tmp.resolve("no-such-key.json").toString();
        FirebaseConfig config = new FirebaseConfig(missing, "demo-pa-ayo-test", "127.0.0.1:8085");

        config.initializeFirebase();   // would throw if it tried to read the missing key

        assertThat(FirebaseApp.getApps()).hasSize(1);
    }
}
