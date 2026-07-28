package stepdefinitions;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import static org.testng.Assert.assertTrue;

/**
 * Simulates session-interruption scenarios for a web application, standing in
 * for native "interrupt" tests (incoming call/SMS) which don't apply outside
 * of a mobile app context. Here an "interruption" is a browser-level event:
 * a refresh, an abrupt navigation away and back, or a new-tab context switch.
 */
public class InterruptSteps {

    private WebDriver driver = Hooks.getDriver();
    private String originalWindowHandle;

    @When("user opens a new browser tab and switches back")
    public void open_new_tab_and_switch_back() {
        originalWindowHandle = driver.getWindowHandle();
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("window.open('about:blank','_blank');");
        for (String handle : driver.getWindowHandles()) {
            driver.switchTo().window(handle);
        }
        driver.switchTo().window(originalWindowHandle);
    }

    @When("user refreshes the page mid-form-entry")
    public void refresh_mid_form_entry() {
        driver.navigate().refresh();
    }

    @Then("the application should recover without crashing")
    public void verify_application_recovers() {
        String title = driver.getTitle();
        assertTrue(title != null && !title.isEmpty(), "Application did not recover a valid page after the interruption");
    }

    @Then("the session should still be active or gracefully require re-login")
    public void verify_session_state_after_interruption() {
        boolean dashboardVisible = driver.getTitle().contains("OpenEMR");
        boolean loginFormVisible = driver.findElements(By.id("authUser")).size() > 0;
        assertTrue(dashboardVisible || loginFormVisible,
                "Neither the dashboard nor the login form was displayed after the interruption");
    }
}
