Feature: OpenEMR Login

  @smoke
  Scenario: Successful login
    Given user opens login page
    When user enters username "admin" and password "pass"
    And user selects language "Default - English (Standard)"
    And user clicks login button
    Then user should see dashboard page