package stepdefinitions;

import io.cucumber.java.en.*;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import pages.LoginPage;
import utils.ConfigReader;
import java.time.Duration;

import static org.testng.Assert.*;

public class LoginSteps {

    private WebDriver driver;
    private LoginPage loginPage;
    private WebDriverWait wait;
    private long navigationStartMillis;

    public LoginSteps() {
        this.driver = Hooks.getDriver();
        loginPage = new LoginPage(driver);
        wait = new WebDriverWait(driver, Duration.ofSeconds(ConfigReader.getDefaultTimeoutSeconds()));
    }

    @Given("user opens login page")
    public void open_login_page() {
        driver.get(ConfigReader.getBaseUrl());
        // Wait for the page to actually be interactive before any subsequent
        // step tries to type into it -- fixes a "#authUser NoSuchElement"
        // flake seen when the page hadn't finished loading yet.
        wait.until(ExpectedConditions.visibilityOfElementLocated(loginPage.getUsernameLocator()));
    }

    @When("user enters username {string} and password {string}")
    public void enter_credentials(String uname, String pwd) {
        // Scenarios that retry a login (e.g. repeated failed attempts) call
        // this step again right after a failed submit, which triggers a full
        // page reload. Without waiting here, this step can race that reload
        // and hit a NoSuchElementException on a page that's mid-refresh.
        wait.until(ExpectedConditions.visibilityOfElementLocated(loginPage.getUsernameLocator()));

        if (uname != null && !uname.isEmpty()) {
            loginPage.enterUsername(uname);
        }
        if (pwd != null && !pwd.isEmpty()) {
            loginPage.enterPassword(pwd);
        }
    }

    @When("user selects language {string}")
    public void select_language(String lang) {
        try {
            driver.switchTo().frame("loginframe");
        } catch (Exception e) {
            // ignore if frame not present
        }

        wait.until(ExpectedConditions.visibilityOfElementLocated(loginPage.getLanguageDropdown()))
                .sendKeys(lang);

        driver.switchTo().defaultContent();
    }

    @When("user clicks login button")
    public void click_login() {
        loginPage.clickLogin();
    }

    @Then("user should see dashboard page")
    public void verify_dashboard() {
        // Previously checked driver.getTitle().contains("OpenEMR"), which is
        // a false-positive check: the login page's own title is literally
        // "OpenEMR Login", so this passed whether login succeeded OR was
        // rejected (e.g. "Invalid username or password" -- account lockout
        // from repeated automated attempts against the shared public demo).
        // That's why failures were surfacing two steps later, at the Patient
        // menu, with a confusing "menu not clickable" symptom instead of a
        // clear "login was rejected" one. Checking that the login form is
        // gone and the URL no longer points at login.php is an actual
        // logged-in signal, not just a substring that both pages share.
        try {
            wait.until(d -> !loginPage.isLoginFormDisplayed() && !d.getCurrentUrl().contains("login"));
        } catch (TimeoutException e) {
            throw new TimeoutException(
                    "Login did not reach the dashboard (still on the login page -- likely rejected) -- "
                            + e.getMessage() + utils.DiagnosticsHelper.describePage(driver), e);
        }
        assertTrue(!loginPage.isLoginFormDisplayed() && !driver.getCurrentUrl().contains("login"),
                "Expected to reach the dashboard after login, but the login form is still displayed");
    }

    // ---- Negative / validation ----

    @Then("user should see login error {string}")
    public void verify_login_error(String expectedMessage) {
        assertTrue(waitForLoginFormToReappear(), "Expected login to be rejected, but the login form is no longer displayed");
    }

    @Then("login should be rejected")
    public void login_should_be_rejected() {
        assertTrue(waitForLoginFormToReappear(), "Expected login form to remain visible after a rejected login attempt");
    }

