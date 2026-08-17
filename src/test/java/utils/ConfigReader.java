package utils;

/**
 * Centralizes environment-configurable values so they aren't hardcoded
 * across step definition classes. Every value can be overridden with a
 * JVM system property (e.g. `mvn test -Dbase.url=https://my-instance/...`),
 * and falls back to the public OpenEMR demo when nothing is supplied.
 */
public final class ConfigReader {

    private static final String DEFAULT_BASE_URL =
            "https://demo.openemr.io/openemr/interface/login/login.php";

    private static final String DEFAULT_DASHBOARD_URL =
            "https://demo.openemr.io/openemr/interface/main/tabs/main.php";

    private ConfigReader() {
    }

    public static String getBaseUrl() {
        return System.getProperty("base.url", DEFAULT_BASE_URL);
    }

    public static String getDashboardUrl() {
        return System.getProperty("dashboard.url", DEFAULT_DASHBOARD_URL);
    }

    public static String getBrowser() {
        return System.getProperty("browser", "chrome");
    }

    public static boolean isHeadless() {
        return Boolean.parseBoolean(
                System.getProperty("headless", System.getenv("CI") != null ? "true" : "false")
        );
    }

    public static int getDefaultTimeoutSeconds() {
        return Integer.parseInt(System.getProperty("timeout.seconds", "20"));
    }

    // ---- REST/FHIR API configuration ----
    //
    // Derived from getBaseUrl() (strip "/interface/login/login.php") rather
    // than hardcoded again, so pointing -Dbase.url at a different OpenEMR
    // instance automatically retargets the API tests too. Each piece is
    // still independently overridable for setups where that assumption
    // doesn't hold (e.g. API behind a different host/site id).

    public static String getSiteRootUrl() {
        String base = getBaseUrl();
        int idx = base.indexOf("/interface/");
        String root = idx > 0 ? base.substring(0, idx) : base;
        return System.getProperty("site.root.url", root);
    }

    /** OAuth2/OIDC authorization server base, per Documentation/api/AUTHENTICATION.md. */
    public static String getOAuth2BaseUrl() {
        return System.getProperty("oauth2.base.url", getSiteRootUrl() + "/oauth2/default");
    }

    /** Standard REST API base, per Documentation/api/STANDARD_API.md. */
    public static String getApiBaseUrl() {
        return System.getProperty("api.base.url", getSiteRootUrl() + "/apis/default/api");
    }

    /** FHIR API base -- not yet exercised by tests, kept for future use. */
    public static String getFhirBaseUrl() {
        return System.getProperty("fhir.base.url", getSiteRootUrl() + "/apis/default/fhir");
    }

    /** Credentials used for the OAuth2 Password Grant (api.* so they're independent of the UI login props). */
    public static String getApiUsername() {
        return System.getProperty("api.username", "admin");
    }

    public static String getApiPassword() {
        return System.getProperty("api.password", "pass");
    }

    // ---- Data/ETL (direct database) configuration ----
    //
    // The shared public demo does not expose direct MySQL access (nor should
    // it), so these all default to a typical local/Dockerized OpenEMR MySQL
    // instance (see docs/qa/Data-ETL-Testing.md for the docker-compose
    // reference) and every value is override-able for whatever instance the
    // person running these actually has. isDatabaseConfigured() is what lets
    // DataQualityChecks fail with a specific "no DB configured" skip message
    // instead of a raw JDBC connection-refused stack trace when nobody has
    // set this up (the same graceful-degradation pattern as ApiAuthHelper).

    public static String getDbHost() {
        return System.getProperty("db.host", "localhost");
    }

    public static int getDbPort() {
        return Integer.parseInt(System.getProperty("db.port", "3306"));
    }

    public static String getDbName() {
        return System.getProperty("db.name", "openemr");
    }

    public static String getDbUser() {
        return System.getProperty("db.user", "openemr");
    }

    public static String getDbPassword() {
        return System.getProperty("db.password", "openemr");
    }

    public static String getDbJdbcUrl() {
        return System.getProperty("db.url",
                "jdbc:mysql://" + getDbHost() + ":" + getDbPort() + "/" + getDbName()
                        + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
    }

    /** Explicit opt-in flag -- data/ETL checks only run when this is true, even if the above defaults happen to resolve to a reachable host. */
    public static boolean isDatabaseEnabled() {
        return Boolean.parseBoolean(System.getProperty("db.enabled", "false"));
    }
}
