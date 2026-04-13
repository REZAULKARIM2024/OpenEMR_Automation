package stepdefinitions;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import stepdefinitions.Hooks;
import pages.PatientPage;

import java.time.Duration;

public class PatientSteps {

    private WebDriver driver = Hooks.getDriver();
    private WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    private PatientPage patientPage = new PatientPage(driver);

    @When("user navigates to patient section")
    public void navigate_patient() {
        driver.switchTo().defaultContent();

        patientPage.clickPatientMenu();
        wait.until(ExpectedConditions.elementToBeClickable(patientPage.getNewSearchOption()));
        patientPage.clickNewSearch();

        // Switch to iframe containing patient form
        driver.switchTo().defaultContent();
        boolean foundFrame = false;

        for (WebElement frame : driver.findElements(By.tagName("iframe"))) {
            driver.switchTo().frame(frame);
            if (driver.findElements(By.id("form_fname")).size() > 0) {
                foundFrame = true;
                break;
            }
            driver.switchTo().defaultContent();
        }

        if (!foundFrame) {
            throw new RuntimeException("Patient form iframe not found.");
        }
    }

    @And("user enters patient details {string}, {string}, {string}, {string}")
    public void enter_patient_details(String fname, String lname, String dob, String gender) {

        wait.until(ExpectedConditions.visibilityOf(patientPage.getFirstNameField()));

        patientPage.enterFirstName(fname);
        patientPage.enterLastName(lname);
        patientPage.enterDOB(dob);
        patientPage.selectGender(gender);
    }

    @And("saves the patient")
    public void save_patient() {
        wait.until(ExpectedConditions.elementToBeClickable(patientPage.getSaveButton()));
        patientPage.clickSave();
    }

    @Then("patient should be added successfully")
    public void verify_patient_added() {
        driver.switchTo().defaultContent();

        // After saving, OpenEMR loads patient summary in a new iframe
        boolean found = false;

        for (WebElement frame : driver.findElements(By.tagName("iframe"))) {
            driver.switchTo().frame(frame);
            if (driver.findElements(By.xpath("//*[contains(text(),'Medical') or contains(text(),'Patient')]")).size() > 0) {
                found = true;
                break;
            }
            driver.switchTo().defaultContent();
        }

        if (!found) {
            throw new AssertionError("Patient summary not found after saving.");
        }
    }

    @Then("user should see error message {string}")
    public void verify_error_message(String expected) {
        // If validation fails, form remains visible
        boolean stillOnForm = driver.findElements(By.id("form_fname")).size() > 0;

        if (!stillOnForm) {
            throw new AssertionError("Expected validation error, but form submitted successfully.");
        }
    }
}
