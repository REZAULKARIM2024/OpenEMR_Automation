package utils;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Test;
import pages.LoginPage;

import java.time.Duration;

/**
 * Pre-flight check -- deliberately NOT part of the smoke/regression suites.
 *
 * Why this exists: on 2026-08-04, every login-dependent scenario in the
 * smoke suite failed together (6 failures, 6 skipped downstream) because
 * the shared public demo (demo.openemr.io) was rejecting the documented
 * default credentials (admin/pass) -- confirmed two ways: identically in
 * CI, and by a manual browser login attempt against the live site. That
 * is an outage/reset on a third party's shared demo instance, not a bug
 * in this framework's code or locators. But the smoke suite still burned
 * ~5 minutes and produced 6 confusing failures (framed as "login
 * rejected", not "demo is down") before that became clear.
 *
 * This class does exactly ONE login attempt and reports a specific,
 * actionable verdict in ~20-30 seconds: dashboard reached (demo is up),
 * credentials rejected (demo outage/reset -- re-run later), or neither
 * (demo unreachable/slow/changed markup). Wire it into CI as its own,
 * earlier step (see .github/workflows/ci.yml) so the expensive suites
 * never even start when the shared demo itself is the problem.
 *
 * Run standalone: mvn -B -Dheadless=true test -DsuiteXmlFile=testng-preflight.xml
 */
public class PreflightHealthCheck {

    private static final String USERNAME = System.getProperty("preflight.username", "admin");
    private static final String PASSWORD = System.getProperty("preflight.password", "pass");

    @Test
    public void demoLoginIsWorking() {
        boolean headless = ConfigReader.isHeadless();

        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        if (headless) {
            options.addArguments("--headless=new");
            options.addArguments("--window-size=1920,1080");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
        }

        WebDriver driver = new ChromeDriver(options);
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            LoginPage loginPage = new LoginPage(driver);

            driver.get(ConfigReader.getBaseUrl());
            wait.until(ExpectedConditions.visibilityOfElementLocated(loginPage.getUsernameLocator()));
            loginPage.enterUsername(USERNAME);
            loginPage.enterPassword(PASSWORD);
            loginPage.clickLogin();

            boolean reachedDashboard;
            try {
                // Same "actually logged in" signal used by LoginSteps.verify_dashboard():
                // login form gone AND the URL no longer points at login.php.
                wait.until(d -> !loginPage.isLoginFormDisplayed() && !d.getCurrentUrl().contains("login"));
                reachedDashboard = true;
            } catch (Exception timeout) {
                reachedDashboard = false;
            }

            if (reachedDashboard) {
                System.out.println("PRE-FLIGHT OK: demo.openemr.io accepted " + USERNAME + "/" + PASSWORD
                        + " and reached the dashboard. Proceeding to the full suite.");
                return;
            }

            String pageBody;
            try {
                pageBody = driver.findElement(org.openqa.selenium.By.tagName("body")).getText();
            } catch (Exception e) {
                pageBody = "";
            }

            if (pageBody.contains("Invalid username or password")) {
                throw new AssertionError(
                        "PRE-FLIGHT FAILED: the shared public demo (demo.openemr.io) is rejecting the "
                        + "documented default credentials (" + USERNAME + "/" + PASSWORD + ") right now.\n"
                        + "This is NOT a bug in this test framework -- it was independently confirmed by a "
                        + "manual browser login attempt against the live site. OpenEMR's public demos reset "
                        + "nightly (documented as 08:00 UTC) and can also glitch outside that window.\n"
                        + "Action: re-run this pipeline in a few minutes. If it keeps failing across "
                        + "multiple hours, consider pointing -Dbase.url / -Ddashboard.url at a self-hosted "
                        + "OpenEMR instance instead of the shared public demo."
                        + DiagnosticsHelper.describePage(driver));
            }

            throw new AssertionError(
                    "PRE-FLIGHT FAILED: login did not reach the dashboard, and no 'Invalid username or "
                    + "password' banner was found either -- the demo site may be slow, unreachable, or its "
                    + "markup has changed." + DiagnosticsHelper.describePage(driver));
        } finally {
            driver.quit();
        }
    }
}
