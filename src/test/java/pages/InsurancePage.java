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

        try {
            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(),'Insurance')]"))).click();
        } catch (TimeoutException e) {
            throw new TimeoutException(e.getMessage() + DiagnosticsHelper.describePage(driver), e);
        }

        driver.switchTo().frame("pat");

        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Add New')]"))).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("provider"))).sendKeys(provider);
        driver.findElement(By.name("policy_number")).sendKeys(policy);

        driver.findElement(By.id("form_save")).click();

        driver.switchTo().defaultContent();
    }
}