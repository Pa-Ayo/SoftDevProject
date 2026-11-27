/*
 * File:        NewsItem.java
 * Module:      Data model
 * Purpose:     A summarized government news item (FR-06, FR-07).
 * Responsible: Felix Joshua H. Abing
 */
package com.paayo.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** One item in the Firestore news collection. */
public class NewsItem {
    private String newsId;
    private String title;
    private String summary;
    private String sourceName;
    private String sourceUrl;
    private String imageUrl;
    private LocalDate publishedDate;
    private LocalDateTime fetchedAt;

    /** Returns the Firestore document ID. */
    public String getNewsId() {
        return newsId;
    }

    /** Sets the Firestore document ID. */
    public void setNewsId(String newsId) {
        this.newsId = newsId;
    }

    /** Returns the headline. */
    public String getTitle() {
        return title;
    }

    /** Sets the headline. */
    public void setTitle(String title) {
        this.title = title;
    }

    /** Returns the article text, replaced by the condensed summary before saving. */
    public String getSummary() {
        return summary;
    }

    /** Sets the article text, replaced by the condensed summary before saving. */
    public void setSummary(String summary) {
        this.summary = summary;
    }

    /** Returns the originating agency or site. */
    public String getSourceName() {
        return sourceName;
    }

    /** Sets the originating agency or site. */
    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    /** Returns the link to the original article (deduplication key). */
    public String getSourceUrl() {
        return sourceUrl;
    }

    /** Sets the link to the original article (deduplication key). */
    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    /** Returns the thumbnail URL, or null when there is none. */
    public String getImageUrl() {
        return imageUrl;
    }

    /** Sets the thumbnail URL, or null when there is none. */
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    /** Returns the publication date. */
    public LocalDate getPublishedDate() {
        return publishedDate;
    }

    /** Sets the publication date. */
    public void setPublishedDate(LocalDate publishedDate) {
        this.publishedDate = publishedDate;
    }

    /** Returns the harvest time (Asia/Manila). */
    public LocalDateTime getFetchedAt() {
        return fetchedAt;
    }

    /** Sets the harvest time (Asia/Manila). */
    public void setFetchedAt(LocalDateTime fetchedAt) {
        this.fetchedAt = fetchedAt;
    }
}
