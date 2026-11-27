/*
 * File:        AbstractFirestoreIT.java
 * Module:      Testing
 * Purpose:     Base class for integration tests (*IT): full Spring context on a clean Firestore emulator.
 * Responsible: Reuel Matthew Arcilla (QA)
 */
package com.paayo.support;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Extend this for repository and service integration tests. The Spring context points at the
 * emulator, the database is emptied before every test, and the class is skipped (locally) when
 * the emulator is not running. Run with: mvn verify -Pintegration
 */
@SpringBootTest(properties = {
        "pa-ayo.firebase.emulator-host=" + FirestoreEmulator.HOST_PORT,
        "pa-ayo.firebase.project-id=" + FirestoreEmulator.PROJECT_ID,
        "pa-ayo.firebase.credentials-path="
})
@ActiveProfiles("test")
public abstract class AbstractFirestoreIT {

    @BeforeAll
    static void requireEmulator() {
        if (!FirestoreEmulator.isRunning()) {
            TestEnv.skipOrFail(FirestoreEmulator.START_HINT);
        }
    }

    @BeforeEach
    void wipeDatabase() {
        FirestoreEmulator.clearAll();
    }
}
