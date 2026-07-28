package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Generic page object for the OpenEMR left-hand navigation menu.
 * Mirrors the dropdown-menu convention already used by AdminPage / PatientPage
 * so the same locator pattern can drive navigation across every top-level module
 * (Patient, Calendar, Messages, Reports, Fees, Documents, Immunizations, Admin ...).
 */
public class DashboardPage {

    private WebDriver driver;

    public DashboardPage(WebDriver driver) {
        this.driver = driver;
    }

    private By menuLabel(String moduleName) {
        return By.xpath(
                "//div[@class='menuLabel px-1 dropdown-toggle oe-dropdown-toggle' and normalize-space()='"
                        + moduleName + "']"
        );
    }

    private By expandedSubMenuItems() {
        return By.xpath("//div[@class='menuLabel px-1']");
    }

    public boolean isModuleVisible(String moduleName) {
        return driver.findElements(menuLabel(moduleName)).size() > 0;
    }

    public void openModule(String moduleName) {
        driver.findElement(menuLabel(moduleName)).click();
    }

    public boolean isSubMenuExpanded() {
        return driver.findElements(expandedSubMenuItems()).size() > 0;
    }

    public String getPageTitle() {
        return driver.getTitle();
    }
}
