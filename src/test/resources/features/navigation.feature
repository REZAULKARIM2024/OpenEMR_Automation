Feature: Dashboard Navigation
  As clinic staff
  I want to move between OpenEMR modules from the dashboard
  So that I can reach the tools I need without losing context

  Background:
    Given user opens login page
    When user enters username "admin" and password "pass"
    And user selects language "Default - English (Standard)"
    And user clicks login button
    Then user should see dashboard page

  @smoke @navigation
  Scenario: Dashboard is the default landing page after login
    Then the dashboard should be the default page after login

  @regression @navigation @data-driven
  Scenario Outline: Top-level module menu items are visible on the dashboard
    Then the "<module>" menu item should be visible on the dashboard

    Examples:
      | module       |
      | Patient      |
      | Calendar     |
      | Messages     |
      | Reports      |
      | Fees         |
      | Documents    |
      | Admin        |

  @regression @navigation @data-driven
  Scenario Outline: Opening a module expands its sub-menu
    When user opens the "<module>" module from the dashboard menu
    Then the "<module>" module sub-menu should expand

    Examples:
      | module    |
      | Patient   |
      | Calendar  |
      | Messages  |

  @regression @navigation
  Scenario: Left navigation menu persists across module switches
    When user opens the "Patient" module from the dashboard menu
    And user opens the "Calendar" module from the dashboard menu
    Then the left navigation menu should remain visible

  @device @navigation
  Scenario: Navigating back after opening a module returns to the dashboard
    When user opens the "Patient" module from the dashboard menu
    And user navigates back in the browser
    Then the dashboard should be the default page after login
