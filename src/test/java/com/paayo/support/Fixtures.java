/*
 * File:        Fixtures.java
 * Module:      Testing
 * Purpose:     Loads saved HTML pages from src/test/resources/fixtures so scraper tests run offline (NFR-15).
 * Responsible: Reuel Matthew Arcilla (QA)
 */
package com.paayo.support;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/** Reads test resources. Example: Fixtures.document("news/listing-with-articles.html"). */
public final class Fixtures {
    /** Base address used to resolve relative links in fixtures. */
    public static final String BASE_URL = "https://www.cebu.gov.ph/";

    private Fixtures() {
    }

    /** Returns the raw text of a file under fixtures/. */
    public static String read(String path) {
        String resource = "/fixtures/" + path;
        try (InputStream in = Fixtures.class.getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalArgumentException("Missing test fixture: src/test/resources" + resource);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read fixture " + resource, e);
        }
    }

    /** Parses a fixture into a Jsoup Document, as NewsScraper.parseArticles would receive it. */
    public static Document document(String path) {
        return Jsoup.parse(read(path), BASE_URL);
    }
}
