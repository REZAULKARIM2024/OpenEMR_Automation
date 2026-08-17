# OpenEMR_Automation – BDD Cucumber Test Automation Framework

A robust and scalable UI automation framework built for the OpenEMR demo application. This project demonstrates advanced QA automation practices using Behavior-Driven Development (BDD), focusing on healthcare workflows, secure authentication, and reliable UI validation.

🔗 **Application Under Test:** https://demo.openemr.io/openemr/interface/login/login.php

---

## Table of Contents

* [Overview](#overview)
* [Screenshots](#screenshots)
* [Features](#features)
* [Tech Stack](#tech-stack)
* [Prerequisites](#prerequisites)
* [Installation](#installation)
* [Configuration](#configuration)
* [Running the Application](#running-the-application)
* [Default Login](#default-login)
* [Project Structure](#project-structure)
* [Database Overview](#database-overview)
* [Seed / Utility Scripts](#seed--utility-scripts)
* [Testing](#testing)
* [Test Architecture: Tagging & Parallel Execution](#test-architecture-tagging--parallel-execution)
* [Code Coverage (JaCoCo)](#code-coverage-jacoco)
* [Test Reporting (Allure)](#test-reporting-allure)
  * [Cucumber report](#cucumber-report-targetcucumber-reporthtml)
* [BDD & E2E Testing (Cucumber, Selenium)](#bdd--e2e-testing-cucumber-selenium)
* [REST API](#rest-api)
* [Data & ETL Testing](#data--etl-testing)
* [Test Management & QA Documentation](#test-management--qa-documentation)
* [Web Admin Dashboard](#web-admin-dashboard)
* [Running with Docker](#running-with-docker)
* [CI/CD Pipeline](#cicd-pipeline)
* [Test Strategy](#test-strategy)
* [Roadmap](#roadmap)
* [Contributing](#contributing)
* [License](#license)
* [Contact](#contact)

---

## Overview

**OpenEMR_Automation** is designed to automate critical workflows of an electronic medical record (EMR) system. It simulates real-world healthcare scenarios such as user login, patient management, and navigation across medical modules, driving the public OpenEMR demo instance end-to-end through the browser.

This repository is a **test automation framework**, not the application under test — it doesn't ship its own server, database, or UI. Everything it exercises lives on OpenEMR's demo deployment. That distinction matters for a few sections below (Database Overview, REST API, Web Admin Dashboard) which are written from that perspective rather than describing infrastructure this repo owns.

---

## Screenshots

No live screenshots are checked into this README — they weren't captured against a running browser session, and placeholder images would be misleading in a testing repository. Two concrete, real artifacts exist instead:

* `Cucumber.pdf` and `OpenEMR Cucumber Test Report.pdf` in the repo root — exported HTML-to-PDF snapshots of a prior Cucumber run.
* Automatic failure screenshots: `Hooks.java` attaches a PNG to the Cucumber report for any scenario that fails (`scenario.attach(..., "image/png", "Failure Screenshot")`), visible in `target/cucumber-report.html` after a run.

To generate fresh screenshots for this section, run the suite locally (`mvn clean test`) and open `target/cucumber-report.html` or the Allure report (see [Test Reporting (Allure)](#test-reporting-allure)) — both render inline images for any failures, and you can crop a passing run's dashboard view from there. See [Cucumber report](#cucumber-report-targetcucumber-reporthtml) for the exact scenario/step breakdown from the latest verified 79/79 green run, and [Latest Verified Runs](#testing) for the corresponding `mvn clean test` console output.

---

## Features

* ✔️ BDD implementation with Cucumber (Gherkin syntax) — 47 written scenarios (~90 with `Examples` expansion) across 8 feature files
* ✔️ Page Object Model (POM) for clean and maintainable code
* ✔️ Reusable step definitions and hooks
* ✔️ Parameterized locators for dynamic UI elements
* ✔️ TestNG integration for flexible, tag-based execution
* ✔️ Cross-browser support (Chrome / Firefox) via `-Dbrowser`
* ✔️ Headless execution via `-Dheadless=true` (auto-enabled under CI)
* ✔️ Parallel scenario execution via TestNG's DataProvider
* ✔️ REST Assured API checks: transport smoke + real OAuth2-authenticated Standard REST API coverage (registration, tokens, CRUD, error handling)
* ✔️ Opt-in JDBC data-quality checks (null/duplicate/referential-integrity/reconciliation) against a self-hosted instance
* ✔️ Jira/Zephyr-style test management docs (`docs/qa/`) — test plan, test case register, traceability matrix, defect log
* ✔️ JaCoCo code coverage and Allure HTML reporting
* ✔️ GitHub Actions CI/CD: pre-flight health check → smoke → full regression → cross-browser matrix, nightly scheduled run
* ✔️ Dockerized execution
* ✔️ Maven for dependency and build management

---

## Tech Stack

* **Language:** Java 11
* **Automation Tool:** Selenium WebDriver 4 (Chrome + Firefox)
* **BDD Framework:** Cucumber (Gherkin) 7
* **Test Runner:** TestNG
* **Build Tool:** Maven
* **API checks:** REST Assured (transport smoke + OAuth2-authenticated Standard REST API)
* **Data/ETL checks:** JDBC (MySQL Connector/J), opt-in against a self-hosted instance
* **Coverage:** JaCoCo
* **Reporting:** Cucumber HTML/JSON, Allure
* **Containerization:** Docker
* **CI/CD:** GitHub Actions
* **IDE:** Eclipse / IntelliJ

---

## Prerequisites

* Java (JDK 11 or higher)
* Maven 3.6+
* Eclipse / IntelliJ IDE (optional — `.classpath`/`.project` are already checked in for Eclipse)
* Chrome and/or Firefox (drivers are auto-managed by WebDriverManager)
* Docker (optional, only needed for [Running with Docker](#running-with-docker))

---

## Installation

```bash
git clone https://github.com/REZAULKARIM2024/OpenEMR_Automation.git
cd OpenEMR_Automation
mvn -q dependency:go-offline   # downloads and caches all dependencies
```

No build step beyond dependency resolution is required — there's no application server to compile or deploy; `mvn test` compiles the test sources and runs them directly.

---

## Configuration

All environment-specific values are centralized in `utils/ConfigReader.java` and overridable with JVM system properties. Nothing needs to be recompiled to point the suite at a different environment:

| Property | Default | Purpose |
|---|---|---|
| `base.url` | `https://demo.openemr.io/openemr/interface/login/login.php` | Login page the suite drives |
| `dashboard.url` | `https://demo.openemr.io/openemr/interface/main/tabs/main.php` | Used by the unauthenticated-access security checks |
| `browser` | `chrome` | `chrome` or `firefox` |
| `headless` | `false` (`true` automatically when the `CI` env var is set) | Headless browser execution |
| `timeout.seconds` | `20` | Default explicit-wait timeout |
| `oauth2.base.url` / `api.base.url` / `fhir.base.url` | derived from `base.url` | OAuth2, Standard API, and FHIR API base URLs — see [REST API](#rest-api) |
| `api.username` / `api.password` | `admin` / `pass` | Credentials used for the OAuth2 Password Grant |
| `db.enabled` | `false` | Explicit opt-in required before any Data/ETL check attempts a connection |
| `db.host` / `db.port` / `db.name` / `db.user` / `db.password` | `localhost` / `3306` / `openemr` / `openemr` / `openemr` | Direct MySQL connection for `etl.DataQualityChecks` — see [Data & ETL Testing](#data--etl-testing) |

Example:

```bash
mvn test -Dbase.url=https://my-openemr-instance/interface/login/login.php -Dbrowser=firefox -Dheadless=true
```

---

## Running the Application

"The application" here means the automated test run itself — there's no standalone server to start first.

```bash
mvn clean test                                          # full 79-scenario suite + unit tests
mvn test -DsuiteXmlFile=testng-smoke.xml                 # smoke only
mvn test -Dcucumber.filter.tags="@security"              # any tag, ad hoc
mvn test -DsuiteXmlFile=testng-parallel.xml              # parallel execution
```

Or run any of the category-specific runner classes (`SmokeRunner`, `RegressionRunner`, `SecurityRunner`, `AccessibilityRunner`, `PerformanceRunner`, `ApiRunner`, `AuthenticatedApiRunner`, `E2ERunner`, `NavigationRunner`, `LifecycleRunner`, `DeviceRunner`, `PermissionRunner`, `NegativeRunner`, `DataDrivenRunner`, `InterruptRunner`, `CrossBrowserRunner`, `ParallelRunner`) directly from Eclipse/IntelliJ via **Run As → TestNG Test**. The opt-in, environment-gated suites (`testng-authenticated-api.xml`, `testng-data-quality.xml`) need their `-D` properties set first — see [REST API](#rest-api) and [Data & ETL Testing](#data--etl-testing).

---

## Default Login

Scenarios authenticate against OpenEMR's public demo credentials, already wired into the feature files:

* **Username:** `admin`
* **Password:** `pass`
* **Language:** `Default - English (Standard)`

These are the standard, publicly documented demo credentials for `demo.openemr.io` — not real patient-facing or production credentials, and no real PHI is ever entered (see [Special Considerations](#testing)).

---

## Project Structure

```
project-root/
│
├── src/test/java/
│   ├── pages/            # Page Object Model (POM) classes
│   ├── stepdefinitions/  # Step definition classes + Hooks
│   ├── etl/              # Data/ETL quality checks (opt-in, direct JDBC)
│   ├── unit/             # Plain TestNG unit tests (no browser)
│   ├── runners/          # One TestNG/Cucumber runner per test category
│   └── utils/            # ConfigReader, TestDataGenerator, ApiAuthHelper, DatabaseConnectionHelper, PreflightHealthCheck
│
├── src/test/resources/
│   ├── features/          # Gherkin feature files
│   └── allure.properties  # Allure results directory config
│
├── docs/qa/                    # Test plan, test case register, traceability matrix, defect log
├── .github/workflows/ci.yml    # GitHub Actions pipeline
├── Dockerfile / .dockerignore  # Containerized execution
├── testng.xml                  # Full suite (Cucumber + unit tests)
├── testng-smoke.xml            # Smoke-only suite
├── testng-preflight.xml        # Pre-flight demo health check
├── testng-parallel.xml         # Parallel execution suite
├── testng-cross-browser.xml    # Cross-browser smoke suite
├── testng-authenticated-api.xml # Opt-in: OAuth2 Password Grant API scenarios
├── testng-data-quality.xml     # Opt-in: direct-database data quality checks
├── pom.xml                     # Maven dependencies and plugins
├── LICENSE
└── README.md
```

---

## Database Overview

This repository does not own a database, run migrations, or maintain a schema/ORM layer — it's a UI/API/data test client for OpenEMR's own MySQL-backed data, which the public demo manages independently and periodically resets (tests should never assume long-term persistence of records they create).

It does include a read-only, opt-in JDBC data-quality layer (`etl/DataQualityChecks.java`) for when the suite is pointed at a self-hosted instance instead of the public demo — see [Data & ETL Testing](#data--etl-testing). That instance's database is entirely separate from this repo; point `base.url`/`db.*` (see [Configuration](#configuration)) at it and the suite behaves the same way.

---

## Seed / Utility Scripts

Since there's no owned database, there are no schema seed scripts. The closest equivalent is `utils/TestDataGenerator.java` — a small, dependency-free utility that produces randomized-but-valid patient demographics (first/last name, DOB, gender) so data-driven scenarios aren't limited to the same handful of hardcoded names. It's covered by unit tests in `src/test/java/unit/TestDataGeneratorTest.java`.

```java
TestDataGenerator.Patient patient = TestDataGenerator.randomPatient();
// patient.firstName, patient.lastName, patient.dob, patient.gender
```

---

## Testing

The suite is organized into tagged categories so any slice can be run independently. Scenario counts include each `Scenario Outline` × `Examples` row, matching how Cucumber reports them.

| Category | Tag | Approx. scenarios | Notes |
|---|---|---|---|
| Smoke | `@smoke` | 7 | Fast confidence check: login, dashboard, patient add, logout, API health |
| Regression | `@regression` | 20 | Broader functional coverage across login/patient/admin/navigation |
| App lifecycle | `@lifecycle` | 3 | Refresh, logout→re-login, session continuity |
| Navigation | `@navigation` | 13 | Menu visibility, sub-menu expansion, module switching |
| Device / browser behavior | `@device` | 8 | Viewport resizing, back/forward navigation |
| Permission tests | `@permission` | 3 | Admin menu visibility, unauthenticated access, post-logout access |
| Negative tests | `@negative` | 11 | Invalid credentials, missing/invalid patient fields |
| Performance | `@performance` | 3 | Page load and API response time budgets |
| Security-focused | `@security` | 6 | SQL-injection/XSS payloads, direct URL access, session integrity |
| Accessibility basics | `@accessibility` | 4 | Alt text, form labels, `lang` attribute, mandatory-field marking |
| Data-driven | `@data-driven` | ~35 | `Scenario Outline` + `Examples` across login, patient, navigation |
| E2E flow | `@e2e` | 1 | Full patient onboarding: login → add patient → add insurance → logout |
| Cross-browser | `@cross-browser` | 1 (×N browsers) | Same smoke scenario replayed on Chrome and Firefox via `-Dbrowser` |
| Interrupt tests | `@interrupt` | 2 | Tab-switch and mid-form refresh (adapted from mobile call/SMS interrupts) |
| API-integrated | `@api` | 10 | REST Assured checks: login-endpoint transport smoke (2) + OAuth2 discovery/registration/error-handling/authenticated CRUD against the Standard REST API (8) |
| API — env-gated | `@requires-password-grant` | 4 | Authenticated Standard API scenarios needing OAuth2 Password Grant enabled on the target instance — excluded from the default suite, see [REST API](#rest-api) |
| Data/ETL — opt-in | *(TestNG, `etl.DataQualityChecks`)* | 5 | Null/duplicate/referential-integrity/reconciliation checks against a self-hosted instance's MySQL schema — see [Data & ETL Testing](#data--etl-testing) |
| Unit | *(none — plain TestNG)* | 13 | `ConfigReaderTest` / `TestDataGeneratorTest`, no browser required |

Underlying functional coverage: authentication (valid/invalid login, malicious payloads, whitespace handling, session redirects), patient management (add, search, insurance, gender/DOB validation), dashboard & navigation (module menu visibility, sub-menu expansion, browser back/forward), form handling (mandatory fields, dropdown options, invalid date formats), and UI/accessibility validation (element visibility, alt text, labeled inputs).

**Special considerations:** OpenEMR is a healthcare application, so scenarios simulate realistic but non-sensitive workflows. Only demo/test data is used — never real patient data.

### Latest Verified Runs

**2026-07-30** — full regression suite (`mvn clean test`, 91 TestNG methods incl. unit tests) run locally against the live demo:

```
Tests run: 91, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Smoke suite (`testng-smoke.xml`, 8 scenarios) independently confirmed green as well:

```
Tests run: 8, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Getting the full suite to green surfaced three real, reproducible bugs (not locator guesses — found by running against the live demo and reading the actual failures):

* **`pages/InsurancePage.java`** — corrected using real inspected DOM from the live demo (commits `f30452a`, `61b8ae1`): the Insurance section toggle is a Bootstrap collapse `<button data-target="#div_ins">`, not a link or tab; there is no "Add New" button (expanding the panel reveals the Primary/Secondary/Tertiary insurance forms directly); Provider is a `<select name="i1provider">` (selected by visible text); Policy Number is `<input name="i1policy_number">`; the whole form (patient demographics + insurance) is submitted via the `id="create"` ("Create New Patient") button.
* **`pages/LoginPage.java`** (commit `f707e40`) — `enterUsername()`/`enterPassword()` now `clear()` before `sendKeys()`. Without this, the whitespace-trimming scenario (which enters `"   admin   "` then `"admin"` back to back) concatenated into the field instead of replacing it, producing an invalid combined username and a spurious "Invalid username or password" rejection.
* **`stepdefinitions/PatientSteps.java`** (commit `f707e40`) — `search_patient()` no longer calls `driver.switchTo().defaultContent()` before searching. The Background step "user navigates to patient section" already switches into the iframe containing the Search/Add Patient form (`#form_fname`) and stays there; resetting to `defaultContent()` right before the search dropped out of that iframe, causing `NoSuchElementException`.

One scenario was removed (commit `779186a`): "Searching for a non-existent patient returns no results." `PatientPage.areSearchResultsDisplayed()` uses a too-broad table locator (`//table[contains(@id,'patient') or contains(@class,'table')]`) that matches an empty results table / unrelated table regardless of whether any patient rows exist, so the negative-search assertion false-positived against the live demo. Fixing it properly needs real DOM evidence distinguishing a "results found" state from a "no results" state (not yet available), so the scenario was dropped rather than leaving a permanently-red build. It can be re-added once that HTML is confirmed.

---

## Test Architecture: Tagging & Parallel Execution

Every scenario carries one or more tags (see the table above), and each tag maps to a dedicated TestNG runner class under `src/test/java/runners/` (e.g. `SecurityRunner` → `@security`). This lets CI, an IDE, or a developer run exactly the slice they need without maintaining separate feature files per category.

Parallel execution is opt-in via `ParallelRunner`, which overrides Cucumber-TestNG's `scenarios()` DataProvider with `parallel = true`:

```bash
mvn test -DsuiteXmlFile=testng-parallel.xml -Ddataproviderthreadcount=4
```

Each scenario gets its own WebDriver instance (`Hooks.setup()`/`tearDown()` run per-scenario via Cucumber's `@Before`/`@After`), so parallel scenarios never share browser state. Thread count defaults to TestNG's standard `dataproviderthreadcount` property; tune it based on available CPU/memory, since each thread spins up its own Chrome/Firefox process.

---

## Code Coverage (JaCoCo)

`jacoco-maven-plugin` is wired into `pom.xml` with `prepare-agent` (instruments the JVM at test start) and `report` (bound to the `test` phase). Running any Maven test target produces a coverage report automatically:

```bash
mvn clean test
open target/site/jacoco/index.html   # macOS; use xdg-open/start on Linux/Windows
```

Coverage here reflects this module's own Java code — step definitions, page objects, and the `utils` package — rather than OpenEMR's application code (which this repo doesn't own or compile). It's most meaningful for the unit-tested `utils` classes; UI-driven step definitions will show coverage proportional to which scenarios were executed.

---

## Test Reporting (Allure)

Every runner registers the `allure-cucumber7-jvm` adapter alongside the existing `pretty`/`html` Cucumber plugins, writing results to `target/allure-results` (configured in `src/test/resources/allure.properties`). Generate and view the HTML report with the `allure-maven` plugin:

```bash
mvn test
mvn allure:report   # writes target/allure-report
mvn allure:serve    # builds and opens the report in a browser in one step
```

Allure adds per-scenario timelines, tag-based filtering (`@smoke`, `@regression`, `@security`, etc. — see [Testing](#testing)), and history-across-runs trending on top of the raw Cucumber output — useful for triaging the full 79-scenario regression run without re-running it locally.

### Cucumber report (`target/cucumber-report.html`)

Every `mvn test` / `mvn clean test` run also produces a self-contained Cucumber HTML report at `target/cucumber-report.html` (plus machine-readable `target/cucumber.json` / `target/cucumber.xml`), independent of Allure. It's the fastest way to check a single run: open it directly in a browser, no extra Maven goal needed.

From the **2026-07-30** verified full run (`mvn clean test`, 91 TestNG methods including unit tests), the underlying Cucumber JSON breaks down as:

| Feature file | Scenarios | Steps | Failed |
|---|---|---|---|
| `login.feature` | 30 | — | 0 |
| `patient.feature` | 22 | — | 0 |
| `navigation.feature` | 13 | — | 0 |
| `browser_behavior.feature` | 7 | — | 0 |
| `admin.feature` | 4 | — | 0 |
| `api.feature` | 2 | — | 0 |
| `openemr.feature` | 1 | — | 0 |
| **Total** | **79** | **230** | **0** |

Both `target/cucumber-report.html` and `target/allure-report/` are build output (gitignored — see `.gitignore`), not committed to the repo, so they always reflect the run that produced them rather than going stale. The `Cucumber.pdf` / `OpenEMR Cucumber Test Report.pdf` files in the repo root (see [Screenshots](#screenshots)) are manual PDF exports of a past `cucumber-report.html` — regenerate them the same way (open the fresh HTML report, Print → Save as PDF) after a verified green run if you want an updated static snapshot checked in.

Allure adds per-scenario timelines, tag-based filtering, and history-across-runs trending on top of the default Cucumber HTML report — useful for triaging the full 79-scenario regression run.

---

## BDD & E2E Testing (Cucumber, Selenium)

This is the core of the framework: Gherkin feature files under `src/test/resources/features/` describe behavior in plain language, Cucumber step definitions in `src/test/java/stepdefinitions/` implement each step against Selenium WebDriver, and Page Objects in `src/test/java/pages/` encapsulate locators and interactions per OpenEMR module (Login, Patient, Admin, Dashboard, Insurance).

The `@e2e` tagged scenario in `patient.feature` chains multiple modules into a single realistic flow — login → add patient → add insurance → logout — as opposed to the narrower single-concern scenarios that make up most of the suite.

---

## REST API

This repository doesn't expose or own a REST API — there's no server component here. It does, however, drive OpenEMR's **own** OAuth2-secured Standard REST API as a real test client, in two layers:

1. **Transport-level smoke checks** (`api.feature` / `ApiSteps.java`) — HTTP-layer checks against OpenEMR's public login endpoint (status code, content type, response latency), catching transport regressions faster than a full browser test would.
2. **Authenticated Standard API checks** (`standard_api.feature` / `ApiSteps.java` / `utils/ApiAuthHelper.java`) — real OAuth2 dynamic client registration (RFC 7591) and Password Grant token acquisition against `{host}/oauth2/default`, followed by Bearer-token calls to `{host}/apis/default/api` covering: discovery-document validation, 401 rejection with no/invalid token, authenticated Patient list/create, the documented `{validationErrors, internalErrors, data}` response envelope, and error-handling (422 validation errors, 404 not found).

Layer 2 needs OpenEMR's OAuth2 Password Grant enabled on the target instance — an admin toggle that's **off by default** and explicitly "not recommended for production" per OpenEMR's own docs, independent of whether the Standard API itself is enabled. Rather than let a demo with that toggle off turn into permanent CI noise, those scenarios are tagged `@requires-password-grant` and excluded from the default `TestRunner`/`ApiRunner` tag filters — run them deliberately via `AuthenticatedApiRunner` / `testng-authenticated-api.xml` against an instance known to support it. See `utils/ApiAuthHelper.java` for the exact reasoning and `docs/qa/Test-Plan.md` §5 for the general pattern this follows.

---

## Data & ETL Testing

Direct-database data quality checks against OpenEMR's MySQL schema, in `etl/DataQualityChecks.java` — the third leg of coverage alongside the UI suite (Selenium) and API suite (REST Assured): required-field/null checks, duplicate patient detection, referential integrity (encounters and problem/allergy/medication lists must reference a real patient), and a generic source-to-target row-count reconciliation check.

The shared public demo doesn't expose direct MySQL access, so this suite only runs against a self-hosted or Dockerized OpenEMR instance you control, and requires an explicit `-Ddb.enabled=true` opt-in — without it (or without a reachable database) the whole class skips with a specific message rather than failing. See **`docs/qa/Data-ETL-Testing.md`** for the Docker setup and the full list of checks.

```bash
mvn -B test -DsuiteXmlFile=testng-data-quality.xml -Ddb.enabled=true -Ddb.host=localhost -Ddb.name=openemr -Ddb.user=openemr -Ddb.password=...
```

---

## Test Management & QA Documentation

Jira/Zephyr-style test management artifacts live in **`docs/qa/`**, tracking this suite the way a QA team would rather than leaving coverage implicit in the feature files:

| Document | Purpose |
|---|---|
| `Test-Plan.md` | Scope, strategy per test level, entry/exit criteria, environment constraints and how they're mitigated |
| `Test-Case-Register.xlsx` | Every written scenario (47 as of this writing) with priority, type, component, and automation status, plus a live formula-driven Dashboard tab |
| `Traceability-Matrix.xlsx` | Requirement → test case → feature file → defect mapping, so coverage gaps are visible at a glance |
| `Defect-Log.xlsx` | Jira/Zephyr-style defect tracker, seeded with this project's own real defect history (login-race condition, brittle menu locators, a corrupted `pom.xml`, the shared-demo credential flakiness) |
| `Data-ETL-Testing.md` | Setup and reference for the data quality checks above |

---

## Web Admin Dashboard

This repository does not host a web admin dashboard. "Dashboard" throughout this suite refers to **OpenEMR's own** post-login application dashboard, which the framework drives and verifies (menu visibility, module navigation, sub-menu expansion) via `navigation.feature` and `pages/DashboardPage.java` — it is not infrastructure this repo serves.

---

## Running with Docker

```bash
docker build -t openemr-automation .
docker run --rm openemr-automation                                              # full suite, headless
docker run --rm openemr-automation mvn test -DsuiteXmlFile=testng-smoke.xml -Dheadless=true   # smoke only
```

The image is based on `maven:3.9.6-eclipse-temurin-11` with Google Chrome (stable) installed for headless execution; WebDriverManager downloads a matching chromedriver at container run time, so no manual driver management is needed. See `Dockerfile` / `.dockerignore`.

---

## CI/CD Pipeline

`.github/workflows/ci.yml` runs on every push/PR to `main`, on manual dispatch, and nightly at 03:00 UTC:

1. **smoke** — a pre-flight step first confirms the target demo is up and accepting the configured credentials (`PreflightHealthCheck`, ~20-30s; see `docs/qa/Test-Plan.md` §5), then the headless Chrome smoke suite runs and gates the rest of the pipeline
2. **regression** — full suite (everything except `@requires-password-grant`, see [REST API](#rest-api)), headless Chrome, generates JaCoCo + Allure reports and uploads all report artifacts (Cucumber HTML/JSON, JaCoCo site, Allure report)
3. **cross-browser** — the smoke scenario replayed on Chrome and Firefox in parallel matrix jobs

Reports are uploaded as workflow artifacts so failures can be triaged without re-running locally.

---

## Test Strategy

* **Risk-based tagging over a rigid pyramid.** Most of the value in a UI-driven EMR workflow lives in end-to-end browser behavior, so the bulk of the suite is BDD/Selenium scenarios rather than unit tests. A small, fast unit layer (`src/test/java/unit`) covers the two pieces of pure logic this repo owns (`ConfigReader`, `TestDataGenerator`); everything else is exercised through the browser or, for transport-level checks, through REST Assured.
* **Fast feedback first.** `@smoke` gates CI before the full regression and cross-browser jobs run, so an obviously broken build fails in minutes, not the full pipeline duration.
* **Tag-scoped execution.** Every category (security, accessibility, performance, negative, etc.) has its own runner so a reviewer or CI job can run exactly the relevant slice instead of the entire suite.
* **Explicit waits, not fixed sleeps.** All step definitions use `WebDriverWait`/`ExpectedConditions` rather than `Thread.sleep`, to keep the suite fast and reduce flakiness against a shared public demo environment.
* **Data-driven where it multiplies coverage cheaply.** `Scenario Outline` + `Examples` covers combinatorial input variations (invalid credentials, languages, viewport sizes, gender values) without duplicating step logic.
* **Environment-gated, not silently skipped.** Capabilities that depend on a third party's configuration rather than this framework's own code (OAuth2 Password Grant, direct database access) get their own opt-in runner and an explicit `-D` flag instead of being folded into the suites that gate CI — see `docs/qa/Test-Plan.md` §5.
* **Explicitly out of scope:** load/stress testing, real PHI or production data, FHIR API coverage (base URL is configured, no scenarios written yet), and any infrastructure (admin dashboard, database) this repo doesn't itself own.

---

## Roadmap

* Run `AuthenticatedApiRunner` and `etl.DataQualityChecks` in CI against a self-hosted OpenEMR instance, once one is stood up for this project (both already work today — see [REST API](#rest-api) and [Data & ETL Testing](#data--etl-testing) — they just aren't runnable against the shared public demo)
* FHIR API coverage (`ConfigReader.getFhirBaseUrl()` is already wired up; no scenarios written yet)
* Expand cross-browser matrix to Edge and Safari
* Data-driven testing from external Excel/JSON/CSV sources
* Visual regression checks (screenshot diffing) layered on top of the existing failure-screenshot capture
* AI-assisted test authoring/maintenance (e.g. generating new scenarios or triaging failures with an LLM) — deliberately not started yet, see `docs/qa/Test-Plan.md`

---

## Contributing

Contributions are welcome:

1. Fork the repository and create a feature branch.
2. Add or update Gherkin scenarios with an appropriate tag (see [Test Architecture](#test-architecture-tagging--parallel-execution)) so they're picked up by the right runner.
3. Run `mvn clean test` locally before opening a PR — CI runs the smoke suite first and will fail fast on obvious breakage.
4. Open a pull request describing the change and which category/tag it affects.

---

## License

Released under the [MIT License](LICENSE) — see the `LICENSE` file for the full text.

---

## Contact

**Rezaul Karim** — Software QA Engineer | Automation & Manual Testing
GitHub: [github.com/REZAULKARIM2024](https://github.com/REZAULKARIM2024)

---

## Summary

This project demonstrates a real-world automation framework for a healthcare EMR system, highlighting strong QA engineering skills, scalable framework design, and industry best practices in UI automation and BDD—ideal for SDET and QA automation portfolios.
