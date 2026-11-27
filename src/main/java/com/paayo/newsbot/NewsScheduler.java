/*
 * File:        NewsScheduler.java
 * Module:      News bot
 * Purpose:     Runs the news bot at startup and on a daily schedule (FR-06).
 * Responsible: Felix Joshua H. Abing
 */
package com.paayo.newsbot;

import com.paayo.service.NewsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Time-based trigger of the news bot. The startup run covers schedules missed while Render slept. */
@Component
public class NewsScheduler {
    private static final Logger log = LoggerFactory.getLogger(NewsScheduler.class);

    private final NewsService newsService;
    private final String cronExpression;

    /** Uses the news service and the configured schedule. */
    public NewsScheduler(NewsService newsService, @Value("${pa-ayo.news.cron}") String cronExpression) {
        this.newsService = newsService;
        this.cronExpression = cronExpression;
    }

    /** Fetches and publishes news; a failure is logged and never reaches the website. */
    @Scheduled(cron = "${pa-ayo.news.cron}", zone = "Asia/Manila")
    @EventListener(ApplicationReadyEvent.class)
    @Order(3)
    public void runScheduledFetch() {
        try {
            log.info("News bot published {} new item(s); schedule: {} Asia/Manila",
                    newsService.fetchAndPublishNews(), cronExpression);
        } catch (RuntimeException e) {
            log.error("News bot run failed", e);
        }
    }
}
