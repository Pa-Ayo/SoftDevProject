/*
 * File:        Category.java
 * Module:      Data model
 * Purpose:     Lists the three infrastructure categories a report can have (FR-02).
 * Responsible: Felix Joshua H. Abing
 */
package com.paayo.model;

/** Infrastructure sector of a report. The declaration order is the order of the filter pills. */
public enum Category {
    SANITATION("Sanitation"),
    ROADWORKS("Roadworks"),
    ELECTRICAL("Electrical");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    /** Returns the label shown to users. */
    public String getDisplayName() {
        return displayName;
    }
}
