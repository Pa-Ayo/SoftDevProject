/*
 * File:        AbstractE2EIT.java
 * Module:      Testing
 * Purpose:     Base class for browser tests: real server on a random port, Playwright Chromium, clean emulator.
 * Responsible: Reuel Matthew Arcilla (QA)
 */
package com.paayo.support;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

/**
 * Extend this for end-to-end tests. Each test gets a fresh page at desktop size; call
 * {@link #useViewport(int, int)} to check the responsive grid (4 / 2 / 1 columns).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "pa-ayo.firebase.emulator-host=" + FirestoreEmulator.HOST_PORT,
        "pa-ayo.firebase.project-id=" + FirestoreEmulator.PROJECT_ID,
        "pa-ayo.firebase.credentials-path="
})
@ActiveProfiles("test")
public abstract class AbstractE2EIT extends AbstractFirestoreIT {
    /** 4-column grid (>= 1200 px). */
    protected static final int[] DESKTOP = {1280, 800};
    /** 2-column grid (768 to 1199 px). */
    protected static final int[] TABLET = {1024, 768};
    /** 1-column grid (< 768 px). */
    protected static final int[] MOBILE = {375, 667};

    private static Playwright playwright;
    private static Browser browser;

    @LocalServerPort
    private int port;

    protected BrowserContext context;
    protected Page page;

    @BeforeAll
    static void launchBrowser() {
        try {
            playwright = Playwright.create();
            browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
        } catch (RuntimeException e) {
            closeBrowser();
            TestEnv.skipOrFail("Playwright Chromium is not available (" + e.getMessage() + "). Install it with: "
                    + "mvn exec:java -e -Dexec.classpathScope=test -Dexec.mainClass=com.microsoft.playwright.CLI "
                    + "-Dexec.args=\"install chromium\"");
        }
    }

    @AfterAll
    static void closeBrowser() {
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
        context = browser.newContext(new Browser.NewContextOptions().setViewportSize(DESKTOP[0], DESKTOP[1]));
        page = context.newPage();
        page.setDefaultTimeout(10_000);
    }

    @AfterEach
    void closePage() {
        if (context != null) {
            context.close();
        }
    }

    /** Resizes the browser, e.g. useViewport(MOBILE). */
    protected void useViewport(int[] size) {
        page.setViewportSize(size[0], size[1]);
    }

    /** Address of the running test server, e.g. http://localhost:51234 */
    protected String baseUrl() {
        return "http://localhost:" + port;
    }
}
