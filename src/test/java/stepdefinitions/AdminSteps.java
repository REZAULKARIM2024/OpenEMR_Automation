package stepdefinitions;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.cucumber.java.en.And;
import stepdefinitions.Hooks;
import pages.AdminPage;

import java.time.Duration;

public class AdminSteps {

    private WebDriver driver = Hooks.getDriver();
    private WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    private AdminPage adminPage = new AdminPage(driver);

    @And("user logs out")
    public void logout() {
        driver.switchTo().defaultContent();

        // Force open Admin dropdown
        WebElement adminMenu = adminPage.getAdminMenu();
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
}
