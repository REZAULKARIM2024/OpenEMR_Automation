Feature: Patient Management
  As clinic staff
  I want to manage patient records
  So that patient information stays accurate and accessible

  Background:
    Given user opens login page
    When user enters username "admin" and password "pass"
    And user selects language "Default - English (Standard)"
    And user clicks login button
    Then user should see dashboard page
    When user navigates to patient section

  @smoke
  Scenario: Add new patient successfully
    When user enters patient details "Reza", "Karim", "1988-05-05", "Male"
    And saves the patient
    Then patient should be added successfully

  @regression @data-driven
  Scenario Outline: Add multiple patients with valid demographic data
    When user enters patient details "<firstName>", "<lastName>", "<dob>", "<gender>"
    And saves the patient
    Then patient should be added successfully

    Examples:
      | firstName | lastName  | dob        | gender |
      | Alice      | Johnson   | 1990-01-15 | Female |
      | Brian      | Smith     | 1985-06-20 | Male   |
      | Carmen     | Diaz      | 1978-11-02 | Female |
      | David      | Nguyen    | 2000-03-30 | Male   |
      | Elena      | Petrova   | 1995-09-09 | Female |

  @regression @negative @data-driven
  Scenario Outline: Add patient with a missing mandatory field
    When user enters patient details "<firstName>", "<lastName>", "<dob>", "<gender>"
    And saves the patient
    Then user should see error message "<errorMessage>"

    Examples:
      | firstName | lastName | dob        | gender | errorMessage                |
      |           | Karim    | 1988-05-05 | Male   | First Name is required      |
      | Reza      |          | 1988-05-05 | Male   | Last Name is required       |
      | Reza      | Karim    |            | Male   | Date of Birth is required   |
      | Reza      | Karim    | 1988-05-05 |        | Gender is required          |

  @regression @negative @data-driven
  Scenario Outline: Add patient with an invalid date of birth format
    When user enters patient details "Reza", "Karim", "<invalidDob>", "Male"
    And saves the patient
    Then user should see error message "Invalid date format"

    Examples:
      | invalidDob   |
      | 05/05/1988   |
      | 1988-13-40   |
      | not-a-date   |

  @regression @negative
  Scenario: Add patient with special characters in the name
    When user enters patient details "Réza-O'Neil", "Kärim", "1988-05-05", "Male"
    And saves the patient
    Then patient should be added successfully

  @regression
  Scenario: Search for an existing patient by name
    When user searches for patient "Reza"
    Then patient search results should be displayed

  @regression @negative
  Scenario: Searching for a non-existent patient returns no results
    When user searches for patient "Zzznonexistentpatient"
    Then no patient search results should be displayed

  @regression @data-driven
  Scenario Outline: Gender dropdown accepts each supported value
    When user enters patient details "Test", "Patient", "1990-01-01", "<gender>"
    And saves the patient
    Then patient should be added successfully

    Examples:
      | gender         |
      | Male           |
      | Female         |
      | Unassigned     |

  @forms
  Scenario: Gender dropdown exposes the expected options
    Then the gender dropdown should contain "Male", "Female" and "Unassigned"

  @forms
  Scenario: Clearing the patient form empties all fields
    When user enters patient details "Temp", "Data", "1990-01-01", "Male"
    And user clears the patient form
    Then the patient form fields should be empty

  @forms @accessibility
  Scenario: First name field is marked as a mandatory field
    Then the first name field should be marked as mandatory

  @e2e @smoke
  Scenario: End-to-end patient onboarding with insurance
    When user enters patient details "Jordan", "Blake", "1992-07-14", "Male"
    And saves the patient
    Then patient should be added successfully
    When user adds insurance details "Blue Cross" and "POL-998877"
    And user logs out
    Then user should be redirected to the login page after logout
