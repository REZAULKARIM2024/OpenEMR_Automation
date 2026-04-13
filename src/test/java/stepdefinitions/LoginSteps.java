package stepdefinitions;

import io.cucumber.java.en.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import pages.LoginPage;
import java.time.Duration;

public class LoginSteps {

    private WebDriver driver;
    private LoginPage loginPage;
    private WebDriverWait wait;

    public LoginSteps() {
        this.driver = Hooks.getDriver();
        loginPage = new LoginPage(driver);
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    @Given("user opens login page")
    public void open_login_page() {
        driver.get("https://demo.openemr.io/openemr/interface/login/login.php");
    }

    @When("user enters username {string} and password {string}")
    public void enter_credentials(String uname, String pwd) {
        loginPage.enterUsername(uname);
        loginPage.enterPassword(pwd);
    }

    @When("user selects language {string}")
    public void select_language(String lang) {
        try {
            driver.switchTo().frame("loginframe");
        } catch (Exception e) {
            // ignore if frame not present
        }

        wait.until(ExpectedConditions.visibilityOfElementLocated(loginPage.getLanguageDropdown()))
                .sendKeys(lang);

        driver.switchTo().defaultContent();
    }

    @When("user clicks login button")
    public void click_login() {
        loginPage.clickLogin();
    }

    @Then("user should see dashboard page")
    public void verify_dashboard() {
        wait.until(ExpectedConditions.titleContains("OpenEMR"));
    }
}
