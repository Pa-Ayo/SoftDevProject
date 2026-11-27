/*
 * File:        FirestoreEmulator.java
 * Module:      Testing
 * Purpose:     Detects and resets the local Firestore emulator used by integration tests.
 * Responsible: Reuel Matthew Arcilla (QA)
 */
package com.paayo.support;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/** Talks to the Firestore emulator on 127.0.0.1:8085 (see firebase.json). */
public final class FirestoreEmulator {
    /** Test-only project id, kept apart from the dev project "demo-pa-ayo" so tests never wipe seed data. */
    public static final String PROJECT_ID = "demo-pa-ayo-test";
    public static final String HOST = "127.0.0.1";
    public static final int PORT = 8085;
    public static final String HOST_PORT = HOST + ":" + PORT;

    /** Shown when the emulator is not running. */
    public static final String START_HINT =
            "Firestore emulator is not running on " + HOST_PORT
                    + ". Start it with: npx -y firebase-tools@15 emulators:start --only firestore --project demo-pa-ayo"
                    + " (or run everything with: npx -y firebase-tools@15 emulators:exec --only firestore"
                    + " --project demo-pa-ayo \"mvn verify -Pintegration\")";

    private FirestoreEmulator() {
    }

    /** True when something is listening on the emulator port. */
    public static boolean isRunning() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(HOST, PORT), 500);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /** Deletes every document of the test project. Other emulator projects are untouched. */
    public static void clearAll() {
        URI uri = URI.create("http://" + HOST_PORT + "/emulator/v1/projects/" + PROJECT_ID
                + "/databases/(default)/documents");
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(5))
                .DELETE()
                .build();
        try {
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IllegalStateException("Could not clear the emulator: HTTP "
                        + response.statusCode() + " " + response.body());
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not reach the Firestore emulator", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while clearing the emulator", e);
        }
    }
}
