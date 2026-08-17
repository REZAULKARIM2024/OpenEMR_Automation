Feature: OpenEMR Standard REST API
  As a QA engineer
  I want to verify OpenEMR's OAuth2-secured Standard REST API
  So that authentication, authorization, functional correctness, and error
  handling are covered by automation, not just the UI

  # Scenarios tagged @requires-password-grant need OpenEMR's OAuth2 Password
  # Grant enabled on the target instance (Administration > Config >
  # Connectors > "Enable OAuth2 Password Grant"). That toggle is OFF by
  # default and OpenEMR's own docs call it "not recommended for production",
  # so whether these pass depends on the target instance's configuration --
  # see ApiAuthHelper.java and the README for why they are intentionally
  # excluded from the default smoke/regression tag sets.

  @api @auth @smoke
  Scenario: OAuth2 discovery document exposes the expected endpoints
    Given the OpenEMR OAuth2 discovery document is reachable
    Then the discovery document should list the token endpoint
    And the discovery document should list the registration endpoint
    And the discovery document should support the "password" grant type

  @api @auth @smoke
  Scenario: Dynamic client registration returns valid credentials
    When a new OAuth2 client is registered with the standard API scopes
    Then the registration response should return a client_id
    And the registration response should return a client_secret

  @api @security @negative
  Scenario: Standard API rejects requests with no bearer token
    When a GET request is sent to the patient resource without a token
    Then the response status code should be 401

  @api @security @negative
  Scenario: Standard API rejects an obviously invalid bearer token
    When a GET request is sent to the patient resource with an invalid token
    Then the response status code should be 401

  @api @authenticated @requires-password-grant
  Scenario: Authenticated request retrieves the patient list
    Given a valid OAuth2 access token via password grant
    When a GET request is sent to the patient resource with a valid token
    Then the response status code should be 200
    And the response body should contain a "data" field
    And the response body should not contain unexpected "validationErrors"

  @api @authenticated @requires-password-grant
  Scenario: Creating a patient via the API returns the created record
    Given a valid OAuth2 access token via password grant
    When a new patient is created via the API with a generated name and DOB
    Then the response status code should be 201
    And the created patient response should include an "id"
    And the created patient response should include an "uuid"

  @api @authenticated @requires-password-grant @negative
  Scenario: Creating a patient with a missing required field returns a validation error
    Given a valid OAuth2 access token via password grant
    When a new patient is created via the API with a missing "fname" field
    Then the response status code should be 422
    And the response body should contain a non-empty "validationErrors" list

  @api @authenticated @requires-password-grant @negative
  Scenario: Requesting a non-existent patient returns 404
    Given a valid OAuth2 access token via password grant
    When a GET request is sent to a patient resource with a non-existent id
    Then the response status code should be 404
