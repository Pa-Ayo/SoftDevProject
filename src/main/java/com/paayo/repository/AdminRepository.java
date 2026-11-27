/*
 * File:        AdminRepository.java
 * Module:      Admin security
 * Purpose:     Reads and writes the admins collection, including single-use reset tokens (FR-05, FR-13).
 * Responsible: Felix Joshua H. Abing
 */
package com.paayo.repository;

import static com.paayo.repository.FirestoreCalls.await;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.paayo.model.Admin;
import com.paayo.util.FirestoreTypes;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Repository;

/** Firestore access for administrator accounts. */
@Repository
public class AdminRepository {
    private final Firestore firestore;

    /** Uses the shared Firestore client. */
    public AdminRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    /** Returns true when no administrator exists yet. */
    public boolean isEmpty() {
        return await(admins().limit(1).get()).isEmpty();
    }

    /** Saves a new administrator under an auto-generated ID and returns it. */
    public Admin save(Admin admin) {
        DocumentReference ref = admins().document();
        Map<String, Object> map = new HashMap<>(); // Map.of() rejects the null token fields
        map.put("username", admin.getUsername());
        map.put("email", admin.getEmail());
        map.put("passwordHash", admin.getPasswordHash());
        map.put("resetTokenHash", admin.getResetTokenHash());
        map.put("resetTokenExpiresAt", FirestoreTypes.toTimestamp(admin.getResetTokenExpiresAt()));
        await(ref.set(map));
        admin.setAdminId(ref.getId());
        return admin;
    }

    /** Finds an administrator by login name, or returns null. */
    public Admin findByUsername(String username) {
        return findOne("username", username);
    }

    /** Finds an administrator by recovery email, or returns null. */
    public Admin findByEmail(String email) {
        return findOne("email", email);
    }

    /** Finds the administrator holding this reset token hash, or returns null. */
    public Admin findByResetTokenHash(String tokenHash) {
        return findOne("resetTokenHash", tokenHash);
    }

    /** Stores a reset token hash and its expiry, replacing any earlier one. */
    public void saveResetToken(String adminId, String tokenHash, LocalDateTime expiresAt) {
        await(admins().document(adminId).update(
                "resetTokenHash", tokenHash,
                "resetTokenExpiresAt", FirestoreTypes.toTimestamp(expiresAt)));
    }

    /**
     * Stores a new password hash and clears the reset token in one transaction, so a token works only once.
     * Returns false if the token is missing or expired.
     */
    public boolean updatePasswordHash(String adminId, String passwordHash) {
        DocumentReference ref = admins().document(adminId);
        return await(firestore.runTransaction(transaction -> {
            Timestamp expiresAt = transaction.get(ref).get().getTimestamp("resetTokenExpiresAt");
            if (expiresAt == null || expiresAt.compareTo(Timestamp.now()) < 0) {
                return false;
            }
            Map<String, Object> updates = new HashMap<>();
            updates.put("passwordHash", passwordHash);
            updates.put("resetTokenHash", null);
            updates.put("resetTokenExpiresAt", null);
            transaction.update(ref, updates);
            return true;
        }));
    }

    private CollectionReference admins() {
        return firestore.collection("admins");
    }

    private Admin findOne(String field, String value) {
        List<QueryDocumentSnapshot> docs = await(admins().whereEqualTo(field, value).limit(1).get()).getDocuments();
        return docs.isEmpty() ? null : fromSnapshot(docs.get(0));
    }

    private static Admin fromSnapshot(DocumentSnapshot doc) {
        Admin admin = new Admin();
        admin.setAdminId(doc.getId());
        admin.setUsername(doc.getString("username"));
        admin.setEmail(doc.getString("email"));
        admin.setPasswordHash(doc.getString("passwordHash"));
        admin.setResetTokenHash(doc.getString("resetTokenHash"));
        admin.setResetTokenExpiresAt(FirestoreTypes.toLocalDateTime(doc.getTimestamp("resetTokenExpiresAt")));
        return admin;
    }
}
