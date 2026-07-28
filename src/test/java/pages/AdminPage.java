package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class AdminPage {

    private WebDriver driver;

    public AdminPage(WebDriver driver) {
        this.driver = driver;
    }

    // Admin dropdown toggle
    private By adminMenu = By.xpath(
        "//div[@class='menuLabel px-1 dropdown-toggle oe-dropdown-toggle' and normalize-space()='Admin']"
    );

    // Logout button (inside dropdown)
    private By logoutButton = By.xpath(
        "//a[@class='dropdown-item' and @data-bind='click: logout']"
    );

    public WebElement getAdminMenu() {
        return driver.findElement(adminMenu);
    }

    public WebElement getLogoutButton() {
        return driver.findElement(logoutButton);
    }

    public void clickAdminMenu() {
        getAdminMenu().click();
    }

    public void clickLogout() {
        getLogoutButton().click();
    }
}
