/*
 * File:        AdminAuthService.java
 * Module:      Admin security
 * Purpose:     Verifies admin logins and runs the Forgot Password flow (FR-05, FR-13, NFR-06).
 * Responsible: Felix Joshua H. Abing
 */
package com.paayo.service;

import com.paayo.model.Admin;
import com.paayo.repository.AdminRepository;
import com.paayo.util.FirestoreTypes;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/** Administrator authentication rules. Tokens, emails and passwords are never logged. */
@Service
public class AdminAuthService {
    private static final Logger log = LoggerFactory.getLogger(AdminAuthService.class);
    private static final int TOKEN_BYTES = 32;
    private static final int TOKEN_MINUTES = 30;

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final String baseUrl;
    private final String mailFrom;
    private final SecureRandom random = new SecureRandom();

    /** Uses the admin repository, BCrypt and the mail server. */
    public AdminAuthService(AdminRepository adminRepository, PasswordEncoder passwordEncoder,
                            JavaMailSender mailSender,
                            @Value("${pa-ayo.base-url}") String baseUrl,
                            @Value("${pa-ayo.mail.from}") String mailFrom) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
        this.baseUrl = baseUrl;
        this.mailFrom = mailFrom;
    }

    /** Returns a username or email trimmed and in lowercase, the form stored in Firestore. */
    public static String normalize(String identifier) {
        return identifier == null ? "" : identifier.trim().toLowerCase(Locale.ROOT);
    }

    /** Returns true for 8 characters or more, up to the 72 bytes BCrypt can hash. */
    public static boolean isAcceptablePassword(String password) {
        return password != null && password.length() >= 8
                && password.getBytes(StandardCharsets.UTF_8).length <= 72;
    }

    /** Returns true when the username exists and the password matches its BCrypt hash. */
    public boolean authenticate(String username, String password) {
        Admin admin = adminRepository.findByUsername(normalize(username));
        return admin != null && passwordEncoder.matches(password, admin.getPasswordHash());
    }

    /** Stores a hashed 30-minute token and emails the reset link; does nothing for an unknown account. */
    public void initiatePasswordReset(String identifier) {
        String key = normalize(identifier);
        Admin admin = adminRepository.findByUsername(key);
        if (admin == null) {
            admin = adminRepository.findByEmail(key);
        }
        if (admin == null) {
            return;
        }
        byte[] bytes = new byte[TOKEN_BYTES];
        random.nextBytes(bytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        adminRepository.saveResetToken(admin.getAdminId(), sha256(rawToken),
                FirestoreTypes.now().plusMinutes(TOKEN_MINUTES));
        // Sent in the background: a slow reply would reveal that the account exists.
        String recipient = admin.getEmail();
        CompletableFuture.runAsync(() -> sendResetEmail(recipient, rawToken));
    }

    /** Returns true when the token belongs to an account and has not expired. */
    public boolean validateResetToken(String rawToken) {
        return findByValidToken(rawToken) != null;
    }

    /** Sets the new password and invalidates the token; returns false if either one is not acceptable. */
    public boolean resetPassword(String rawToken, String newRawPassword) {
        if (!isAcceptablePassword(newRawPassword)) {
            return false;
        }
        Admin admin = findByValidToken(rawToken);
        return admin != null
                && adminRepository.updatePasswordHash(admin.getAdminId(), passwordEncoder.encode(newRawPassword));
    }

    private Admin findByValidToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return null;
        }
        Admin admin = adminRepository.findByResetTokenHash(sha256(rawToken));
        boolean valid = admin != null && admin.getResetTokenExpiresAt() != null
                && admin.getResetTokenExpiresAt().isAfter(FirestoreTypes.now());
        return valid ? admin : null;
    }

    private void sendResetEmail(String recipient, String rawToken) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(mailFrom);
        mail.setTo(recipient);
        mail.setSubject("Reset your Pa-Ayo admin password");
        mail.setText("Open this link to set a new password:\n\n"
                + baseUrl + "/admin/reset-password?token=" + rawToken
                + "\n\nThe link works once and expires in " + TOKEN_MINUTES + " minutes. "
                + "If you did not ask for it, you can ignore this email.");
        try {
            mailSender.send(mail);
        } catch (MailException e) {
            log.error("The password reset email could not be sent", e);
        }
    }

    private static String sha256(String value) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
