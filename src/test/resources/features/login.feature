Feature: OpenEMR Login
  As a clinic staff member
  I want to log in to OpenEMR
  So that I can access patient and administrative workflows securely

  Background:
    Given user opens login page

  @smoke @cross-browser
  Scenario: Successful login with valid credentials
    When user enters username "admin" and password "pass"
    And user selects language "Default - English (Standard)"
    And user clicks login button
    Then user should see dashboard page

  @smoke
  Scenario: Login page displays the OpenEMR branding
    Then the OpenEMR logo should be visible on the login page
    And the username field should be displayed
    And the password field should be displayed and masked
    And the login button should be displayed

  @regression @negative @data-driven
  Scenario Outline: Login is rejected for invalid credential combinations
    When user enters username "<username>" and password "<password>"
    And user clicks login button
    Then login should be rejected

    Examples:
      | username     | password     |
      | admin        | wrongpass    |
      | wronguser    | pass         |
      | wronguser    | wrongpass    |
      |              | pass         |
      | admin        |              |
      |              |              |
      | Admin        | pass         |
      | ADMIN        | PASS         |
      | admin123456  | pass         |
      | admin        | pass12345678 |

  @regression @data-driven
  Scenario Outline: Successful login across supported languages
    When user enters username "admin" and password "pass"
    And user selects language "<language>"
    And user clicks login button
    Then user should see dashboard page

    Examples:
      | language                        |
      | Default - English (Standard)    |
      | Spanish (Español)                |
      | French (Français)                |
      | German (Deutsch)                 |
      | Italian (Italiano)                |

  @security @negative @data-driven
  Scenario Outline: Login attempts with malicious payloads are handled safely
    When user attempts login with malicious payload "<payload>" as username
    Then the application should not be compromised

    Examples:
      | payload                          |
      | ' OR '1'='1                      |
      | admin'--                         |
      | <script>alert('xss')</script>    |
      | ../../../../etc/passwd           |

  @regression @negative
  Scenario: Username field trims leading and trailing whitespace
    When user enters username "admin" with leading and trailing spaces
    And user enters username "admin" and password "pass"
    And user clicks login button
    Then user should see dashboard page

  @regression @negative
  Scenario: Repeated failed login attempts do not crash the application
    When user enters username "admin" and password "wrongpass"
    And user clicks login button
    And user enters username "admin" and password "wrongpass"
    And user clicks login button
    And user enters username "admin" and password "wrongpass"
    And user clicks login button
    Then login should be rejected

  @lifecycle
  Scenario: Refreshing the login page does not lose the form
    When user refreshes the login page
    Then the login form should still be usable

  @device
  Scenario Outline: Login page remains usable at different window sizes
    When user resizes the browser window to <width> by <height>
    Then the login form should still be usable

    Examples:
      | width | height |
      | 1920  | 1080   |
      | 1366  | 768    |
      | 1024  | 768    |

  @performance
  Scenario: Login page loads within an acceptable time budget
    When user measures the time to load the login page
    Then the login page should load within 10 seconds

  @security @permission
  Scenario: Direct URL access to the dashboard without logging in is blocked
    When user navigates directly to the patient dashboard URL without logging in
    Then user should be redirected to the login page

  @accessibility
  Scenario: Login page meets basic accessibility expectations
    Then all visible images on the login page should have alt text
    And the username and password fields should have accessible labels
    And the page should declare a language attribute
