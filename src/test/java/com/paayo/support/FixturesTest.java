/*
 * File:        FixturesTest.java
 * Module:      Testing
 * Purpose:     Checks that the saved news pages exist and parse, so scraper tests never fail on a missing file.
 * Responsible: Reuel Matthew Arcilla (QA)
 */
package com.paayo.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.jsoup.nodes.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class FixturesTest {

    @ParameterizedTest(name = "fixture loads and parses: {0}")
    @ValueSource(strings = {
            "news/listing-with-articles.html",
            "news/article-no-date-no-thumbnail.html",
            "news/changed-layout.html",
            "news/empty-listing.html"
    })
    void everyNewsFixtureLoadsAndParses(String path) {
        Document doc = Fixtures.document(path);

        assertThat(doc.body().text()).isNotBlank();
    }

    @Test
    @DisplayName("listing fixture has 3 articles, 2 thumbnails, 2 dates")
    void listingFixtureHasThreeArticlesAndTwoThumbnails() {
        Document doc = Fixtures.document("news/listing-with-articles.html");

        assertThat(doc.select("article")).hasSize(3);
        assertThat(doc.select("article img")).hasSize(2);
        assertThat(doc.select("article time")).hasSize(2);
    }

    @Test
    @DisplayName("no-date fixture has no date and no image")
    void noDateFixtureReallyHasNoDateAndNoImage() {
        Document doc = Fixtures.document("news/article-no-date-no-thumbnail.html");

        assertThat(doc.select("article")).hasSize(1);
        assertThat(doc.select("time")).isEmpty();
        assertThat(doc.select("img")).isEmpty();
    }

    @Test
    @DisplayName("changed-layout fixture has no article elements")
    void changedLayoutFixtureHasNoArticleElements() {
        assertThat(Fixtures.document("news/changed-layout.html").select("article")).isEmpty();
    }

    @Test
    @DisplayName("a missing fixture gives a clear error")
    void missingFixtureGivesAClearError() {
        assertThatThrownBy(() -> Fixtures.read("news/does-not-exist.html"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Missing test fixture");
    }
}
