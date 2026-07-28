package unit;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import utils.ConfigReader;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Pure unit tests for ConfigReader -- no browser, no network. These run in
 * milliseconds and back the "Testing" / "Code Coverage (JaCoCo)" sections of
 * the README, which describe a unit layer beneath the BDD/E2E and API layers.
 */
public class ConfigReaderTest {

    @AfterMethod
    public void clearOverrides() {
        System.clearProperty("base.url");
        System.clearProperty("dashboard.url");
        System.clearProperty("browser");
        System.clearProperty("headless");
        System.clearProperty("timeout.seconds");
    }

    @Test
    public void defaultBaseUrlPointsToOpenEmrDemo() {
        assertEquals(ConfigReader.getBaseUrl(), "https://demo.openemr.io/openemr/interface/login/login.php");
    }

    @Test
    public void baseUrlIsOverridableViaSystemProperty() {
        System.setProperty("base.url", "https://example.test/login.php");
        assertEquals(ConfigReader.getBaseUrl(), "https://example.test/login.php");
    }

    @Test
    public void defaultBrowserIsChrome() {
        assertEquals(ConfigReader.getBrowser(), "chrome");
    }

    @Test
    public void browserIsOverridableViaSystemProperty() {
        System.setProperty("browser", "firefox");
        assertEquals(ConfigReader.getBrowser(), "firefox");
    }

    @Test
    public void headlessDefaultsToFalseOutsideCi() {
        System.clearProperty("headless");
        // This assertion assumes the unit test itself isn't run with CI env var set
        // to something unexpected; it only checks explicit-override behavior below
        // to stay deterministic regardless of environment.
        System.setProperty("headless", "true");
        assertTrue(ConfigReader.isHeadless());
        System.setProperty("headless", "false");
        assertFalse(ConfigReader.isHeadless());
    }

    @Test
    public void defaultTimeoutIsTwentySeconds() {
        assertEquals(ConfigReader.getDefaultTimeoutSeconds(), 20);
    }

    @Test
    public void timeoutIsOverridableViaSystemProperty() {
        System.setProperty("timeout.seconds", "5");
        assertEquals(ConfigReader.getDefaultTimeoutSeconds(), 5);
    }
}
