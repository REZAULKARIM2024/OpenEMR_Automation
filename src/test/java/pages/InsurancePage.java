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
        // Root cause (confirmed against the real OpenEMR source on GitHub --
        // InsuranceViewCard.php + templates/patient/card/{insurance,card_base}.html.twig --
        // not a guess): there is no link with visible text "Insurance" anywhere
        // on this page. The card's "Insurance" heading is just a collapse/expand
        // toggle (href="#"); the actual add/edit control is an ICON-ONLY link
        // (<i class="fa fa-pencil-alt">, no text) whose href points at
        // insurance_edit.php. That card is part of the patient's demographics
        // page, which is rendered inside the "pat" content frame, not the
        // outer frameset -- so the old code was searching for nonexistent
        // link text, in the wrong frame, before ever switching into it. The
        // DiagnosticsHelper dump from the last real run (still showing the
        // outer dashboard's top nav, not the patient page) is exactly what
        // you'd expect from that bug.
        driver.switchTo().frame("pat");
        try {
            try {
                wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//a[contains(@href,'insurance_edit.php')]"))).click();
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