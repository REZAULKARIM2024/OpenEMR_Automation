Feature: Browser and Device Behavior
  As a QA engineer
  I want to confirm the application behaves predictably under common browser
  interactions
  So that real-world user behavior (resizing, back/forward, refresh) doesn't
  break the experience

  Background:
    Given user opens login page

  @device @regression
  Scenario Outline: Application remains usable across common viewport sizes
    When user resizes the browser window to <width> by <height>
    And user enters username "admin" and password "pass"
    And user clicks login button
    Then user should see dashboard page

    Examples:
      | width | height |
      | 1920  | 1080   |
      | 1440  | 900    |
      | 1280  | 720    |
      | 1024  | 768    |

  @device @regression
  Scenario: Browser back/forward navigation after login does not break the session
    When user enters username "admin" and password "pass"
    And user clicks login button
    Then user should see dashboard page
    When user navigates back in the browser
    And user navigates forward in the browser
    Then user should see dashboard page

  @interrupt
  Scenario: Opening a new tab and switching back does not affect the active session
    When user enters username "admin" and password "pass"
    And user clicks login button
    Then user should see dashboard page
    When user opens a new browser tab and switches back
    Then the application should recover without crashing

  @interrupt @regression
  Scenario: Refreshing mid-workflow recovers gracefully
    When user enters username "admin" and password "pass"
    And user clicks login button
    Then user should see dashboard page
    When user refreshes the page mid-form-entry
    Then the session should still be active or gracefully require re-login
