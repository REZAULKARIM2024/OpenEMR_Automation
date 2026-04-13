package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class LoginPage {

    private WebDriver driver;

    // Updated locators
    private By username = By.id("authUser");
    private By password = By.id("clearPass");

    // NEW: Correct locator for language dropdown
    private By language = By.xpath("//select[@name='languageChoice' or @id='language' or @id='lang']");

    private By loginButton = By.xpath("//button[@type='submit']");

    public LoginPage(WebDriver driver) {
        this.driver = driver;
    }

    public void enterUsername(String uname) {
        driver.findElement(username).sendKeys(uname);
    }

    public void enterPassword(String pwd) {
        driver.findElement(password).sendKeys(pwd);
    }

    public By getLanguageDropdown() {
        return language;
    }

    public void clickLogin() {
        driver.findElement(loginButton).click();
    }
}
