# Testing guide

How to run and write tests for Pa-Ayo. Owner: QA (Reuel). Test cases come from the project proposal (TC-01 to TC-17).

## Three layers

| Layer | Files | Runs with | Needs |
|---|---|---|---|
| Unit / web layer | `*Test.java` | `mvn test` | Java 21 only |
| Integration (Firestore) | `*IT.java` extending `AbstractFirestoreIT` | `mvn verify -Pintegration` | Firestore emulator |
| End-to-end (browser) | `*E2EIT.java` extending `AbstractE2EIT` | `mvn verify -Pintegration` | Emulator + Playwright Chromium |

Maven's `surefire` runs `*Test`, `failsafe` runs `*IT`. Slow tests go in `*IT` so `mvn test` stays fast.

## Commands

```bash
# 1. Unit tests + the 80% coverage gate (no setup needed)
mvn test
start target\site\jacoco\index.html        # Windows (Mac: open ...)

# 2. One-time: install the Chromium used by the browser tests
mvn exec:java -e -Dexec.classpathScope=test -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install chromium"

# 3. Everything, with the emulator started and stopped automatically
npx -y firebase-tools@15 emulators:exec --only firestore --project demo-pa-ayo "mvn verify -Pintegration"

# Run the tests of ONE test case (every test is tagged with its TC number)
mvn test -Dgroups=TC-07
mvn verify -Pintegration -Dgroups=TC-12

# Run one class
mvn test -Dtest=FirestoreTypesTest
mvn verify -Pintegration -Dit.test=FirestoreSmokeIT

# Live smoke test of the deployed site (TC-15), read-only
mvn verify -Pintegration -Dit.test=RenderSmokeIT -Dsmoke.url=https://YOUR-APP.onrender.com
```

If the emulator already runs for development (VS Code task "Start local database"), just run `mvn verify -Pintegration`.
Tests use the separate project id `demo-pa-ayo-test`, so they **never wipe your seed data**.
Without the emulator or browser, `*IT` tests are **skipped locally** with a hint; in CI they **fail** instead.

## What the console shows

`TestCaseReporter` (registered in `src/test/resources/META-INF/services/`) prints every test with its status, then a
summary per test case. Example:

```
  ReportServiceTest
    PASS  TC-01 valid SANITATION report, no photo: saved ...  [TC-01]  (12 ms)
    FAIL  TC-03 rejected server-side: 101-char title  [TC-03]  (4 ms)  - Expecting value to be false but was true
    SKIP  TC-16(a) ...  [TC-16]  (0 ms)  - Firestore emulator is not running ...

  ===== TEST CASE SUMMARY =====
  TC-01   PASS  1 passed, 0 failed, 0 skipped
  TC-03   FAIL  5 passed, 1 failed, 0 skipped
```

Use the summary rows to fill the **Actual Result / Status** columns of the Test Case Matrix. A test case only shows up
once its `.template` file has been activated (see above). It needs `junit-platform-launcher` in `pom.xml` (test scope).

## Test files waiting for their code (`*.java.template`)

Files ending in `.template` are **not compiled**, so `mvn test` stays green while the code is still being written.
When the class a template tests is merged:

1. Rename it: remove `.template` from the file name.
   Windows (cmd): `ren ReportServiceTest.java.template ReportServiceTest.java`
   Mac/Linux: `mv ReportServiceTest.java.template ReportServiceTest.java`
2. Fix the lines marked `TODO` (package names, constructor arguments, exception types). They follow the proposal's class diagram.
3. Run `mvn test` (or `mvn verify -Pintegration` for `*IT`) and fix what is red. A failing test can be a real bug.

## Test cases -> test files

Every test carries `@Tag("TC-xx")` and a display name starting with its TC number.

