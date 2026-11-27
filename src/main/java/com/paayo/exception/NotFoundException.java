/*
 * File:        NotFoundException.java
 * Module:      Error handling
 * Purpose:     Signals that a requested record does not exist (UC-03 E3).
 * Responsible: Felix Joshua H. Abing
 */
package com.paayo.exception;

/** Thrown when a record cannot be found. */
public class NotFoundException extends RuntimeException {

    /** Creates the exception with a message for the user. */
    public NotFoundException(String message) {
        super(message);
    }
}
