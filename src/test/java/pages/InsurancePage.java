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

            // TODO: unconfirmed by real DOM evidence yet -- once #div_ins
            // expands, we don't yet know whether the add/edit control inside
            // it is a text button labeled "Add New" or an icon-only link
            // (per InsuranceViewCard.php's btnLink to insurance_edit.php).
            // Keeping this locator until real HTML from inside the expanded
            // panel is available.
            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Add New')]"))).click();

            wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("provider"))).sendKeys(provider);
            driver.findElement(By.name("policy_number")).sendKeys(policy);

            driver.findElement(By.id("form_save")).click();
        } finally {
            driver.switchTo().defaultContent();
        }
    }
}