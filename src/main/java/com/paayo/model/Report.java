/*
 * File:        Report.java
 * Module:      Data model
 * Purpose:     A community infrastructure report and its field rules (FR-01, FR-03, FR-08).
 * Responsible: Felix Joshua H. Abing
 */
package com.paayo.model;

import com.paayo.util.FirestoreTypes;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/** One report in the Firestore reports collection. */
public class Report {
    private static final int TITLE_MAX = 100;
    private static final int LOCATION_MAX = 200;
    private static final int DESCRIPTION_MAX = 1000;

    private String reportId;

    @NotBlank(message = "Enter a report title.")
    @Size(max = TITLE_MAX, message = "Keep the title within {max} characters.")
    private String title;

    @NotNull(message = "Select a category.")
    private Category category;

    @NotBlank(message = "Enter the location.")
    @Size(max = LOCATION_MAX, message = "Keep the location within {max} characters.")
    private String location;

    @NotBlank(message = "Describe the issue.")
    @Size(max = DESCRIPTION_MAX, message = "Keep the description within {max} characters.")
    private String description;

    private String imageUrl;
    private ReportStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** Changes the status and records when it changed. */
    public void updateStatus(ReportStatus newStatus) {
        status = newStatus;
        updatedAt = FirestoreTypes.now();
    }

    /** Returns true when every required field is present and within its length limit after trimming. */
    public boolean isValid() {
        return category != null
                && fits(title, TITLE_MAX)
                && fits(location, LOCATION_MAX)
                && fits(description, DESCRIPTION_MAX);
    }

    private static boolean fits(String value, int max) {
        return value != null && !value.isBlank() && value.trim().length() <= max;
    }

    /** Returns the Firestore document ID. */
    public String getReportId() {
        return reportId;
    }

    /** Sets the Firestore document ID. */
    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    /** Returns the short headline. */
    public String getTitle() {
        return title;
    }

    /** Sets the short headline. */
    public void setTitle(String title) {
        this.title = title;
    }

    /** Returns the infrastructure category. */
    public Category getCategory() {
        return category;
    }

    /** Sets the infrastructure category. */
    public void setCategory(Category category) {
        this.category = category;
    }

    /** Returns the typed street or barangay. */
    public String getLocation() {
        return location;
    }

    /** Sets the typed street or barangay. */
    public void setLocation(String location) {
        this.location = location;
    }

    /** Returns the detailed explanation. */
    public String getDescription() {
        return description;
    }

    /** Sets the detailed explanation. */
    public void setDescription(String description) {
        this.description = description;
    }

    /** Returns the photo URL, or null when there is no photo. */
    public String getImageUrl() {
        return imageUrl;
    }

    /** Sets the photo URL, or null when there is no photo. */
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    /** Returns the lifecycle state. */
    public ReportStatus getStatus() {
        return status;
    }

    /** Sets the lifecycle state. */
    public void setStatus(ReportStatus status) {
        this.status = status;
    }

    /** Returns the submission time (Asia/Manila). */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /** Sets the submission time (Asia/Manila). */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /** Returns the last status change (Asia/Manila). */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /** Sets the last status change (Asia/Manila). */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
