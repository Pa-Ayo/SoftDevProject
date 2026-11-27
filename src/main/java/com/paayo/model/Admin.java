/*
 * File:        Admin.java
 * Module:      Data model
 * Purpose:     An administrator account with its password and reset-token hashes (FR-05, FR-13).
 * Responsible: Felix Joshua H. Abing
 */
package com.paayo.model;

import java.time.LocalDateTime;

/** One account in the Firestore admins collection. */
public class Admin {
    private String adminId;
    private String username;
    private String email;
    private String passwordHash;
    private String resetTokenHash;
    private LocalDateTime resetTokenExpiresAt;

    /** Returns the Firestore document ID. */
    public String getAdminId() {
        return adminId;
    }

    /** Sets the Firestore document ID. */
    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    /** Returns the login name. */
    public String getUsername() {
        return username;
    }

    /** Sets the login name. */
    public void setUsername(String username) {
        this.username = username;
    }

    /** Returns the recovery email. */
    public String getEmail() {
        return email;
    }

    /** Sets the recovery email. */
    public void setEmail(String email) {
        this.email = email;
    }

    /** Returns the BCrypt password hash. */
    public String getPasswordHash() {
        return passwordHash;
    }

    /** Sets the BCrypt password hash. */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /** Returns the SHA-256 hash of the active reset token, or null. */
    public String getResetTokenHash() {
        return resetTokenHash;
    }

    /** Sets the SHA-256 hash of the active reset token, or null. */
    public void setResetTokenHash(String resetTokenHash) {
        this.resetTokenHash = resetTokenHash;
    }

    /** Returns the reset token expiry (Asia/Manila), or null. */
    public LocalDateTime getResetTokenExpiresAt() {
        return resetTokenExpiresAt;
    }

    /** Sets the reset token expiry (Asia/Manila), or null. */
    public void setResetTokenExpiresAt(LocalDateTime resetTokenExpiresAt) {
        this.resetTokenExpiresAt = resetTokenExpiresAt;
    }
}
