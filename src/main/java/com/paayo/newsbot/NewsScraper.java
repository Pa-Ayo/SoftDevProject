/*
 * File:        NewsScraper.java
 * Module:      News bot
 * Purpose:     Fetches government news pages with Jsoup and extracts their articles (FR-06, NFR-03).
 * Responsible: Felix Joshua H. Abing
 */
package com.paayo.newsbot;

import com.paayo.model.NewsItem;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** The only class that knows how the news pages are built (NFR-14). */
@Component
public class NewsScraper {
    private static final Logger log = LoggerFactory.getLogger(NewsScraper.class);
    private static final int MAX_ITEMS_PER_SOURCE = 5;

    private static final String ARTICLE = "article, .hentry";
    private static final String TITLE_LINK = ":is(h1, h2, h3, h4) a[href]:not([rel~=tag])";
    private static final String EXCERPT = ".elementor-post__excerpt, .entry-summary";
    private static final List<String> BODY_PARAGRAPHS = List.of(
            ".elementor-widget-theme-post-content p", ".entry-content p", "article p");

    private static final Pattern DATE_TEXT = Pattern.compile("[A-Z][a-z]{2,8} \\d{1,2}, \\d{4}");
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("[MMMM][MMM] d, uuuu", Locale.ENGLISH);

    private final List<String> sourceUrls;
    private final String userAgent;
    private final int timeoutMs;
    private final Pattern keywords;

    /** Reads the sources, request settings and infrastructure keywords. */
    public NewsScraper(@Value("${pa-ayo.news.source-urls}") List<String> sourceUrls,
                       @Value("${pa-ayo.news.user-agent}") String userAgent,
                       @Value("${pa-ayo.news.timeout-ms}") int timeoutMs,
                       @Value("${pa-ayo.news.keywords}") List<String> keywords) {
        this.sourceUrls = sourceUrls;
        this.userAgent = userAgent;
        this.timeoutMs = timeoutMs;
        this.keywords = Pattern.compile(keywords.stream().map(Pattern::quote)
                .collect(Collectors.joining("|", "\\b(?:", ")s?\\b")), Pattern.CASE_INSENSITIVE);
    }

    /** Collects articles from every source; a failing source is logged and skipped. */
    public List<NewsItem> scrape() {
        List<NewsItem> items = new ArrayList<>();
        for (String url : sourceUrls) {
            try {
                List<NewsItem> found = parseArticles(fetchPage(url));
                if (found.isEmpty()) {
                    log.warn("No infrastructure news found at {}; check whether its layout changed", url);
                }
                items.addAll(found);
            } catch (IOException | RuntimeException e) {
                log.warn("News source {} skipped: {}", url, e.toString());
            }
        }
        return items;
    }

    /** Downloads one page within the configured timeout. */
    public Document fetchPage(String url) throws IOException {
        return Jsoup.connect(url).userAgent(userAgent).timeout(timeoutMs).get();
    }

    /** Extracts the first infrastructure articles of a listing page; date and thumbnail may be missing. */
    public List<NewsItem> parseArticles(Document doc) {
        Element siteName = doc.selectFirst("meta[property=og:site_name]");
        String host = host(doc.location());
        String sourceName = siteName == null ? host : clean(siteName.attr("content"));

        List<NewsItem> items = new ArrayList<>();
        for (Element article : doc.select(ARTICLE)) {
            // Spam planted on cebu.gov.ph (Nov 2026) is never filed in a category; its real news always is.
            if (article.hasClass("category-uncategorized")) {
                continue;
            }
            Element link = article.selectFirst(TITLE_LINK);
            // Links to other sites (ads, partners) are not this source's news.
            if (link == null || host.isEmpty() || !host.equals(host(link.absUrl("href")))) {
                continue;
            }
            Element excerpt = article.selectFirst(EXCERPT);
            NewsItem item = new NewsItem();
            item.setTitle(clean(link.text()));
            item.setSummary(excerpt == null ? "" : clean(excerpt.text()));
            item.setSourceName(sourceName);
            item.setSourceUrl(link.absUrl("href").split("#")[0]);
            item.setImageUrl(thumbnail(article));
            item.setPublishedDate(date(article));
            if (keywords.matcher(item.getTitle() + " " + item.getSummary()).find()) {
                items.add(item);
            }
            if (items.size() == MAX_ITEMS_PER_SOURCE) {
                break;
            }
        }
        return items;
    }

    /** Returns the body text of one article page, or an empty string if it cannot be read. */
    public String fetchArticleText(String url) {
        try {
            Document page = fetchPage(url);
            for (String selector : BODY_PARAGRAPHS) {
                Elements paragraphs = page.select(selector);
                if (!paragraphs.isEmpty()) {
                    // Lines without lowercase letters are headings, not sentences.
                    return paragraphs.eachText().stream().map(NewsScraper::clean)
                            .filter(text -> !text.equals(text.toUpperCase(Locale.ROOT)))
                            .collect(Collectors.joining(" "));
                }
            }
        } catch (IOException | RuntimeException e) {
            log.warn("Article {} skipped: {}", url, e.toString());
        }
        return "";
    }

    // Some posts use decorative Unicode letters; NFKC turns them into normal ones.
    private static String clean(String text) {
        return Normalizer.normalize(text, Normalizer.Form.NFKC).trim();
    }

    private static String host(String url) {
        try {
            String host = new URI(url).getHost();
            return host == null ? "" : host;
        } catch (URISyntaxException e) {
            return "";
        }
    }

    // Takes the smallest srcset version, because the sidebar shows thumbnails at 76 px.
    private static String thumbnail(Element article) {
        Element image = article.selectFirst("img[src]");
        if (image == null) {
            return null;
        }
        String best = image.attr("src");
        int bestWidth = Integer.MAX_VALUE;
        for (String candidate : image.attr("srcset").split(",\\s+")) {
            String[] parts = candidate.trim().split("\\s+");
            if (parts.length == 2 && parts[1].matches("\\d{1,5}w")) {
                int width = Integer.parseInt(parts[1].replace("w", ""));
                if (width < bestWidth) {
                    bestWidth = width;
                    best = parts[0];
                }
            }
        }
        image.attr("src", best);
        String url = image.absUrl("src");
        return url.startsWith("http") ? url : null;
    }

    private static LocalDate date(Element article) {
        Matcher matcher = DATE_TEXT.matcher(article.text());
        LocalDate date = null;
        // The date line follows the title, so the last match wins over a date inside the title.
        while (matcher.find()) {
            try {
                date = LocalDate.parse(matcher.group(), DATE_FORMAT);
            } catch (DateTimeParseException e) {
                log.debug("Not a date: {}", matcher.group());
            }
        }
        return date;
    }
}
