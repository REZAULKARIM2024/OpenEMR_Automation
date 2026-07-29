package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.ConfigReader;
import utils.DiagnosticsHelper;

import java.time.Duration;

/**
 * Generic page object for the OpenEMR left-hand navigation menu.
 * Mirrors the dropdown-menu convention already used by AdminPage / PatientPage
 * so the same locator pattern can drive navigation across every top-level module
 * (Patient, Calendar, Messages, Reports, Fees, Documents, Immunizations, Admin ...).
 *
 * The class-attribute match is intentionally loose (contains "menuLabel"
 * rather than requiring one exact, full class string) because a live run
 * against the OpenEMR demo showed the original exact-match locator no longer
 * resolving -- see PatientPage/AdminPage for the same fix and rationale.
 */
public class DashboardPage {

    private WebDriver driver;
    private WebDriverWait wait;

    public DashboardPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(ConfigReader.getDefaultTimeoutSeconds()));
    }

    private By menuLabel(String moduleName) {
        return By.xpath(
                "//*[contains(concat(' ', normalize-space(@class), ' '), ' menuLabel ') and normalize-space()='"
                        + moduleName + "']"
        );
    }

    private By expandedSubMenuItems() {
        return By.xpath("//*[contains(concat(' ', normalize-space(@class), ' '), ' menuLabel ')]");
    }

    public boolean isModuleVisible(String moduleName) {
        // Poll briefly rather than checking once -- the dashboard can still
        // be rendering menu items just after the post-login redirect.
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(menuLabel(moduleName)));
        } catch (Exception e) {
            // fall through to the definitive check below; if it's also
            // false, the caller's assertion message alone won't say what
            // page we were actually on, so log it here for visibility.
            System.err.println("[DIAG] '" + moduleName + "' menu not present." + DiagnosticsHelper.describePage(driver));
        }
        return driver.findElements(menuLabel(moduleName)).size() > 0;
    }

    public void openModule(String moduleName) {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(menuLabel(moduleName))).click();
        } catch (TimeoutException e) {
            throw new TimeoutException(e.getMessage() + DiagnosticsHelper.describePage(driver), e);
        }
    }

    public boolean isSubMenuExpanded() {
        return driver.findElements(expandedSubMenuItems()).size() > 0;
    }

    public String getPageTitle() {
        return driver.getTitle();
    }
}
