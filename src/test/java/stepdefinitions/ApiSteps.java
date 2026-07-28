package stepdefinitions;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Lightweight HTTP-level checks that complement the UI suite.
 * The public OpenEMR demo does not expose an authenticated REST API for
 * this framework to call, so these checks operate at the transport level
 * (status codes, headers, response time) against the same endpoints the UI
 * drives -- a common pattern when a dedicated test API isn't available.
 */
public class ApiSteps {

    private Response response;
    private static final String LOGIN_URL = "https://demo.openemr.io/openemr/interface/login/login.php";

    @When("a GET request is sent to the login endpoint")
    public void send_get_request_to_login_endpoint() {
        response = RestAssured.given().relaxedHTTPSValidation().get(LOGIN_URL);
    }

    @Then("the response status code should be {int}")
    public void verify_status_code(int expectedStatus) {
        assertEquals(response.getStatusCode(), expectedStatus,
                "Unexpected HTTP status code from " + LOGIN_URL);
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
}
