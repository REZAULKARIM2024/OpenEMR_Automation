package stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import utils.ApiAuthHelper;
import utils.ConfigReader;
import utils.TestDataGenerator;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * HTTP-level checks against OpenEMR, in two layers:
 *
 * 1. Transport-level checks against the login page itself (status codes,
 *    headers, response time) -- these need no authentication and always run.
 *
 * 2. Real, authenticated checks against OpenEMR's OAuth2-secured Standard
 *    REST API (Documentation/api/AUTHENTICATION.md + STANDARD_API.md in the
 *    OpenEMR core repo: POST {oauth2 base}/registration, POST
 *    {oauth2 base}/token, then Bearer-token calls to {apis base}/api/...).
 *    An earlier version of this class claimed "the public demo does not
 *    expose an authenticated REST API" -- live inspection of
 *    demo.openemr.io's OIDC discovery document showed that claim was simply
 *    out of date: the API and its OAuth2 server are both live and enabled.
 *
 *    What is genuinely uncertain is whether Password Grant specifically
 *    (a "NOT RECOMMENDED for production" grant type, off by default and
 *    togglable independent of the API itself) is enabled on whatever demo
 *    instance these tests run against. So: registration and discovery
 *    scenarios (tagged @auth, not @requires-password-grant) don't depend on
 *    that toggle and always run. Scenarios that actually need a Bearer
 *    token (tagged @requires-password-grant) are deliberately excluded from
 *    the default smoke/regression tag sets -- see standard_api.feature and
 *    the README -- so a demo with that toggle off doesn't turn into
 *    permanent CI noise, the same reasoning behind PreflightHealthCheck.java
 *    treating a third party's outage as distinct from "our code is broken."
 */
public class ApiSteps {

    private Response response;

    // ---- Transport-level (unauthenticated) ----

    @When("a GET request is sent to the login endpoint")
    public void send_get_request_to_login_endpoint() {
        response = RestAssured.given().relaxedHTTPSValidation().get(ConfigReader.getBaseUrl());
    }

    @Then("the response status code should be {int}")
    public void verify_status_code(int expectedStatus) {
        assertEquals(response.getStatusCode(), expectedStatus,
                "Unexpected HTTP status code -- body: " + response.getBody().asString());
    }

    @Then("the response content type should contain {string}")
    public void verify_content_type(String expectedContentType) {
        String contentType = response.getContentType();
        assertTrue(contentType != null && contentType.contains(expectedContentType),
                "Expected content type to contain '" + expectedContentType + "' but was '" + contentType + "'");
    }

    @Then("the response should be received within {int} milliseconds")
    public void verify_response_time(int maxMillis) {
        long actual = response.getTime();
        assertTrue(actual <= maxMillis, "Response took " + actual + "ms, exceeding the " + maxMillis + "ms budget");
    }

    // ---- OAuth2 discovery ----

    @Given("the OpenEMR OAuth2 discovery document is reachable")
    public void discovery_reachable() {
        response = RestAssured.given().relaxedHTTPSValidation()
                .get(ConfigReader.getOAuth2BaseUrl() + "/.well-known/openid-configuration");
        assertEquals(response.getStatusCode(), 200,
                "OAuth2 discovery document was not reachable at " + ConfigReader.getOAuth2BaseUrl());
    }

    @Then("the discovery document should list the token endpoint")
    public void discovery_has_token_endpoint() {
        assertNotNull(response.jsonPath().getString("token_endpoint"), "Missing token_endpoint in discovery document");
    }

    @Then("the discovery document should list the registration endpoint")
    public void discovery_has_registration_endpoint() {
        assertNotNull(response.jsonPath().getString("registration_endpoint"), "Missing registration_endpoint in discovery document");
    }

    @Then("the discovery document should support the {string} grant type")
    public void discovery_supports_grant(String grantType) {
        List<String> grants = response.jsonPath().getList("grant_types_supported");
        assertTrue(grants != null && grants.contains(grantType),
                "Expected grant_types_supported to contain '" + grantType + "' but was " + grants);
    }

    // ---- Dynamic client registration ----

    @When("a new OAuth2 client is registered with the standard API scopes")
    public void register_client() {
        ApiAuthHelper.reset();
        response = RestAssured.given().relaxedHTTPSValidation()
                .contentType(ContentType.JSON)
                .body(ApiAuthHelper.registrationPayload())
                .post(ConfigReader.getOAuth2BaseUrl() + "/registration");
    }

    @Then("the registration response should return a client_id")
    public void registration_has_client_id() {
        assertEquals(response.getStatusCode(), 201,
                "Expected client registration to succeed -- body: " + response.getBody().asString());
        assertNotNull(response.jsonPath().getString("client_id"), "Registration response missing client_id");
    }

