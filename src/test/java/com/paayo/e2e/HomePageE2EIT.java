/*
 * File:        HomePageE2EIT.java
 * Module:      Testing
 * Purpose:     First browser test: the app starts and serves a page. Replace with real S1-S4 flows as screens land.
 * Responsible: Reuel Matthew Arcilla (QA)
 */
package com.paayo.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import com.microsoft.playwright.Response;
import com.paayo.support.AbstractE2EIT;
import org.junit.jupiter.api.Test;

class HomePageE2EIT extends AbstractE2EIT {

    @Test
    void applicationServesAPageWithoutServerError() {
        Response response = page.navigate(baseUrl() + "/");

        assertThat(response).isNotNull();
        assertThat(response.status()).isLessThan(500);
    }

    @Test
    void pageIsUsableOnMobileWidth() {
        useViewport(MOBILE);

        Response response = page.navigate(baseUrl() + "/");

        assertThat(response).isNotNull();
        assertThat(response.status()).isLessThan(500);
        // Nothing may force sideways scrolling on a phone.
        Object scrollWidth = page.evaluate("document.documentElement.scrollWidth");
        assertThat(((Number) scrollWidth).intValue()).isLessThanOrEqualTo(MOBILE[0]);
    }
}
