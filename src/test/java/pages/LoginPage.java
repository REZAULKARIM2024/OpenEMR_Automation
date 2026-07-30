package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class LoginPage {

    private WebDriver driver;

    private By username = By.id("authUser");
    private By password = By.id("clearPass");
    private By language = By.xpath("//select[@name='languageChoice' or @id='language' or @id='lang']");
    private By loginButton = By.xpath("//button[@type='submit']");

    // Error / validation banner shown on failed login
    private By errorBanner = By.xpath(
            "//div[contains(@class,'alert') or contains(@class,'error') or contains(@class,'text-danger')]"
    );

    // Login page branding / layout
    private By logo = By.xpath("//img[contains(@src,'logo') or contains(@alt,'OpenEMR')]");
    private By pageHeader = By.xpath("//h2 | //h1 | //*[contains(@class,'oe-login')]");

    public LoginPage(WebDriver driver) {
        this.driver = driver;
    }

    public void enterUsername(String uname) {
        // Clear first: reproduced test failure showed that without this,
        // back-to-back username-entry steps (e.g. the whitespace-trimming
        // scenario, which enters "   admin   " then "admin") concatenate
        // into the field instead of replacing its contents, producing an
        // invalid combined username and a spurious login rejection.
        WebElement el = driver.findElement(username);
        el.clear();
        el.sendKeys(uname);
    }

    public void enterPassword(String pwd) {
        WebElement el = driver.findElement(password);
        el.clear();
        el.sendKeys(pwd);
    }

    public void clearUsername() {
        driver.findElement(username).clear();
    }

    public void clearPassword() {
        driver.findElement(password).clear();
    }

    public WebElement getUsernameField() {
        return driver.findElement(username);
    }

    public By getUsernameLocator() {
        return username;
    }

    public WebElement getPasswordField() {
        return driver.findElement(password);
    }

    public By getLanguageDropdown() {
        return language;
    }

    public By getLoginButtonLocator() {
        return loginButton;
    }

    public WebElement getLoginButton() {
        return driver.findElement(loginButton);
    }

    public void clickLogin() {
        driver.findElement(loginButton).click();
    }

    public By getErrorBannerLocator() {
        return errorBanner;
    }

    public boolean isErrorDisplayed() {
        return driver.findElements(errorBanner).size() > 0
                || driver.findElements(username).size() > 0; // still on login form = failure indicator
    }

    public boolean isLogoDisplayed() {
        return driver.findElements(logo).size() > 0;
    }

    public boolean isLoginFormDisplayed() {
        return driver.findElements(username).size() > 0 && driver.findElements(password).size() > 0;
    }

    public String getPasswordFieldType() {
        return driver.findElement(password).getAttribute("type");
    }
}