    @Then("the registration response should return a client_secret")
    public void registration_has_client_secret() {
        assertNotNull(response.jsonPath().getString("client_secret"), "Registration response missing client_secret");
    }

    // ---- Standard API: unauthenticated / negative access ----

    @When("a GET request is sent to the patient resource without a token")
    public void get_patient_no_token() {
        response = RestAssured.given().relaxedHTTPSValidation()
                .accept(ContentType.JSON)
                .get(ConfigReader.getApiBaseUrl() + "/patient");
    }

    @When("a GET request is sent to the patient resource with an invalid token")
    public void get_patient_bad_token() {
        response = RestAssured.given().relaxedHTTPSValidation()
                .header("Authorization", "Bearer not-a-real-token")
                .accept(ContentType.JSON)
                .get(ConfigReader.getApiBaseUrl() + "/patient");
    }

    // ---- Standard API: authenticated ----

    @Given("a valid OAuth2 access token via password grant")
    public void obtain_token() {
        String token = ApiAuthHelper.getAccessToken();
        assertNotNull(token, "Could not obtain an access token via password grant -- "
                + ApiAuthHelper.getLastFailureReason());
    }

    @When("a GET request is sent to the patient resource with a valid token")
    public void get_patient_with_token() {
        response = RestAssured.given().relaxedHTTPSValidation()
                .header("Authorization", "Bearer " + ApiAuthHelper.getAccessToken())
                .accept(ContentType.JSON)
                .get(ConfigReader.getApiBaseUrl() + "/patient");
    }

    @When("a new patient is created via the API with a generated name and DOB")
    public void create_patient() {
        TestDataGenerator.Patient patient = TestDataGenerator.randomPatient();
        response = RestAssured.given().relaxedHTTPSValidation()
                .header("Authorization", "Bearer " + ApiAuthHelper.getAccessToken())
                .contentType(ContentType.JSON)
                .body("{"
                        + "\"fname\":\"" + patient.firstName + "\","
                        + "\"lname\":\"" + patient.lastName + "\","
                        + "\"DOB\":\"" + patient.dob + "\","
                        + "\"sex\":\"" + patient.gender + "\""
                        + "}")
                .post(ConfigReader.getApiBaseUrl() + "/patient");
    }

    @When("a new patient is created via the API with a missing {string} field")
    public void create_patient_missing_field(String missingField) {
        TestDataGenerator.Patient patient = TestDataGenerator.randomPatient();
        // Deliberately omits fname (or whichever field the scenario names) to
        // trigger the API's server-side validation, per the 422 example body
        // documented in Documentation/api/STANDARD_API.md.
        response = RestAssured.given().relaxedHTTPSValidation()
                .header("Authorization", "Bearer " + ApiAuthHelper.getAccessToken())
                .contentType(ContentType.JSON)
                .body("{\"lname\":\"" + patient.lastName + "\",\"DOB\":\"" + patient.dob + "\",\"sex\":\"" + patient.gender + "\"}")
                .post(ConfigReader.getApiBaseUrl() + "/patient");
    }

    @When("a GET request is sent to a patient resource with a non-existent id")
    public void get_nonexistent_patient() {
        response = RestAssured.given().relaxedHTTPSValidation()
                .header("Authorization", "Bearer " + ApiAuthHelper.getAccessToken())
                .accept(ContentType.JSON)
                .get(ConfigReader.getApiBaseUrl() + "/patient/00000000-0000-0000-0000-000000000000");
    }

    // ---- Data-contract / envelope assertions ----

    @Then("the response body should contain a {string} field")
    public void body_has_field(String field) {
        assertNotNull(response.jsonPath().get(field),
                "Expected response body to contain field '" + field + "' -- body: " + response.getBody().asString());
    }

    @Then("the response body should not contain unexpected {string}")
    public void body_field_empty(String field) {
        List<?> list = response.jsonPath().getList(field);
        assertTrue(list == null || list.isEmpty(), "Expected '" + field + "' to be empty but was " + list);
    }

    @Then("the response body should contain a non-empty {string} list")
    public void body_has_nonempty_list(String field) {
        List<?> list = response.jsonPath().getList(field);
        assertTrue(list != null && !list.isEmpty(), "Expected non-empty '" + field + "' but was " + list);
    }

    @Then("the created patient response should include an {string}")
    public void created_patient_has_field(String field) {
        Object value = response.jsonPath().get("data." + field);
        assertNotNull(value, "Expected created patient response to include data." + field
                + " -- body: " + response.getBody().asString());
    }
}
