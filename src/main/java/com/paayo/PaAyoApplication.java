/*
 * File:        PaAyoApplication.java
 * Module:      Project setup
 * Purpose:     Starts the Pa-Ayo Spring Boot application and enables scheduled jobs.
 * Responsible: Felix Joshua H. Abing
 */
package com.paayo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Application entry point. Admins live in Firestore, so Spring's default in-memory user is switched off. */
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@EnableScheduling
public class PaAyoApplication {

    /** Launches the web application. */
    public static void main(String[] args) {
        SpringApplication.run(PaAyoApplication.class, args);
    }
}
