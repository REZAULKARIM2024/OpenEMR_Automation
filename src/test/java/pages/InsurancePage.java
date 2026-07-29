package pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import utils.DiagnosticsHelper;

import java.time.Duration;

public class InsurancePage {

    WebDriver driver;
    WebDriverWait wait;

    public InsurancePage(WebDriver driver) {
        this.driver = driver;
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    public void addInsurance(String provider, String policy) {
        // Root cause, now confirmed two ways: (1) a live screenshot of the
        // demo showing a vertical category menu -- Contact, Choices,
        // Employer, Stats, Misc, Related, Insurance -- on the patient's
        // "Edit Current Patient" screen, and (2) the real OpenEMR source for
        // that exact screen (interface/patient_file/summary/demographics_full.php),
        // which renders that menu as <ul class="tabNav"> via
        // display_layout_tabs('DEM', ...) -- i.e. real tab links whose
        // visible text is literally the category name, "Insurance" included.
        // (An earlier fix here assumed the read-only summary "card" page --
        // demographics.php / InsuranceViewCard -- where Insurance has no
        // visible link text at all; that was the wrong template for this
        // flow.) That tab menu only exists once the patient's own page is
        // loaded inside the "pat" content frame, not the outer frameset --
        // matching the original bug where the old code searched for this
        // text in the wrong frame and never found it.
        driver.switchTo().frame("pat");
        try {
            try {
                wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//a[contains(normalize-space(.),'Insurance')]"))).click();
            } catch (TimeoutException e) {
                throw new TimeoutException(e.getMessage() + DiagnosticsHelper.describePage(driver), e);
            }

            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Add New')]"))).click();

            wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("provider"))).sendKeys(provider);
            driver.findElement(By.name("policy_number")).sendKeys(policy);

            driver.findElement(By.id("form_save")).click();
        } finally {
            driver.switchTo().defaultContent();
        }
    }
}