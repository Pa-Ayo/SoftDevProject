/*
 * File:        CloudinaryConfig.java
 * Module:      Report photos
 * Purpose:     Sets up the Cloudinary client that stores report photos (FR-12).
 * Responsible: Felix Joshua H. Abing
 */
package com.paayo.config;

import com.cloudinary.Cloudinary;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Cloudinary connection. Without CLOUDINARY_URL the site still runs, but photo uploads are refused. */
@Configuration
public class CloudinaryConfig {
    private static final Logger log = LoggerFactory.getLogger(CloudinaryConfig.class);
    private static final int TIMEOUT_SECONDS = 5;

    /** Provides the Cloudinary client. It gives up after 5 seconds without an answer; a slow upload may take longer. */
    @Bean
    public Cloudinary cloudinary(@Value("${pa-ayo.cloudinary.url}") String url) {
        if (url.isBlank()) {
            log.warn("CLOUDINARY_URL is not set: reports with a photo cannot be saved");
        }
        Cloudinary cloudinary = url.isBlank() ? new Cloudinary(Map.of()) : new Cloudinary(url);
        cloudinary.config.timeout = TIMEOUT_SECONDS;
        return cloudinary;
    }
}
