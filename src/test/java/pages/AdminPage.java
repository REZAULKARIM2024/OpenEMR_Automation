package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.ConfigReader;
import utils.DiagnosticsHelper;

import java.time.Duration;

public class AdminPage {

    private WebDriver driver;
    private WebDriverWait wait;

    public AdminPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(ConfigReader.getDefaultTimeoutSeconds()));
    }

    // Admin dropdown toggle. Relaxed from an exact, full class-string match
    // (which broke against the live demo) to matching any element whose
    // class list *contains* "menuLabel" -- see PatientPage for the same fix.
    private By adminMenu = By.xpath(
        "//*[contains(concat(' ', normalize-space(@class), ' '), ' menuLabel ') and normalize-space()='Admin']"
    );

    // Logout button (inside dropdown)
    private By logoutButton = By.xpath(
        "//a[@data-bind='click: logout']"
    );

    public By getAdminMenuLocator() {
        return adminMenu;
    }

    public WebElement getAdminMenu() {
        return driver.findElement(adminMenu);
    }

    public boolean isAdminMenuVisible() {
        return driver.findElements(adminMenu).size() > 0;
    }

    public WebElement getLogoutButton() {
        return driver.findElement(logoutButton);
    }

    public void clickAdminMenu() {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(adminMenu)).click();
        } catch (TimeoutException e) {
            throw new TimeoutException(e.getMessage() + DiagnosticsHelper.describePage(driver), e);
        }
    }

    public void clickLogout() {
        getLogoutButton().click();
    }
}
