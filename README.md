# OpenEMR_Automation – BDD Cucumber Test Automation Framework

A robust and scalable UI automation framework built for the OpenEMR demo application. This project demonstrates advanced QA automation practices using Behavior-Driven Development (BDD), focusing on healthcare workflows, secure authentication, and reliable UI validation.

---

## 🚀 Overview

**OpenEMR_Automation** is designed to automate critical workflows of an electronic medical record (EMR) system. It simulates real-world healthcare scenarios such as user login, patient management, and navigation across medical modules.

🔗 **Application Under Test:**
http://demo.openemr.io/b/openemr/interface/login/login.php

---

## 🛠️ Tech Stack

* **Language:** Java
* **Automation Tool:** Selenium WebDriver
* **BDD Framework:** Cucumber (Gherkin)
* **Test Runner:** TestNG
* **Build Tool:** Maven
* **IDE:** Eclipse / IntelliJ

---

## 📁 Project Structure

```bash id="q2m7x1"
project-root/
│
├── src/test/java/
│   ├── features/        # Gherkin feature files
│   ├── steps/           # Step definition classes
│   ├── hooks/           # Setup & teardown (Before/After hooks)
│   └── runner/          # Test runner classes
│
├── src/main/java/
│   └── pages/           # Page Object Model (POM) classes
│
├── testng.xml           # TestNG suite configuration
├── pom.xml              # Maven dependencies and plugins
└── README.md            # Project documentation
```

---

## 🎯 Test Coverage

This framework covers essential healthcare application workflows:

* 🔐 **Authentication**

  * Valid and invalid login scenarios
  * Session handling and logout validation

* 👤 **Patient Management**

  * Add new patient
  * Search and view patient records

* 🗂️ **Dashboard & Navigation**

  * Accessing different modules (Patients, Calendar, Messages)
  * Verifying page transitions

* 🧾 **Form Handling**

  * Input validation for patient forms
  * Handling dynamic fields and dropdowns

* 🎯 **UI Validation**

  * Element visibility, text assertions, and layout checks

---

## ⚙️ Framework Highlights

* ✔️ BDD implementation with Cucumber (Gherkin syntax)
* ✔️ Page Object Model (POM) for clean and maintainable code
* ✔️ Reusable step definitions and hooks
* ✔️ Parameterized locators for dynamic UI elements
* ✔️ TestNG integration for flexible execution
* ✔️ Maven for dependency and build management

---

## ▶️ Running the Tests

### Using Maven

```bash id="7s8k2n"
mvn clean test
```

### Using TestNG Suite

```bash id="k9d3lm"
mvn test -DsuiteXmlFile=testng.xml
```

### Using IDE (Eclipse / IntelliJ)

* Right-click on the TestNG runner class
* Select **Run As → TestNG Test**

---

## 📌 Prerequisites

Ensure the following are installed:

* Java (JDK 8 or higher)
* Maven
* Eclipse / IntelliJ IDE
* Browser drivers (e.g., ChromeDriver)

---

## 🔐 Special Considerations

* OpenEMR is a **healthcare application**, so test scenarios are designed to simulate realistic and sensitive workflows.
* Focus on **data validation, secure login handling, and UI reliability**.
* Avoid using real patient data—use only demo/test data provided by the application.

---

## 📈 Future Enhancements

* Add reporting (Extent Reports / Allure Reports)
* Integrate CI/CD (Jenkins / GitHub Actions)
* Enable parallel test execution
* Add cross-browser testing
* Implement data-driven testing (Excel/JSON)
* API testing integration (REST Assured)
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

---
