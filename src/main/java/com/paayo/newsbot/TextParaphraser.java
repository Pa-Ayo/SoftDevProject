/*
 * File:        TextParaphraser.java
 * Module:      News bot
 * Purpose:     Condenses announcements into short summaries with fixed rules, no AI service (FR-07).
 * Responsible: Felix Joshua H. Abing
 */
package com.paayo.newsbot;

import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Extractive summarizer: keeps whole leading sentences up to the maximum length. */
@Component
public class TextParaphraser {
    // A sentence ends at . ! or ? followed by a space, except after common abbreviations.
    private static final String SENTENCE_END =
            "(?<=[.!?])(?<!\\b(?:Gov|Engr|Dr|Mr|Ms|Mrs|St|Sr|Jr|Hon|Brgy|No|[ap]\\.m)\\.) ";

    private final int maxSummaryLength;

    /** Reads the maximum summary length. */
    public TextParaphraser(@Value("${pa-ayo.news.max-summary-length}") int maxSummaryLength) {
        this.maxSummaryLength = maxSummaryLength;
    }

    /** Returns a plain-text summary no longer than the maximum; blank input gives an empty summary. */
    public String paraphrase(String originalText) {
        if (originalText == null || originalText.isBlank()) {
            return "";
        }
        String clean = Jsoup.parse(originalText).text();
        StringBuilder summary = new StringBuilder();
        for (String sentence : clean.split(SENTENCE_END)) {
            if (summary.length() + sentence.length() > maxSummaryLength) {
                break;
            }
            summary.append(sentence).append(' ');
        }
        return summary.isEmpty() ? truncate(clean) : summary.toString().trim();
    }

    private String truncate(String text) {
        int limit = maxSummaryLength - 3;
        int lastSpace = text.lastIndexOf(' ', limit);
        return text.substring(0, lastSpace > 0 ? lastSpace : limit) + "...";
    }
}
