Feature: API-Level Smoke Checks
  As a QA engineer
  I want lightweight HTTP-level checks alongside the UI suite
  So that transport-level regressions (outages, redirects, slow responses)
  are caught even faster than a full browser test

  @api @smoke
  Scenario: Login endpoint responds successfully
    When a GET request is sent to the login endpoint
    Then the response status code should be 200
    And the response content type should contain "text/html"

  @api @performance
  Scenario: Login endpoint responds within an acceptable time budget
    When a GET request is sent to the login endpoint
    Then the response should be received within 5000 milliseconds
