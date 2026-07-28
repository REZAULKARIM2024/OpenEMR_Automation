Feature: OpenEMR Admin Actions
  As clinic staff
  I want reliable session and admin controls
  So that access to the system is properly guarded

  Background:
    Given user opens login page
    When user enters username "admin" and password "pass"
    And user selects language "Default - English (Standard)"
    And user clicks login button
    Then user should see dashboard page

  @regression @smoke
  Scenario: Logout successfully
    And user logs out
    Then user should be redirected to the login page after logout

  @permission
  Scenario: Admin menu is visible for a logged-in administrator
    Then the Admin menu should be visible for a logged-in user

  @permission @security
  Scenario: Session ends after logout and protected pages redirect to login
    And user logs out
    When user navigates directly to the patient dashboard URL without logging in
    Then user should be redirected to the login page

  @lifecycle
  Scenario: Logging out and logging back in restores dashboard access
    And user logs out
    Then user should be redirected to the login page after logout
    When user enters username "admin" and password "pass"
    And user selects language "Default - English (Standard)"
    And user clicks login button
    Then user should see dashboard page
