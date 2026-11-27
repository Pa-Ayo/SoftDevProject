/*
 * File:        PersistenceException.java
 * Module:      Error handling
 * Purpose:     Signals that Firestore or photo storage failed or timed out (NFR-02).
 * Responsible: Felix Joshua H. Abing
 */
package com.paayo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Thrown when data cannot be read or written; shown to users as a friendly 503 page when not handled earlier. */
@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class PersistenceException extends RuntimeException {

    /** Creates the exception with a message and the original cause. */
    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