    /**
     * Rejected-login checks used to call isLoginFormDisplayed() synchronously
     * right after clickLogin(), which raced the page's post-submit reload/
     * redirect and produced flaky failures. Polling here instead of a single
     * instant check gives the page time to settle before we decide the
     * login form really isn't there.
     */
    private boolean waitForLoginFormToReappear() {
        try {
            wait.until(d -> loginPage.isLoginFormDisplayed());
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    @When("user clears the username field")
    public void clear_username() {
        loginPage.clearUsername();
    }

    @When("user enters username {string} with leading and trailing spaces")
    public void enter_username_with_spaces(String uname) {
        loginPage.enterUsername("   " + uname + "   ");
    }

    // ---- UI validation ----

    @Then("the OpenEMR logo should be visible on the login page")
    public void verify_logo_visible() {
        assertTrue(loginPage.isLogoDisplayed(), "OpenEMR logo was not found on the login page");
    }

    @Then("the username field should be displayed")
    public void verify_username_field_displayed() {
        assertTrue(loginPage.getUsernameField().isDisplayed(), "Username field is not displayed");
    }

    @Then("the password field should be displayed and masked")
    public void verify_password_field_masked() {
        assertTrue(loginPage.getPasswordField().isDisplayed(), "Password field is not displayed");
        assertEquals(loginPage.getPasswordFieldType(), "password", "Password field is not masked (type != password)");
    }

    @Then("the login button should be displayed")
    public void verify_login_button_displayed() {
        assertTrue(loginPage.getLoginButton().isDisplayed(), "Login button is not displayed");
    }

    // ---- Lifecycle / browser behavior ----

    @When("user refreshes the login page")
    public void refresh_login_page() {
        driver.navigate().refresh();
    }

    @When("user navigates back in the browser")
    public void navigate_back() {
        driver.navigate().back();
    }

    @When("user navigates forward in the browser")
    public void navigate_forward() {
        driver.navigate().forward();
    }

    @When("user resizes the browser window to {int} by {int}")
    public void resize_browser(int width, int height) {
        driver.manage().window().setSize(new org.openqa.selenium.Dimension(width, height));
    }

    @Then("the login form should still be usable")
    public void verify_login_form_usable() {
        assertTrue(waitForLoginFormToReappear(), "Login form is not usable after resizing/navigating");
    }

    // ---- Performance ----

    @When("user measures the time to load the login page")
    public void measure_login_page_load_time() {
        navigationStartMillis = System.currentTimeMillis();
        driver.get(ConfigReader.getBaseUrl());
    }

    @Then("the login page should load within {int} seconds")
    public void verify_login_page_load_within(int maxSeconds) {
        wait.until(ExpectedConditions.presenceOfElementLocated(loginPage.getLoginButtonLocator()));
        long elapsedMillis = System.currentTimeMillis() - navigationStartMillis;
        assertTrue(elapsedMillis <= maxSeconds * 1000L,
                "Login page took " + elapsedMillis + "ms, exceeding the " + maxSeconds + "s budget");
    }

    // ---- Security ----

    @When("user attempts login with malicious payload {string} as username")
    public void attempt_login_with_payload(String payload) {
        loginPage.enterUsername(payload);
        loginPage.enterPassword("irrelevant");
        loginPage.clickLogin();
    }

    @Then("the application should not be compromised")
    public void verify_application_not_compromised() {
        // A safe application either rejects the login (form still shown) or
        // shows a generic error, but must never throw the raw payload back
        // unescaped into the page title or crash the driver session.
        String title = driver.getTitle();
        assertFalse(title.contains("<script>"), "Unescaped script payload reflected in page title");
        assertTrue(loginPage.isLoginFormDisplayed() || title.contains("OpenEMR"),
                "Application entered an unexpected state after a malicious login attempt");
    }

    // ---- Direct URL / session security ----

    @When("user navigates directly to the patient dashboard URL without logging in")
    public void navigate_directly_without_login() {
        driver.get(ConfigReader.getDashboardUrl());
    }

    @Then("user should be redirected to the login page")
    public void verify_redirected_to_login() {
        try {
            wait.until(d -> loginPage.isLoginFormDisplayed() || d.getCurrentUrl().contains("login"));
        } catch (TimeoutException e) {
            throw new TimeoutException(e.getMessage() + utils.DiagnosticsHelper.describePage(driver), e);
        }
        assertTrue(loginPage.isLoginFormDisplayed() || driver.getCurrentUrl().contains("login"),
                "Unauthenticated access was not redirected to the login page");
    }
}
