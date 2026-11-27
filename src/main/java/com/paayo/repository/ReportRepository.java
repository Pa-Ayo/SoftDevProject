/*
 * File:        ReportRepository.java
 * Module:      Reports
 * Purpose:     Reads and writes the reports collection (FR-02 to FR-05).
 * Responsible: Felix Joshua H. Abing
 */
package com.paayo.repository;

import static com.paayo.repository.FirestoreCalls.await;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.paayo.model.Category;
import com.paayo.model.Report;
import com.paayo.model.ReportStatus;
import com.paayo.util.FirestoreTypes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

/** Firestore access for reports. */
@Repository
public class ReportRepository {
    private static final Logger log = LoggerFactory.getLogger(ReportRepository.class);
    private static final int QUERY_LIMIT = 100;

    private final Firestore firestore;
    private final String collectionName = "reports";

    /** Uses the shared Firestore client. */
    public ReportRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    /** Saves a new report under an auto-generated ID and returns it. */
    public Report save(Report report) {
        DocumentReference ref = reports().document();
        await(ref.set(toMap(report)));
        report.setReportId(ref.getId());
        return report;
    }

    /** Returns the newest reports, at most 100. */
    public List<Report> findAll() {
        return list(reports());
    }

    /** Returns one report, or null if it does not exist. */
    public Report findById(String id) {
        if (id == null || id.isBlank() || id.contains("/")) {
            return null;
        }
        return fromSnapshot(await(reports().document(id).get()));
    }

    /** Returns the newest reports of one category, at most 100. Needs composite index IDX-01. */
    public List<Report> findByCategory(Category category) {
        return list(reports().whereEqualTo("category", category.name()));
    }

    /** Changes only the status and updatedAt of a report. */
    public void updateStatus(String id, ReportStatus status) {
        await(reports().document(id).update("status", status.name(), "updatedAt", Timestamp.now()));
    }

    /** Deletes one report. */
    public void delete(String id) {
        await(reports().document(id).delete());
    }

    private CollectionReference reports() {
        return firestore.collection(collectionName);
    }

    private List<Report> list(Query query) {
        return await(query.orderBy("createdAt", Query.Direction.DESCENDING).limit(QUERY_LIMIT).get())
                .getDocuments().stream()
                .map(this::fromSnapshot)
                .filter(Objects::nonNull)
                .toList();
    }

    private static Map<String, Object> toMap(Report report) {
        Map<String, Object> map = new HashMap<>(); // Map.of() rejects the null imageUrl
        map.put("title", report.getTitle());
        map.put("category", report.getCategory().name());
        map.put("location", report.getLocation());
        map.put("description", report.getDescription());
        map.put("imageUrl", report.getImageUrl());
        map.put("status", report.getStatus().name());
        map.put("createdAt", FirestoreTypes.toTimestamp(report.getCreatedAt()));
        map.put("updatedAt", FirestoreTypes.toTimestamp(report.getUpdatedAt()));
        return map;
    }

    private Report fromSnapshot(DocumentSnapshot doc) {
        if (!doc.exists()) {
            return null;
        }
        try {
            Report report = new Report();
            report.setReportId(doc.getId());
            report.setTitle(doc.getString("title"));
            report.setCategory(Category.valueOf(doc.getString("category")));
            report.setLocation(doc.getString("location"));
            report.setDescription(doc.getString("description"));
            report.setImageUrl(doc.getString("imageUrl"));
            report.setStatus(ReportStatus.valueOf(doc.getString("status")));
            report.setCreatedAt(FirestoreTypes.toLocalDateTime(doc.getTimestamp("createdAt")));
            report.setUpdatedAt(FirestoreTypes.toLocalDateTime(doc.getTimestamp("updatedAt")));
            return report;
        } catch (RuntimeException e) {
            // One malformed document must not take down the dashboard.
            log.warn("Skipping malformed report {}", doc.getId(), e);
            return null;
        }
    }
}
