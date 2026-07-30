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
        // Root cause, now corrected a second time based on real inspected
        // HTML from the live demo:
        //   <button class="btn btn-link btn-block text-light text-left"
        //           type="button" data-toggle="collapse"
        //           data-target="#div_ins" aria-expanded="true"
        //           aria-controls="ins">Insurance</button>
        // This is the read-only summary "card" demographics page
        // (demographics.php / InsuranceViewCard, per the OpenEMR source
        // fetched earlier from GitHub) -- "Insurance" is a Bootstrap
        // collapse-toggle *button*, not a clickable <a> link, and not a
        // tabNav tab. The previous fix (targeting //a[...]) was wrong: it
        // assumed the tab-based demographics_full.php template, which is
        // apparently NOT the page this flow actually reaches. Clicking this
        // button expands/collapses the #div_ins panel that holds the
        // patient's insurance info and its edit/add controls.
        // That tab menu only exists once the patient's own page is
        // loaded inside the "pat" content frame, not the outer frameset --
        // matching the original bug where the old code searched for this
        // text in the wrong frame and never found it.
        driver.switchTo().frame("pat");
        try {
            try {
                wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//button[@data-target='#div_ins' or (contains(normalize-space(.),'Insurance') and @data-toggle='collapse')]"))).click();
            } catch (TimeoutException e) {
                throw new TimeoutException(e.getMessage() + DiagnosticsHelper.describePage(driver), e);
            }

            // Corrected based on real inspected HTML from the live demo:
            // there is NO "Add New" button. Expanding #div_ins reveals the
            // Primary/Secondary/Tertiary insurance forms directly, inline.
            // Primary provider is a <select name="i1provider"> populated
            // with provider names (e.g. "Aetna"), and Policy Number is
            //   <input type="entry" class="form-control" size="16"
            //          name="i1policy_number" value="" onkeyup="policykeyup(this)">
            // The earlier "Add New" click and the "provider"/"policy_number"
            // field names were both guesses that didn't match this page.
            WebElement providerSelectEl = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(By.name("i1provider")));
            new Select(providerSelectEl).selectByVisibleText(provider);

            WebElement policyField = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(By.name("i1policy_number")));
            policyField.clear();
            policyField.sendKeys(policy);

            // Corrected based on real inspected HTML from the live demo:
            // there is no separate "Save" button for the insurance section.
            // The Primary/Secondary/Tertiary insurance fields are embedded
            // directly inside the same "New Patient" creation form (Title,
            // Name, DOB, ... Insurance, ...), and the whole form -- patient
            // demographics AND insurance together -- is submitted with:
            //   <button type="button" class="btn btn-primary btn-save"
            //           name="create" id="create" value="Create New Patient">
            //     Create New Patient
            //   </button>
            // "form_save" (the previous guess) does not exist on this page.
            //
            // Reproduced failure: a bare findElement().click() here throws
            // ElementClickInterceptedException -- "Element ... is not
            // clickable at point (404, 495). Other element would receive
            // the click: <select name=\"form_i3subscriber_state\"...>".
            // The full Primary/Secondary/Tertiary insurance form is very
            // tall, so the button isn't scrolled into view and ends up
            // behind another field at its cached click coordinates.
            // Scrolling it into view first, then falling back to a JS
            // click if the native click is still intercepted, is the
            // standard fix for this exact exception.
            WebElement createBtn = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("create")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", createBtn);
            try {
                wait.until(ExpectedConditions.elementToBeClickable(By.id("create"))).click();
            } catch (ElementClickInterceptedException e) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", createBtn);
            }
        } finally {
            driver.switchTo().defaultContent();
        }
    }
}