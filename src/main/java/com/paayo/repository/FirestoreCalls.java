/*
 * File:        FirestoreCalls.java
 * Module:      Database
 * Purpose:     Waits for Firestore calls with the 5-second hard timeout (NFR-02).
 * Responsible: Felix Joshua H. Abing
 */
package com.paayo.repository;

import com.google.api.core.ApiFuture;
import com.paayo.exception.PersistenceException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/** Shared error and timeout handling for the repositories. */
final class FirestoreCalls {
    private static final int TIMEOUT_SECONDS = 5;

    private FirestoreCalls() {
    }

    /** Returns the call's result, or throws PersistenceException if it fails or takes over 5 seconds. */
    static <T> T await(ApiFuture<T> future) {
        try {
            return future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PersistenceException("Firestore call was interrupted", e);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new PersistenceException("Firestore did not answer within " + TIMEOUT_SECONDS + " seconds", e);
        } catch (ExecutionException e) {
            throw new PersistenceException("Firestore call failed", e);
        }
    }
}
