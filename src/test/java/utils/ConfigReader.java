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
}
