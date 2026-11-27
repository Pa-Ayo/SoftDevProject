/*
 * File:        RenderSmokeIT.java
 * Module:      Testing
 * Purpose:     TC-15: read-only smoke test of the LIVE site (Render, HTTPS). It is skipped unless you give it a URL:
 *                mvn verify -Pintegration -Dit.test=RenderSmokeIT -Dsmoke.url=https://your-app.onrender.com
 *              Submitting a report and the admin login are NOT done here (they change real data): do them by hand.
 * Responsible: Reuel Matthew Arcilla (QA)
 */
package com.paayo.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Response;
import com.paayo.support.TestEnv;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("TC-15")
class RenderSmokeIT {
    private static Playwright playwright;
    private static Browser browser;
    private static String baseUrl;

    private BrowserContext context;
    private Page page;

    @BeforeAll
    static void start() {
        String url = System.getProperty("smoke.url", System.getenv().getOrDefault("SMOKE_URL", ""));
        assumeTrue(!url.isBlank(), "Skipped: pass -Dsmoke.url=https://<your-app>.onrender.com to run the live smoke test");
        baseUrl = url.replaceAll("/+$", "");
        try {
            playwright = Playwright.create();
            browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
        } catch (RuntimeException e) {
            stop();
            TestEnv.skipOrFail("Playwright Chromium is not available: " + e.getMessage());
        }
    }

    @AfterAll
    static void stop() {
        if (browser != null) {
            browser.close();
            browser = null;
        }
        if (playwright != null) {
            playwright.close();
            playwright = null;
        }
    }

    @BeforeEach
    void openPage() {
        context = browser.newContext();
        page = context.newPage();
        page.setDefaultTimeout(30_000);
        page.setDefaultNavigationTimeout(90_000);   // the free tier can take a minute to wake up
    }

    @AfterEach
    void closePage() {
        if (context != null) {
            context.close();
        }
    }

    @Test
    @DisplayName("TC-15 the site is served over HTTPS and the dashboard loads")
    void dashboardLoadsOverHttps() {
        assertThat(baseUrl).startsWith("https://");

        Response response = page.navigate(baseUrl + "/");

        assertThat(response).isNotNull();
        assertThat(response.status()).isLessThan(400);
        page.locator(".report-grid").waitFor();      // the 4-column grid (S1)
        page.locator(".news-sidebar").waitFor();     // the DPWH & Gov Updates sidebar (S1)
    }

    @Test
    @DisplayName("TC-15 the report form opens and shows the privacy notice")
    void reportFormOpens() {
        Response response = page.navigate(baseUrl + "/reports/new");

        assertThat(response).isNotNull();
        assertThat(response.status()).isLessThan(400);
        page.locator("#reportForm").waitFor();
        assertThat(page.locator(".privacy-banner").innerText()).contains("Prototype System");
    }

    @Test
    @DisplayName("TC-15 login and Forgot Password pages open")
    void adminPublicPagesOpen() {
        page.navigate(baseUrl + "/admin/login");
        page.locator("#username").waitFor();

        page.navigate(baseUrl + "/admin/forgot-password");
        page.locator("#identifier").waitFor();
    }

    @Test
    @Tag("TC-06")
    @DisplayName("TC-15 the admin dashboard is protected: visitors are sent to the login page")
    void adminDashboardIsProtected() {
        page.navigate(baseUrl + "/admin/dashboard");

        assertThat(page.url()).contains("/admin/login");
    }
}
