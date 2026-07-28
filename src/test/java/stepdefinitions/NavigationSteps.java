package stepdefinitions;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import pages.DashboardPage;

import java.time.Duration;

import static org.testng.Assert.assertTrue;

public class NavigationSteps {

    private WebDriver driver = Hooks.getDriver();
    private WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    private DashboardPage dashboardPage = new DashboardPage(driver);

    @Then("the {string} menu item should be visible on the dashboard")
    public void verify_menu_item_visible(String moduleName) {
        driver.switchTo().defaultContent();
        assertTrue(dashboardPage.isModuleVisible(moduleName), moduleName + " menu item is not visible on the dashboard");
    }

    @When("user opens the {string} module from the dashboard menu")
    public void open_module(String moduleName) {
        driver.switchTo().defaultContent();
        dashboardPage.openModule(moduleName);
    }

    @Then("the {string} module sub-menu should expand")
    public void verify_submenu_expanded(String moduleName) {
        wait.until(d -> dashboardPage.isSubMenuExpanded());
        assertTrue(dashboardPage.isSubMenuExpanded(), moduleName + " sub-menu did not expand");
    }

    @Then("the dashboard should be the default page after login")
    public void verify_dashboard_default_page() {
        wait.until(ExpectedConditions.titleContains("OpenEMR"));
        assertTrue(dashboardPage.getPageTitle().contains("OpenEMR"), "Dashboard was not the default landing page after login");
    }

    @And("the left navigation menu should remain visible")
    public void verify_left_menu_persists() {
        assertTrue(
                dashboardPage.isModuleVisible("Patient") || dashboardPage.isModuleVisible("Calendar"),
                "Left navigation menu is not visible after navigating between modules"
        );
    }
}
