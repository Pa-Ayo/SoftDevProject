/*
 * File:        NewsService.java
 * Module:      News bot
 * Purpose:     Runs the news pipeline and provides the latest news (FR-06, FR-07, NFR-11).
 * Responsible: Felix Joshua H. Abing
 */
package com.paayo.service;

import com.paayo.exception.PersistenceException;
import com.paayo.model.NewsItem;
import com.paayo.newsbot.NewsScraper;
import com.paayo.newsbot.TextParaphraser;
import com.paayo.repository.NewsRepository;
import com.paayo.util.FirestoreTypes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** News business rules. */
@Service
public class NewsService {
    private static final Logger log = LoggerFactory.getLogger(NewsService.class);

    private final NewsRepository newsRepository;
    private final NewsScraper scraper;
    private final TextParaphraser paraphraser;

    /** Uses the news repository, the scraper and the summarizer. */
    public NewsService(NewsRepository newsRepository, NewsScraper scraper, TextParaphraser paraphraser) {
        this.newsRepository = newsRepository;
        this.scraper = scraper;
        this.paraphraser = paraphraser;
    }

    /**
     * Harvests, condenses and stores new articles and returns how many were published.
     * Only one run at a time, so a manual run cannot duplicate a scheduled one.
     */
    public synchronized int fetchAndPublishNews() {
        List<NewsItem> items = new ArrayList<>(scraper.scrape());
        for (NewsItem item : items) {
            if (item.getPublishedDate() == null) {
                item.setPublishedDate(FirestoreTypes.today());
            }
        }
        // Oldest first: the dashboard sorts by fetch time, so the newest article ends up on top.
        items.sort(Comparator.comparing(NewsItem::getPublishedDate));
        int published = 0;
        for (NewsItem item : items) {
            if (publish(item)) {
                published++;
            }
        }
        return published;
    }

    /** Returns the most recently fetched news items. */
    public List<NewsItem> getLatestNews(int limit) {
        return newsRepository.findLatest(limit);
    }

    /** Returns true if an article with this URL is already stored. */
    public boolean isDuplicate(String sourceUrl) {
        return newsRepository.existsBySourceUrl(sourceUrl);
    }

    private boolean publish(NewsItem item) {
        if (isBlank(item.getTitle()) || isBlank(item.getSourceUrl())) {
            return false;
        }
        try {
            if (isDuplicate(item.getSourceUrl())) {
                return false;
            }
            String text = isBlank(item.getSummary())
                    ? scraper.fetchArticleText(item.getSourceUrl()) : item.getSummary();
            String summary = paraphraser.paraphrase(text);
            if (summary.isEmpty()) {
                return false; // the next run tries this article again
            }
            item.setSummary(summary);
            item.setFetchedAt(FirestoreTypes.now());
            newsRepository.save(item);
            return true;
        } catch (PersistenceException e) {
            log.error("News item {} could not be stored", item.getSourceUrl(), e);
            return false;
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
