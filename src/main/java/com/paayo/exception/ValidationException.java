/*
 * File:        ValidationException.java
 * Module:      Error handling
 * Purpose:     Signals that submitted data breaks a validation rule (FR-08).
 * Responsible: Reuel Matthew Arcilla
 */
package com.paayo.exception;

/** Thrown when input is rejected; the message is safe to show to the user. */
public class ValidationException extends RuntimeException {

    /** Creates the exception with a message for the user. */
    public ValidationException(String message) {
        super(message);
    }
}
