/*
 * File:        ImageStorageService.java
 * Module:      Report photos
 * Purpose:     Validates, stores and deletes optional report photos on Cloudinary (FR-08, FR-12).
 * Responsible: Felix Joshua H. Abing
 */
package com.paayo.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.paayo.exception.PersistenceException;
import com.paayo.exception.ValidationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

/** Photo rules and storage. An absent or empty file means "no photo". */
@Service
public class ImageStorageService {
    private static final Logger log = LoggerFactory.getLogger(ImageStorageService.class);
    private static final int MAX_SIDE_PIXELS = 1200;
    private static final String FOLDER = "pa-ayo";
    private static final Pattern STORED_PHOTO = Pattern.compile(".*/(" + FOLDER + "/[^/.]+)\\.[a-z]+");

    private final Cloudinary cloudinary;
    private final long maxFileSizeBytes;
    private final List<String> allowedContentTypes;

    /** Reads the upload limits. */
    public ImageStorageService(Cloudinary cloudinary,
                               @Value("${pa-ayo.upload.max-file-size}") DataSize maxFileSize,
                               @Value("${pa-ayo.upload.allowed-types}") List<String> allowedContentTypes) {
        this.cloudinary = cloudinary;
        this.maxFileSizeBytes = maxFileSize.toBytes();
        this.allowedContentTypes = allowedContentTypes;
    }

    /** Rejects a photo that is too large or is not a JPEG, PNG or WebP image. */
    public void validatePhoto(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return;
        }
        if (file.getSize() > maxFileSizeBytes) {
            throw new ValidationException("The photo must be 5 MB or smaller.");
        }
        if (!allowedContentTypes.contains(file.getContentType()) || !hasImageContent(file)) {
            throw new ValidationException("The photo must be a JPEG, PNG or WebP image.");
        }
    }

    /** Stores the photo and returns its URL, or null when there is no photo. */
    public String storePhoto(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        // Large phone photos are scaled down on upload so the dashboard stays fast and the free quota lasts.
        Transformation<?> scaled = new Transformation<>()
                .width(MAX_SIDE_PIXELS).height(MAX_SIDE_PIXELS).crop("limit").quality("auto");
        try {
            Map<?, ?> result = cloudinary.uploader()
                    .upload(file.getBytes(), Map.of("folder", FOLDER, "transformation", scaled));
            return (String) result.get("secure_url");
        } catch (IOException | RuntimeException e) {
            throw new PersistenceException("Photo upload failed", e);
        }
    }

    /** Deletes a stored photo. A failure is logged for manual removal and does not stop the caller. */
    public void deletePhoto(String imageUrl) {
        Matcher stored = STORED_PHOTO.matcher(imageUrl == null ? "" : imageUrl);
        if (!stored.matches()) {
            return; // no photo, or a sample image bundled with the site
        }
        try {
            cloudinary.uploader().destroy(stored.group(1), Map.of("invalidate", true));
        } catch (IOException | RuntimeException e) {
            log.error("Photo {} could not be deleted; remove it in Cloudinary", stored.group(1), e);
        }
    }

    // Browsers report the type from the file extension, so the first bytes are checked too.
    private static boolean hasImageContent(MultipartFile file) {
        try (InputStream in = file.getInputStream()) {
            String head = new String(in.readNBytes(12), StandardCharsets.ISO_8859_1);
            return head.startsWith("\u00FF\u00D8\u00FF")
                    || head.startsWith("\u0089PNG\r\n")
                    || (head.startsWith("RIFF") && head.startsWith("WEBP", 8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
