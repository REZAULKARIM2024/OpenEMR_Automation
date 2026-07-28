# OpenEMR_Automation – BDD Cucumber Test Automation Framework

A robust and scalable UI automation framework built for the OpenEMR demo application. This project demonstrates advanced QA automation practices using Behavior-Driven Development (BDD), focusing on healthcare workflows, secure authentication, and reliable UI validation.

---

## 🚀 Overview

**OpenEMR_Automation** is designed to automate critical workflows of an electronic medical record (EMR) system. It simulates real-world healthcare scenarios such as user login, patient management, and navigation across medical modules.

🔗 **Application Under Test:**
https://demo.openemr.io/openemr/interface/login/login.php

---

## 🛠️ Tech Stack

* **Language:** Java 11
* **Automation Tool:** Selenium WebDriver 4 (Chrome + Firefox)
* **BDD Framework:** Cucumber (Gherkin) 7
* **Test Runner:** TestNG
* **Build Tool:** Maven
* **API checks:** REST Assured
* **CI/CD:** GitHub Actions
* **IDE:** Eclipse / IntelliJ

---

## 📁 Project Structure

```
project-root/
│
├── src/test/java/
│   ├── pages/            # Page Object Model (POM) classes
│   ├── stepdefinitions/  # Step definition classes + Hooks
│   └── runners/          # One TestNG/Cucumber runner per test category
│
├── src/test/resources/features/   # Gherkin feature files
├── .github/workflows/ci.yml       # GitHub Actions pipeline
├── testng.xml                     # Full-suite TestNG configuration
├── testng-smoke.xml               # Smoke-only suite
├── testng-cross-browser.xml       # Cross-browser smoke suite
├── pom.xml                        # Maven dependencies and plugins
└── README.md
```

---

## 🎯 Test Coverage — 80 scenarios

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
| Interrupt tests | `@interrupt` | 2 | New-tab context switch, mid-form refresh (adapted from mobile call/SMS interrupts — see note below) |
| API-integrated | `@api` | 2 | REST Assured checks against the login endpoint (status, content-type, latency) |

**A note on two categories that don't map onto a web app:** "Install/upgrade/uninstall" and native "interrupt tests" (incoming call/SMS) are mobile-app concepts. There's no APK/IPA lifecycle for a browser-based EMR, so they aren't represented literally. `@interrupt` instead covers the closest web equivalent — tab switches and mid-workflow refreshes — and is called out as an adaptation rather than a literal match.

Underlying functional coverage:

* 🔐 **Authentication** — valid/invalid login, malicious payloads, whitespace handling, session redirects
* 👤 **Patient Management** — add, search, edit-adjacent (clear/reset), insurance, gender/DOB validation
* 🗂️ **Dashboard & Navigation** — module menu visibility, sub-menu expansion, browser back/forward
* 🧾 **Form Handling** — mandatory fields, dropdown options, invalid date formats
* 🎯 **UI & Accessibility Validation** — element visibility, alt text, labeled inputs

---

## ⚙️ Framework Highlights

* ✔️ BDD implementation with Cucumber (Gherkin syntax)
* ✔️ Page Object Model (POM) for clean and maintainable code
* ✔️ Reusable step definitions and hooks
* ✔️ Parameterized locators for dynamic UI elements
* ✔️ TestNG integration for flexible execution
* ✔️ Cross-browser support (Chrome / Firefox) via `-Dbrowser`
* ✔️ Headless execution support via `-Dheadless=true` (auto-enabled under CI)
* ✔️ REST Assured API-level smoke checks
* ✔️ GitHub Actions CI/CD: smoke → full regression → cross-browser matrix, nightly scheduled run
* ✔️ Maven for dependency and build management

---

## ▶️ Running the Tests

### Full suite (all 80 scenarios)

```bash
mvn clean test
```

### A specific category

```bash
mvn test -DsuiteXmlFile=testng-smoke.xml          # smoke only
mvn test -Dcucumber.filter.tags="@security"       # any tag, ad hoc
```

Or use one of the dedicated runners directly (`SmokeRunner`, `RegressionRunner`, `SecurityRunner`, `AccessibilityRunner`, `PerformanceRunner`, `ApiRunner`, `E2ERunner`, `NavigationRunner`, `LifecycleRunner`, `DeviceRunner`, `PermissionRunner`, `NegativeRunner`, `DataDrivenRunner`, `InterruptRunner`, `CrossBrowserRunner`) from your IDE.

### Cross-browser

```bash
mvn test -DsuiteXmlFile=testng-cross-browser.xml -Dbrowser=chrome
mvn test -DsuiteXmlFile=testng-cross-browser.xml -Dbrowser=firefox
```

### Headless (CI-style)

```bash
mvn test -Dheadless=true
```

### Using IDE (Eclipse / IntelliJ)

* Right-click on any runner class under `src/test/java/runners`
* Select **Run As → TestNG Test**

---

## 🔁 CI/CD

`.github/workflows/ci.yml` runs on every push/PR to `main`, on manual dispatch, and nightly at 03:00 UTC:

1. **smoke** — fast headless Chrome smoke suite, gates the rest of the pipeline
2. **regression** — full 80-scenario suite, headless Chrome, uploads the Cucumber HTML/JSON report as a build artifact
3. **cross-browser** — the smoke scenario replayed on Chrome and Firefox in parallel matrix jobs

Reports are uploaded as workflow artifacts so failures can be triaged without re-running locally.

---

## 📌 Prerequisites

* Java (JDK 11 or higher)
* Maven
* Eclipse / IntelliJ IDE
* Chrome and/or Firefox (drivers are auto-managed by WebDriverManager)

---

## 🔐 Special Considerations

* OpenEMR is a **healthcare application**, so test scenarios are designed to simulate realistic and sensitive workflows.
* Focus on **data validation, secure login handling, and UI reliability**.
* Avoid using real patient data—use only demo/test data provided by the application.
* Some locators (particularly for navigation, forms, and accessibility checks) were written against the documented OpenEMR demo conventions and this repo's existing locator patterns, but were not verified against a live browser session in the environment this suite was generated in. Run the suite once against the live demo and adjust any selectors that drift before relying on it for real regression gating.

---

## 📈 Future Enhancements

* Extend REST Assured coverage to authenticated API calls where available
* Add Extent Reports / Allure for richer HTML reporting
* Expand cross-browser matrix to Edge and Safari
* Data-driven testing from external Excel/JSON/CSV sources
* Database validation (SQL)

---

## 🤝 Contribution

Contributions are welcome!
Feel free to fork the repository and submit pull requests.

---

## 👨‍💻 Author

**Rezaul Karim**
Software QA Engineer | Automation & Manual Testing

---

## 📄 Summary

This project demonstrates a real-world automation framework for a healthcare EMR system, highlighting strong QA engineering skills, scalable framework design, and industry best practices in UI automation and BDD—ideal for SDET and QA automation portfolios.