| Test case | Requirements | Where it is tested | Status |
|---|---|---|---|
| TC-01 valid report, no photo | FR-01..04 | `service/ReportServiceTest`, `controller/ReportControllerTest`, `repository/ReportRepositoryIT`, `e2e/ReportFormE2EIT`, `e2e/GridE2EIT` | template |
| TC-02 empty fields blocked | FR-08 | `service/ReportServiceTest`, `model/ReportTest`, `controller/ReportControllerTest`, `e2e/ReportFormE2EIT` | template |
| TC-03 whitespace and length limits | FR-08, FR-03 | `service/ReportServiceTest`, `model/ReportTest`, `repository/ReportRepositoryIT`, `e2e/ReportFormE2EIT` | template |
| TC-04 category filter | FR-02, FR-09 | `controller/ReportControllerTest`, `e2e/GridE2EIT` | template |
| TC-05 admin status flow | FR-05 | `model/ReportTest`, `service/ReportServiceTest`, `repository/ReportRepositoryIT`, `controller/AdminControllerTest`, `e2e/AdminFlowE2EIT` | template |
| TC-06 admin access control | FR-05, NFR-07 | `controller/AdminControllerTest`, `service/AdminAuthServiceTest`, `e2e/AdminFlowE2EIT` | template |
| TC-07 news bot publishes >= 2 | FR-06, FR-07 | `service/NewsServiceTest`, `newsbot/NewsScraperTest`, `repository/NewsRepositoryIT` | template |
| TC-08 no duplicate news | FR-06, FR-07, NFR-11 | `service/NewsServiceTest`, `repository/NewsRepositoryIT` | template |
| TC-09 no date, no thumbnail | FR-06 | `newsbot/NewsScraperTest` (+ `fixtures/news/article-no-date-no-thumbnail.html`) | template |
| TC-10 source down, slow or changed | FR-06, NFR-03 | `newsbot/NewsScraperTest` (local test server + fixtures), `service/NewsServiceTest` | template |
| TC-11 paraphraser limits | FR-07 | `newsbot/TextParaphraserTest` | template |
| TC-12 30+ reports, 4/2/1 columns | FR-03, FR-04, NFR-04 | `e2e/GridE2EIT` (+ `support/SeedData`), `repository/ReportRepositoryIT` | template |
| TC-13 80% coverage | FR-10 | JaCoCo gate in `pom.xml`, runs in `mvn test` and CI | **active** |
| TC-14 XSS, CSRF, privacy notice | NFR-09, NFR-10 | `controller/ReportControllerTest`, `e2e/ReportFormE2EIT` | template |
| TC-15 live HTTPS smoke test | FR-11, NFR-05 | `e2e/RenderSmokeIT` (read-only) + manual submit and admin login | **active** (needs `-Dsmoke.url`) |
| TC-16 photo upload rules | FR-12 | `service/ImageStorageServiceTest`, `service/ReportServiceTest`, `e2e/ReportFormE2EIT` | template |
| TC-17 Forgot Password | FR-13, NFR-06 | `service/AdminAuthServiceTest`, `controller/AdminControllerTest`, `e2e/AdminFlowE2EIT` | template |

Already active today: `util/FirestoreTypesTest` (NFR-16), `config/FirebaseConfigTest`, `support/FixturesTest`,
`support/FirestoreSmokeIT`, `e2e/HomePageE2EIT`, `e2e/RenderSmokeIT`.

TC-15 steps that must stay manual (they change real data): submit one report with and without a photo, log in as admin,
change a status, and press "Fetch Latest News Now". Record the result in the Test Case Matrix of the proposal.

## Folder layout

```
src/test/java/com/paayo/
  support/      AbstractFirestoreIT, AbstractE2EIT, FirestoreEmulator, Fixtures, SeedData, TestEnv
  util/ config/ model/ service/ newsbot/ repository/ controller/ e2e/
src/test/resources/
  application-test.properties   test settings (profile "test")
  fixtures/news/                saved HTML pages for offline scraper tests
```

## Rules of thumb

- **Mock** Firestore, Cloudinary and mail in unit tests (NFR-15). Use the emulator only in `*IT`.
- **Never** call real government sites in tests. Parse `Fixtures.document("news/...")` or use the local test server.
- Unit-test the four gated classes first (`ReportService`, `NewsService`, `NewsScraper`, `TextParaphraser`): the build fails under 80%.
- Controller tests: `@WebMvcTest` + `@MockitoBean` services + `spring-security-test` (`csrf()`, `@WithMockUser`).
- Browser tests need fixed seed data, so use `SeedData`, not the app's own save code.

## CI

`.github/workflows/ci.yml` runs on every pull request: job **unit** (`mvn test` with the coverage gate), then job
**integration** (emulator + Chromium + `mvn verify -Pintegration`). Turn on *Settings > Branches > Require status checks*
for `main` so a failing test blocks the merge.
