/*
 * File:        TestEnv.java
 * Module:      Testing
 * Purpose:     Decides whether a missing test dependency (emulator, browser) skips a test or fails it.
 * Responsible: Reuel Matthew Arcilla (QA)
 */
package com.paayo.support;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;

/** Skips tests on a laptop when the Firestore emulator or browser is missing, but fails them in CI. */
public final class TestEnv {
    private TestEnv() {
    }

    /** True when REQUIRE_TEST_INFRA=true (set by the CI workflow) so a missing emulator cannot pass silently. */
    public static boolean infrastructureRequired() {
        return "true".equalsIgnoreCase(System.getenv("REQUIRE_TEST_INFRA"));
    }

    /** Aborts (skips) the test locally; fails it when infrastructure is required. */
    public static void skipOrFail(String reason) {
        if (infrastructureRequired()) {
            Assertions.fail(reason);
        }
        Assumptions.abort(reason);
    }
}
