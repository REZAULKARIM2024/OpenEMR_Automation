package stepdefinitions;

import io.cucumber.java.en.Then;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

import static org.testng.Assert.assertTrue;

/**
 * Lightweight, framework-level accessibility smoke checks.
 * These are not a substitute for a full audit (e.g. axe-core), but catch
 * regressions in the most common basics: image alt text and labeled inputs.
 */
public class AccessibilitySteps {

    private WebDriver driver = Hooks.getDriver();

    @Then("all visible images on the login page should have alt text")
    public void verify_images_have_alt_text() {
        List<WebElement> images = driver.findElements(By.tagName("img"));
        for (WebElement img : images) {
            if (img.isDisplayed()) {
                String alt = img.getAttribute("alt");
                assertTrue(alt != null, "Image with src '" + img.getAttribute("src") + "' is missing an alt attribute");
            }
        }
    }

    @Then("the username and password fields should have accessible labels")
    public void verify_inputs_have_labels() {
        for (String id : new String[]{"authUser", "clearPass"}) {
            WebElement field = driver.findElement(By.id(id));
            String ariaLabel = field.getAttribute("aria-label");
            List<WebElement> label = driver.findElements(By.xpath("//label[@for='" + id + "']"));
            assertTrue(ariaLabel != null || !label.isEmpty(),
                    "Field #" + id + " has no aria-label and no associated <label for> element");
        }
    }

    @Then("the page should declare a language attribute")
    public void verify_page_language_declared() {
        String lang = driver.findElement(By.tagName("html")).getAttribute("lang");
        assertTrue(lang != null && !lang.isEmpty(), "The <html> element is missing a lang attribute");
    }
}
