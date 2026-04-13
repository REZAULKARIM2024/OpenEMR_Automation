Feature: Patient Management

  @smoke
  Scenario: Add new patient successfully
    Given user opens login page
    When user enters username "admin" and password "pass"
    And user selects language "Default - English (Standard)"
    And user clicks login button
    Then user should see dashboard page
    When user navigates to patient section
    And user enters patient details "Reza", "Karim", "1988-05-05", "Male"
    And saves the patient
    Then patient should be added successfully

  @regression
  Scenario: Add patient with missing mandatory fields
    Given user opens login page
    When user enters username "admin" and password "pass"
    And user selects language "Default - English (Standard)"
    And user clicks login button
    Then user should see dashboard page
    When user navigates to patient section
    And user enters patient details "", "Karim", "1988-05-05", "Male"
    And saves the patient
    Then user should see error message "First Name is required"