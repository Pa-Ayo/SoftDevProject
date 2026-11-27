/*
 * File:        NewsRepository.java
 * Module:      News bot
 * Purpose:     Reads and writes the news collection (FR-07, NFR-11).
 * Responsible: Felix Joshua H. Abing
 */
package com.paayo.repository;

import static com.paayo.repository.FirestoreCalls.await;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.paayo.model.NewsItem;
import com.paayo.util.FirestoreTypes;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

/** Firestore access for news items. */
@Repository
public class NewsRepository {
    private static final Logger log = LoggerFactory.getLogger(NewsRepository.class);

    private final Firestore firestore;
    private final String collectionName = "news";

    /** Uses the shared Firestore client. */
    public NewsRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    /** Saves a news item under an auto-generated ID and returns it. */
    public NewsItem save(NewsItem item) {
        DocumentReference ref = news().document();
        await(ref.set(toMap(item)));
        item.setNewsId(ref.getId());
        return item;
    }

    /** Returns the most recently fetched items. */
    public List<NewsItem> findLatest(int limit) {
        return await(news().orderBy("fetchedAt", Query.Direction.DESCENDING).limit(limit).get())
                .getDocuments().stream()
                .map(NewsRepository::fromSnapshot)
                .filter(Objects::nonNull)
                .toList();
    }

    /** Returns true if an item with this source URL is already stored. */
    public boolean existsBySourceUrl(String url) {
        return !await(news().whereEqualTo("sourceUrl", url).limit(1).get()).isEmpty();
    }

    private CollectionReference news() {
        return firestore.collection(collectionName);
    }

    private static Map<String, Object> toMap(NewsItem item) {
        Map<String, Object> map = new HashMap<>(); // Map.of() rejects the null imageUrl
        map.put("title", item.getTitle());
        map.put("summary", item.getSummary());
        map.put("sourceName", item.getSourceName());
        map.put("sourceUrl", item.getSourceUrl());
        map.put("imageUrl", item.getImageUrl());
        map.put("publishedDate", item.getPublishedDate().toString());
        map.put("fetchedAt", FirestoreTypes.toTimestamp(item.getFetchedAt()));
        return map;
    }

    private static NewsItem fromSnapshot(DocumentSnapshot doc) {
        try {
            NewsItem item = new NewsItem();
            item.setNewsId(doc.getId());
            item.setTitle(doc.getString("title"));
            item.setSummary(doc.getString("summary"));
            item.setSourceName(doc.getString("sourceName"));
            item.setSourceUrl(doc.getString("sourceUrl"));
            item.setImageUrl(doc.getString("imageUrl"));
            item.setPublishedDate(LocalDate.parse(doc.getString("publishedDate")));
            item.setFetchedAt(FirestoreTypes.toLocalDateTime(doc.getTimestamp("fetchedAt")));
            return item;
        } catch (RuntimeException e) {
            // One malformed document must not take down the dashboard.
            log.warn("Skipping malformed news item {}", doc.getId(), e);
            return null;
        }
    }
}
