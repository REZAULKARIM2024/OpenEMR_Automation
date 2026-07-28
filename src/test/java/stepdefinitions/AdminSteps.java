package stepdefinitions;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import stepdefinitions.Hooks;
import pages.AdminPage;

import java.time.Duration;

import static org.testng.Assert.assertTrue;

public class AdminSteps {

    private WebDriver driver = Hooks.getDriver();
    private WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    private AdminPage adminPage = new AdminPage(driver);

    @And("user logs out")
    public void logout() {
        driver.switchTo().defaultContent();

        // Force open Admin dropdown -- waited for via the locator (not a
        // pre-fetched WebElement) so we don't race the dashboard still
        // rendering right after login.
        WebElement adminMenu = wait.until(
                ExpectedConditions.presenceOfElementLocated(adminPage.getAdminMenuLocator())
        );
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", adminMenu);

        // Wait for logout button to appear anywhere in DOM
        WebElement logoutBtn = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                        By.xpath("//a[@data-bind='click: logout']")
                )
        );

        // Force click logout
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", logoutBtn);

        driver.switchTo().defaultContent();
    }

    @Then("user should be redirected to the login page after logout")
    public void verify_redirected_after_logout() {
        wait.until(d -> d.findElements(By.id("authUser")).size() > 0 || d.getCurrentUrl().contains("login"));
        assertTrue(driver.findElements(By.id("authUser")).size() > 0 || driver.getCurrentUrl().contains("login"),
                "Expected the login page to be displayed after logout");
    }

    @Then("the Admin menu should be visible for a logged-in user")
    public void verify_admin_menu_visible() {
        driver.switchTo().defaultContent();
        wait.until(d -> adminPage.isAdminMenuVisible());
        assertTrue(adminPage.isAdminMenuVisible(), "Admin menu is not visible for a logged-in user");
    }
}
