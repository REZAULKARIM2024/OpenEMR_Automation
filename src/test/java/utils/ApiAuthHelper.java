package utils;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

/**
 * OAuth2 client registration (RFC 7591) and Password Grant token acquisition
 * against OpenEMR's live OAuth2 server, per Documentation/api/AUTHENTICATION.md
 * in the OpenEMR core repo:
 *   1. POST {oauth2 base}/registration -> client_id/client_secret (public
 *      endpoint, no auth needed).
 *   2. POST {oauth2 base}/token with grant_type=password -> access_token.
 *
 * IMPORTANT CAVEAT: OpenEMR ships the Password Grant OFF by default, and its
 * own docs call it "NOT RECOMMENDED for production" -- it's a separate admin
 * toggle (Administration -> Config -> Connectors -> "Enable OAuth2 Password
 * Grant") independent of whether the Standard API itself is enabled. This
 * class is written the same way PreflightHealthCheck is: it never assumes
 * success, caches a clear failure reason via getLastFailureReason(), and
 * lets callers decide what to do (fail with a specific diagnostic, same
 * philosophy as the rest of this framework -- see PreflightHealthCheck.java).
 *
 * Token/registration results are cached per JVM (a fresh Cucumber scenario
 * would otherwise re-register a brand-new OAuth2 client for every single
 * scenario, which is unnecessary load and noise on the shared demo).
 */
public final class ApiAuthHelper {

    private static final String SCOPE =
            "openid api:oemr user/patient.rs user/patient.cruds user/encounter.rs "
            + "user/appointment.rs user/allergy.rs user/list.read user/facility.rs";

    private static String cachedClientId;
    private static String cachedAccessToken;
    private static boolean registrationAttempted = false;
    private static boolean tokenAttempted = false;
    private static String lastFailureReason;

    private ApiAuthHelper() {
    }

    /** The exact JSON body sent to the registration endpoint -- exposed so tests can assert on it directly. */
    public static String registrationPayload() {
        return "{"
                + "\"application_type\":\"private\","
                + "\"redirect_uris\":[\"https://qa-automation.example.org/callback\"],"
                + "\"client_name\":\"OpenEMR QA Automation Framework\","
                + "\"token_endpoint_auth_method\":\"client_secret_post\","
                + "\"contacts\":[\"qa-automation@example.org\"],"
                + "\"scope\":\"" + SCOPE + "\""
                + "}";
    }

    public static synchronized String registerClient() {
        if (cachedClientId != null || registrationAttempted) {
            return cachedClientId;
        }
        registrationAttempted = true;
        try {
            Response response = RestAssured.given()
                    .relaxedHTTPSValidation()
                    .contentType(ContentType.JSON)
                    .body(registrationPayload())
                    .post(ConfigReader.getOAuth2BaseUrl() + "/registration");

            if (response.getStatusCode() == 200 || response.getStatusCode() == 201) {
                cachedClientId = response.jsonPath().getString("client_id");
            } else {
                lastFailureReason = "Client registration failed: HTTP " + response.getStatusCode()
                        + " -- " + response.getBody().asString();
            }
        } catch (Exception e) {
            lastFailureReason = "Client registration threw: " + e.getMessage();
        }
        return cachedClientId;
    }

    public static synchronized String getAccessToken() {
        if (cachedAccessToken != null || tokenAttempted) {
            return cachedAccessToken;
        }
        tokenAttempted = true;

        String clientId = registerClient();
        if (clientId == null) {
            return null;
        }

        try {
            Response response = RestAssured.given()
                    .relaxedHTTPSValidation()
                    .contentType(ContentType.URLENC)
                    .formParam("grant_type", "password")
                    .formParam("client_id", clientId)
                    .formParam("scope", SCOPE)
                    .formParam("user_role", "users")
                    .formParam("username", ConfigReader.getApiUsername())
                    .formParam("password", ConfigReader.getApiPassword())
                    .post(ConfigReader.getOAuth2BaseUrl() + "/token");

            if (response.getStatusCode() == 200) {
                cachedAccessToken = response.jsonPath().getString("access_token");
            } else {
                lastFailureReason = "Password grant token request failed: HTTP " + response.getStatusCode()
                        + " -- " + response.getBody().asString()
                        + "  (Password Grant may be disabled on this instance -- see "
                        + "Administration > Config > Connectors > 'Enable OAuth2 Password Grant')";
            }
        } catch (Exception e) {
            lastFailureReason = "Token request threw: " + e.getMessage();
        }
        return cachedAccessToken;
    }

    public static boolean isAuthenticatedApiAvailable() {
        return getAccessToken() != null;
    }

    public static String getLastFailureReason() {
        return lastFailureReason;
    }

    /** Clears all cached state -- used by the registration scenario so it always performs a real, fresh call. */
    public static synchronized void reset() {
        cachedClientId = null;
        cachedAccessToken = null;
        registrationAttempted = false;
        tokenAttempted = false;
        lastFailureReason = null;
    }
}
