/*
 * File:        FirebaseConfig.java
 * Module:      Database
 * Purpose:     Connects to Firestore: the local emulator when configured, otherwise production with a service-account key.
 * Responsible: Felix Joshua H. Abing
 */
package com.paayo.config;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Sets up the Firebase Admin SDK and exposes the single Firestore client. */
@Configuration
public class FirebaseConfig {
    private final String credentialsPath;
    private final String projectId;
    private final String emulatorHost;

    /** Reads the connection settings. */
    public FirebaseConfig(@Value("${pa-ayo.firebase.credentials-path}") String credentialsPath,
                          @Value("${pa-ayo.firebase.project-id}") String projectId,
                          @Value("${pa-ayo.firebase.emulator-host}") String emulatorHost) {
        this.credentialsPath = credentialsPath;
        this.projectId = projectId;
        this.emulatorHost = emulatorHost;
    }

    /** Provides the Firestore client used by every repository. */
    @Bean
    public Firestore firestore() {
        initializeFirebase();
        return FirestoreClient.getFirestore();
    }

    /** Initializes the Firebase app once; fails with a clear message when nothing is configured. */
    public void initializeFirebase() {
        if (!FirebaseApp.getApps().isEmpty()) {
            return;
        }
        FirebaseOptions options;
        if (!emulatorHost.isBlank()) {
            options = FirebaseOptions.builder()
                    .setProjectId(projectId)
                    .setCredentials(GoogleCredentials.create(AccessToken.newBuilder().setTokenValue("emulator").build()))
                    .setFirestoreOptions(FirestoreOptions.newBuilder()
                            .setProjectId(projectId).setEmulatorHost(emulatorHost).build())
                    .build();
        } else if (!credentialsPath.isBlank()) {
            try (InputStream key = Files.newInputStream(Path.of(credentialsPath))) {
                options = FirebaseOptions.builder().setCredentials(GoogleCredentials.fromStream(key)).build();
            } catch (IOException e) {
                throw new UncheckedIOException("Cannot read the Firebase key at FIREBASE_CREDENTIALS_PATH", e);
            }
        } else {
            throw new IllegalStateException(
                    "Set FIRESTORE_EMULATOR_HOST (local) or FIREBASE_CREDENTIALS_PATH (production). See .env.example.");
        }
        FirebaseApp.initializeApp(options);
    }
}
