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
* [BDD & E2E Testing (Cucumber, Selenium)](#bdd--e2e-testing-cucumber-selenium)
* [REST API](#rest-api)
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

To generate fresh screenshots for this section, run the suite locally (`mvn clean test`) and open `target/cucumber-report.html` or the Allure report (see [Test Reporting (Allure)](#test-reporting-allure)) — both render inline images for any failures, and you can crop a passing run's dashboard view from there.

---

## Features

* ✔️ BDD implementation with Cucumber (Gherkin syntax) — 80 scenarios across 7 feature files
* ✔️ Page Object Model (POM) for clean and maintainable code
* ✔️ Reusable step definitions and hooks
* ✔️ Parameterized locators for dynamic UI elements
* ✔️ TestNG integration for flexible, tag-based execution
* ✔️ Cross-browser support (Chrome / Firefox) via `-Dbrowser`
* ✔️ Headless execution via `-Dheadless=true` (auto-enabled under CI)
* ✔️ Parallel scenario execution via TestNG's DataProvider
* ✔️ REST Assured API-level smoke checks
* ✔️ JaCoCo code coverage and Allure HTML reporting
* ✔️ GitHub Actions CI/CD: smoke → full regression → cross-browser matrix, nightly scheduled run
* ✔️ Dockerized execution
* ✔️ Maven for dependency and build management

---

## Tech Stack

* **Language:** Java 11
* **Automation Tool:** Selenium WebDriver 4 (Chrome + Firefox)
* **BDD Framework:** Cucumber (Gherkin) 7
* **Test Runner:** TestNG
* **Build Tool:** Maven
* **API checks:** REST Assured
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

Example:

```bash
mvn test -Dbase.url=https://my-openemr-instance/interface/login/login.php -Dbrowser=firefox -Dheadless=true
```

---

## Running the Application

"The application" here means the automated test run itself — there's no standalone server to start first.

```bash
mvn clean test                                          # full 80-scenario suite + unit tests
mvn test -DsuiteXmlFile=testng-smoke.xml                 # smoke only
mvn test -Dcucumber.filter.tags="@security"              # any tag, ad hoc
mvn test -DsuiteXmlFile=testng-parallel.xml              # parallel execution
```

Or run any of the category-specific runner classes (`SmokeRunner`, `RegressionRunner`, `SecurityRunner`, `AccessibilityRunner`, `PerformanceRunner`, `ApiRunner`, `E2ERunner`, `NavigationRunner`, `LifecycleRunner`, `DeviceRunner`, `PermissionRunner`, `NegativeRunner`, `DataDrivenRunner`, `InterruptRunner`, `CrossBrowserRunner`, `ParallelRunner`) directly from Eclipse/IntelliJ via **Run As → TestNG Test**.

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
│   ├── unit/             # Plain TestNG unit tests (no browser)
│   ├── runners/          # One TestNG/Cucumber runner per test category
│   └── utils/            # ConfigReader, TestDataGenerator
│
├── src/test/resources/
│   ├── features/          # Gherkin feature files
│   └── allure.properties  # Allure results directory config
│
├── .github/workflows/ci.yml   # GitHub Actions pipeline
├── Dockerfile / .dockerignore # Containerized execution
├── testng.xml                 # Full suite (Cucumber + unit tests)
├── testng-smoke.xml           # Smoke-only suite
├── testng-parallel.xml        # Parallel execution suite
├── testng-cross-browser.xml   # Cross-browser smoke suite
├── pom.xml                    # Maven dependencies and plugins
├── LICENSE
└── README.md
```

---

## Database Overview

This repository does not own a database. It's a UI/API test client for OpenEMR's demo instance, which manages its own MySQL-backed data independently (and the public demo periodically resets its data — tests should never assume long-term persistence of records they create). There's no schema, migration, or ORM layer in this codebase to document.

If you're running this suite against a self-hosted OpenEMR instance instead of the public demo, that instance's own database is entirely separate from this repo and outside its scope — point `base.url` (see [Configuration](#configuration)) at your instance and the suite behaves the same way.

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
| Regression | `@regression` | 21 | Broader functional coverage across login/patient/admin/navigation |
| App lifecycle | `@lifecycle` | 3 | Refresh, logout→re-login, session continuity |
| Navigation | `@navigation` | 13 | Menu visibility, sub-menu expansion, module switching |
| Device / browser behavior | `@device` | 8 | Viewport resizing, back/forward navigation |
| Permission tests | `@permission` | 3 | Admin menu visibility, unauthenticated access, post-logout access |
| Negative tests | `@negative` | 12 | Invalid credentials, missing/invalid patient fields, bad search |
| Performance | `@performance` | 3 | Page load and API response time budgets |
| Security-focused | `@security` | 6 | SQL-injection/XSS payloads, direct URL access, session integrity |
| Accessibility basics | `@accessibility` | 4 | Alt text, form labels, `lang` attribute, mandatory-field marking |
| Data-driven | `@data-driven` | ~35 | `Scenario Outline` + `Examples` across login, patient, navigation |
| E2E flow | `@e2e` | 1 | Full patient onboarding: login → add patient → add insurance → logout |
| Cross-browser | `@cross-browser` | 1 (×N browsers) | Same smoke scenario replayed on Chrome and Firefox via `-Dbrowser` |
| Interrupt tests | `@interrupt` | 2 | Tab-switch and mid-form refresh (adapted from mobile call/SMS interrupts) |
| API-integrated | `@api` | 2 | REST Assured checks against the login endpoint (status, content-type, latency) |
| Unit | *(none — plain TestNG)* | 13 | `ConfigReaderTest` / `TestDataGeneratorTest`, no browser required |

Underlying functional coverage: authentication (valid/invalid login, malicious payloads, whitespace handling, session redirects), patient management (add, search, insurance, gender/DOB validation), dashboard & navigation (module menu visibility, sub-menu expansion, browser back/forward), form handling (mandatory fields, dropdown options, invalid date formats), and UI/accessibility validation (element visibility, alt text, labeled inputs).

**Special considerations:** OpenEMR is a healthcare application, so scenarios simulate realistic but non-sensitive workflows. Only demo/test data is used — never real patient data.

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

Allure adds per-scenario timelines, tag-based filtering, and history-across-runs trending on top of the default Cucumber HTML report — useful for triaging the full 80-scenario regression run.

---

## BDD & E2E Testing (Cucumber, Selenium)

This is the core of the framework: Gherkin feature files under `src/test/resources/features/` describe behavior in plain language, Cucumber step definitions in `src/test/java/stepdefinitions/` implement each step against Selenium WebDriver, and Page Objects in `src/test/java/pages/` encapsulate locators and interactions per OpenEMR module (Login, Patient, Admin, Dashboard, Insurance).

The `@e2e` tagged scenario in `patient.feature` chains multiple modules into a single realistic flow — login → add patient → add insurance → logout — as opposed to the narrower single-concern scenarios that make up most of the suite.

---

## REST API

This repository doesn't expose or own a REST API — there's no server component here. The `@api` tagged scenarios in `api.feature` / `ApiSteps.java` are HTTP-layer smoke checks against OpenEMR's public login endpoint using REST Assured (status code, content type, response latency), which complement the UI suite by catching transport-level regressions faster than a full browser test would. They are not a general-purpose API test suite against an OpenEMR REST API, since the public demo doesn't expose an authenticated one for this framework to call.

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

1. **smoke** — fast headless Chrome smoke suite, gates the rest of the pipeline
2. **regression** — full 80-scenario suite, headless Chrome, generates JaCoCo + Allure reports and uploads all report artifacts (Cucumber HTML/JSON, JaCoCo site, Allure report)
3. **cross-browser** — the smoke scenario replayed on Chrome and Firefox in parallel matrix jobs

Reports are uploaded as workflow artifacts so failures can be triaged without re-running locally.

---

## Test Strategy

* **Risk-based tagging over a rigid pyramid.** Most of the value in a UI-driven EMR workflow lives in end-to-end browser behavior, so the bulk of the suite is BDD/Selenium scenarios rather than unit tests. A small, fast unit layer (`src/test/java/unit`) covers the two pieces of pure logic this repo owns (`ConfigReader`, `TestDataGenerator`); everything else is exercised through the browser or, for transport-level checks, through REST Assured.
* **Fast feedback first.** `@smoke` gates CI before the full regression and cross-browser jobs run, so an obviously broken build fails in minutes, not the full pipeline duration.
* **Tag-scoped execution.** Every category (security, accessibility, performance, negative, etc.) has its own runner so a reviewer or CI job can run exactly the relevant slice instead of the entire suite.
* **Explicit waits, not fixed sleeps.** All step definitions use `WebDriverWait`/`ExpectedConditions` rather than `Thread.sleep`, to keep the suite fast and reduce flakiness against a shared public demo environment.
* **Data-driven where it multiplies coverage cheaply.** `Scenario Outline` + `Examples` covers combinatorial input variations (invalid credentials, languages, viewport sizes, gender values) without duplicating step logic.
* **Explicitly out of scope:** load/stress testing, real PHI or production data, and any capability (REST API, database, admin dashboard) this repo doesn't itself own — see the sections above for why those are addressed narrowly (API smoke checks) or not at all.

---

## Roadmap

* Extend REST Assured coverage to authenticated API calls, if/when a test-friendly API becomes available
* Expand cross-browser matrix to Edge and Safari
* Data-driven testing from external Excel/JSON/CSV sources
* Database validation (SQL) against a self-hosted OpenEMR instance, if one is stood up for this project
* Visual regression checks (screenshot diffing) layered on top of the existing failure-screenshot capture

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
