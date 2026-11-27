/*
 * File:        ReportStatus.java
 * Module:      Data model
 * Purpose:     Lists the lifecycle states of a report (FR-05).
 * Responsible: Felix Joshua H. Abing
 */
package com.paayo.model;

/** Lifecycle state of a report. */
public enum ReportStatus {
    REPORTED("Reported"),
    IN_PROGRESS("In Progress"),
    RESOLVED("Resolved");

    private final String displayName;

    ReportStatus(String displayName) {
        this.displayName = displayName;
    }

    /** Returns the label shown to users. */
    public String getDisplayName() {
        return displayName;
    }
}
