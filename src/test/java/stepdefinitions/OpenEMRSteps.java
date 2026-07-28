package stepdefinitions;

import io.cucumber.java.en.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.By;
import stepdefinitions.Hooks;

public class OpenEMRSteps {

    private WebDriver driver;

    public OpenEMRSteps() {
        this.driver = Hooks.getDriver();
    }

    @Given("user opens OpenEMR application")
    public void open_application() {
        driver.get("https://demo.openemr.io/openemr/interface/login/login.php");
    }

    @When("user enters username {string}")
    public void enter_username(String username) {
        driver.findElement(By.id("authUser")).sendKeys(username);
    }

    @When("user enters password {string}")
    public void enter_password(String password) {
        driver.findElement(By.id("clearPass")).sendKeys(password);
    }

    @When("user clicks on login")
    public void click_login() {
        driver.findElement(By.xpath("//button[@type='submit']")).click();
    }

    @Then("OpenEMR dashboard should be displayed")
    public void verify_dashboard() {
        String title = driver.getTitle();
        if (!title.contains("OpenEMR")) {
            throw new AssertionError("Dashboard not displayed. Actual title: " + title);
        }
    }
}
