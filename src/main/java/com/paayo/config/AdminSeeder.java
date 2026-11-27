/*
 * File:        AdminSeeder.java
 * Module:      Admin security
 * Purpose:     Creates the first administrator from environment variables when none exists.
 * Responsible: Felix Joshua H. Abing
 */
package com.paayo.config;

import com.paayo.exception.PersistenceException;
import com.paayo.model.Admin;
import com.paayo.repository.AdminRepository;
import com.paayo.service.AdminAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Seeds the first administrator at startup. Passwords, emails and hashes are never logged. */
@Component
public class AdminSeeder {
    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final String username;
    private final String email;
    private final String password;
    private final boolean emulator;

    /** Reads the ADMIN_* settings; example.com addresses are accepted only on the local emulator. */
    public AdminSeeder(AdminRepository adminRepository, PasswordEncoder passwordEncoder,
                       @Value("${pa-ayo.admin.username}") String username,
                       @Value("${pa-ayo.admin.email}") String email,
                       @Value("${pa-ayo.admin.password}") String password,
                       @Value("${pa-ayo.firebase.emulator-host}") String emulatorHost) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
        this.username = AdminAuthService.normalize(username);
        this.email = AdminAuthService.normalize(email);
        this.password = password;
        this.emulator = !emulatorHost.isBlank();
    }

    /** Saves the first administrator if the admins collection is empty. */
    @EventListener(ApplicationReadyEvent.class)
    @Order(1)
    public void seedFirstAdmin() {
        try {
            if (!adminRepository.isEmpty()) {
                return;
            }
            // Placeholder values copied from .env.example must never become a real account.
            boolean placeholder = !emulator && email.endsWith("@example.com");
            if (username.isEmpty() || placeholder || !email.matches("[^@\\s]+@[^@\\s]+")
                    || !AdminAuthService.isAcceptablePassword(password)) {
                log.warn("No administrator exists. Set ADMIN_USERNAME, your real ADMIN_EMAIL and an ADMIN_PASSWORD "
                        + "of 8 to 72 characters, then restart.");
                return;
            }
            Admin admin = new Admin();
            admin.setUsername(username);
            admin.setEmail(email);
            admin.setPasswordHash(passwordEncoder.encode(password));
            adminRepository.save(admin);
            log.info("First administrator created. Remove ADMIN_PASSWORD from the environment now.");
        } catch (PersistenceException e) {
            log.error("Could not check or create the first administrator", e);
        }
    }
}
