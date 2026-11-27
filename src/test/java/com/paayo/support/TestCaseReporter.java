/*
 * File:        TestCaseReporter.java
 * Module:      Testing
 * Purpose:     Prints every test by name with PASS / FAIL / SKIP in the Maven console, then a summary per
 *              test case (TC-xx). Registered automatically through META-INF/services, no setup needed.
 * Responsible: Reuel Matthew Arcilla (QA)
 */
package com.paayo.support;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestTag;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

/** Console report: one line per test, plus a table with one row per test case (tag "TC-xx"). */
public class TestCaseReporter implements TestExecutionListener {
    private static final String PASS = "PASS";
    private static final String FAIL = "FAIL";
    private static final String SKIP = "SKIP";

    private final Map<String, Long> startedAt = new HashMap<>();
    /** test case id -> {passed, failed, skipped} */
    private final Map<String, int[]> perCase = new TreeMap<>();
    private int passed;
    private int failed;
    private int skipped;

    @Override
    public synchronized void testPlanExecutionStarted(TestPlan testPlan) {
        passed = 0;
        failed = 0;
        skipped = 0;
        perCase.clear();
    }

    @Override
    public synchronized void executionStarted(TestIdentifier id) {
        startedAt.put(id.getUniqueId(), System.nanoTime());
        if (id.isContainer() && id.getSource().filter(ClassSource.class::isInstance).isPresent()) {
            ClassSource source = (ClassSource) id.getSource().get();
            System.out.println();
            System.out.println("  " + source.getJavaClass().getSimpleName());
        }
    }

    @Override
    public synchronized void executionSkipped(TestIdentifier id, String reason) {
        if (id.isTest()) {
            record(id, SKIP, 0, reason);
        }
    }

    @Override
    public synchronized void executionFinished(TestIdentifier id, TestExecutionResult result) {
        long millis = elapsedMillis(id);
        if (id.isTest()) {
            switch (result.getStatus()) {
                case SUCCESSFUL -> record(id, PASS, millis, null);
                case ABORTED -> record(id, SKIP, millis, message(result));
                default -> record(id, FAIL, millis, message(result));
            }
        } else if (result.getStatus() == TestExecutionResult.Status.FAILED) {
            // a failure in @BeforeAll / @AfterAll / context start-up: none of its tests ran
            failed++;
            System.out.println("    " + FAIL + "  " + id.getDisplayName() + " (setup or teardown)  " + message(result));
        } else if (result.getStatus() == TestExecutionResult.Status.ABORTED && id.getSource()
                .filter(ClassSource.class::isInstance).isPresent()) {
            skipped++;
            System.out.println("    " + SKIP + "  whole class skipped  " + message(result));
        }
    }

    @Override
    public synchronized void testPlanExecutionFinished(TestPlan testPlan) {
        if (!perCase.isEmpty()) {
            System.out.println();
            System.out.println("  ===== TEST CASE SUMMARY =====");
            perCase.forEach((tc, n) -> System.out.printf("  %-6s  %-4s  %d passed, %d failed, %d skipped%n",
                    tc, n[1] > 0 ? FAIL : (n[0] > 0 ? PASS : SKIP), n[0], n[1], n[2]));
        }
        System.out.println();
        System.out.printf("  Totals: %d passed, %d failed, %d skipped%n%n", passed, failed, skipped);
    }

    private void record(TestIdentifier id, String status, long millis, String note) {
        Set<String> cases = id.getTags().stream().map(TestTag::getName)
                .filter(name -> name.startsWith("TC-")).collect(Collectors.toCollection(java.util.TreeSet::new));
        int slot = PASS.equals(status) ? 0 : FAIL.equals(status) ? 1 : 2;
        for (String tc : cases) {
            perCase.computeIfAbsent(tc, k -> new int[3])[slot]++;
        }
        if (slot == 0) {
            passed++;
        } else if (slot == 1) {
            failed++;
        } else {
            skipped++;
        }
        String tags = cases.isEmpty() ? "" : "  [" + String.join(", ", cases) + "]";
        String suffix = (note == null || note.isBlank()) ? "" : "  - " + note;
        System.out.printf("    %-4s  %s%s  (%d ms)%s%n", status, id.getDisplayName(), tags, millis, suffix);
    }

    private long elapsedMillis(TestIdentifier id) {
        Long start = startedAt.remove(id.getUniqueId());
        return start == null ? 0 : (System.nanoTime() - start) / 1_000_000;
    }

    private static String message(TestExecutionResult result) {
        String text = result.getThrowable().map(t -> String.valueOf(t.getMessage())).orElse("");
        text = text.replaceAll("\\s+", " ").trim();
        return text.length() > 160 ? text.substring(0, 157) + "..." : text;
    }
}
