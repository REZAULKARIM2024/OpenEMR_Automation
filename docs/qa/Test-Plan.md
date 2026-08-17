# Test Plan — OpenEMR Automation Framework

| | |
|---|---|
| **Project** | OpenEMR Automation Framework (`REZAULKARIM2024/OpenEMR_Automation`) |
| **Document owner** | Rezaul Karim |
| **Status** | Living document — update alongside the suite, not after it |
| **Related artifacts** | `Test-Case-Register.xlsx`, `Traceability-Matrix.xlsx`, `Defect-Log.xlsx` (this folder) |

## 1. Objective

Verify that OpenEMR's core patient-facing workflows (login, patient registration, navigation,
session/security behavior) and its Standard REST API behave correctly, consistently, and
securely — through automated regression coverage that runs on every push, on a nightly
schedule, and on demand.

## 2. Scope

**In scope**
- UI regression: login, patient registration/search, navigation, session lifecycle, browser/
  device behavior, accessibility basics, security/negative-input handling (`login.feature`,
  `patient.feature`, `navigation.feature`, `admin.feature`, `browser_behavior.feature`).
- API regression: OAuth2 discovery and dynamic client registration, Standard REST API
  authentication/authorization errors, authenticated Patient-resource CRUD and data-contract
  checks (`api.feature`, `standard_api.feature`).
- Cross-browser (Chrome, Firefox) and multi-viewport execution of the smoke subset.
- CI/CD integration: GitHub Actions runs the suite on push, PR, nightly cron, and manual dispatch.

**Out of scope (for now)**
- FHIR API coverage (base URL and config are in place via `ConfigReader.getFhirBaseUrl()`, no
  scenarios written yet).
- Direct database-level data/ETL validation — see `Data-ETL-Testing.md` in this folder for why
  that needs a self-hosted instance rather than the shared public demo, and its current state.
- Load/stress testing beyond the single-request response-time budgets already checked.
- Mobile native app testing (there isn't one — OpenEMR here is the web application only).

## 3. Test Environment

| | |
|---|---|
| **Primary target** | `https://demo.openemr.io/openemr` — OpenEMR's shared public demo |
| **Credentials** | `admin` / `pass` (documented default; shared instance, resets periodically) |
| **Override mechanism** | Every URL/credential is a `-D` system property via `ConfigReader` (`base.url`, `dashboard.url`, `api.username`, etc.) — pointing the whole suite at a self-hosted instance requires no code changes |
| **Browsers** | Chrome (primary), Firefox (`cross-browser` CI job) |
| **CI** | GitHub Actions, `.github/workflows/ci.yml` — jobs: `smoke` → `regression` + `cross-browser` (the latter two gated on `smoke` passing) |

**Known environment constraint:** the shared public demo is a third party's instance we don't
control. It has, on at least two occasions during this project, intermittently rejected the
documented default credentials for periods of time (see `DEF-004` in `Defect-Log.xlsx`) and it
does not have OAuth2 Password Grant enabled by default. The test strategy below is written
explicitly around that constraint rather than pretending it doesn't exist.

## 4. Test Strategy by Level

| Level | Tooling | Runner(s) | Runs by default in CI? |
|---|---|---|---|
| UI functional/regression | Selenium 4 + Cucumber 7 + TestNG | `TestRunner`, `SmokeRunner`, `RegressionRunner`, `SecurityRunner`, `NegativeRunner`, etc. | Yes |
| API — transport & unauthenticated | RestAssured | `ApiRunner` | Yes (`@api and not @requires-password-grant`) |
| API — authenticated (OAuth2 Password Grant) | RestAssured + `ApiAuthHelper` | `AuthenticatedApiRunner` | **No** — opt-in only, see §5 |
| Pre-flight environment health check | Selenium (single login attempt) | `PreflightHealthCheck` via `testng-preflight.xml` | Yes, before the `smoke` job |
| Data/ETL | JDBC | `DataQualityRunner` (see `Data-ETL-Testing.md`) | **No** — opt-in only, needs direct DB access |

## 5. Why some suites are excluded from the default run

This project treats "runs in CI by default" and "exists in the framework" as two different
questions, on purpose:

- **`PreflightHealthCheck`** exists because a shared demo outage used to burn the full ~5-minute
  smoke suite before failing with a confusing multi-scenario spray. It now fails in ~20-30s with
  a specific diagnosis (see `utils/PreflightHealthCheck.java`).
- **`@requires-password-grant` API scenarios** are excluded from `TestRunner` and `ApiRunner`
  (see the `tags` attribute on each) because OAuth2 Password Grant is off by default on OpenEMR
  and explicitly "not recommended for production" by OpenEMR's own docs. Forcing them into the
  default run would mean the regression job goes red every time it runs against an instance
  that hasn't enabled that toggle — noise, not signal. Run them deliberately with
  `AuthenticatedApiRunner` / `-Dcucumber.filter.tags="@requires-password-grant"` against an
  instance known to support it.
- **Data/ETL checks** need direct MySQL access that the public demo doesn't expose. They're
  written and ready but only meaningful against a self-hosted instance — see
  `Data-ETL-Testing.md`.

The rule of thumb: anything that depends on a third party's configuration, rather than on this
framework's own code, gets its own opt-in runner instead of being folded into the suites that
gate CI.

## 6. Entry / Exit Criteria

**Entry (before a CI run is considered meaningful)**
- `PreflightHealthCheck` passes — confirms the target instance is currently reachable and
  accepting the configured credentials.
- No open `Critical`/`Highest`-priority defects in `Defect-Log.xlsx` affecting the area under test.

**Exit (before merging a change to `main`)**
- `smoke` job green.
- `regression` and `cross-browser` jobs green, or any failure triaged and logged in
  `Defect-Log.xlsx` with a clear root cause (framework bug vs. environment issue).
- New behavior has a corresponding scenario in the relevant `.feature` file, a row in
  `Test-Case-Register.xlsx`, and a link in `Traceability-Matrix.xlsx`.

## 7. Test Deliverables

| Artifact | Purpose |
|---|---|
| `Test-Case-Register.xlsx` | Every written scenario, with priority, type, component, and automation status. Includes a live Dashboard tab. |
| `Traceability-Matrix.xlsx` | Requirement → test case → feature file → defect, so coverage gaps are visible at a glance. |
| `Defect-Log.xlsx` | Jira/Zephyr-style defect tracker, seeded with this project's real defect history. |
| `target/cucumber-report.html`, `target/allure-report/`, `target/site/jacoco/` | Generated per CI run — functional results, Allure reporting, code coverage. |
| GitHub Actions run history | Historical pass/fail trend, artifacts retained per run. |

## 8. Roles

Single-maintainer project at this stage — one person acts as test designer, automation
engineer, and triager. The structure above (tagged runners, a defect log, a traceability
matrix) is deliberately the same structure a multi-person QA team would use, so it scales if
that changes.

## 9. Risks & Mitigations

| Risk | Mitigation |
|---|---|
| Shared public demo instability (outages, credential resets) | `PreflightHealthCheck`, `ConfigReader` override hooks to retarget at a self-hosted instance |
| OAuth2 Password Grant disabled on target instance | `@requires-password-grant` tag isolation, `ApiAuthHelper` fails with a specific diagnostic rather than a generic 400/401 |
| Flaky UI timing (page not yet interactive) | Explicit `WebDriverWait` conditions throughout `stepdefinitions`, `RetryTransformer` for one automatic retry on transient failures |
| Schema/markup drift on the target OpenEMR version | Locators kept as loose as correctness allows (e.g. `contains(@class,'menuLabel')`), `DiagnosticsHelper` captures page state on every failure |
