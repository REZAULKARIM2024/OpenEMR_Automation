package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.ConfigReader;
import utils.DiagnosticsHelper;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

public class PatientPage {

    private WebDriver driver;
    private WebDriverWait wait;

    public PatientPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(ConfigReader.getDefaultTimeoutSeconds()));
    }

    // Patient dropdown toggle. Relaxed from requiring the exact, full class
    // string (which broke against the live demo -- see README/commit history)
    // to matching any element whose class list *contains* "menuLabel", which
    // tolerates class-order changes or extra classes being added/removed.
    private By patientMenu = By.xpath(
        "//*[contains(concat(' ', normalize-space(@class), ' '), ' menuLabel ') and normalize-space()='Patient']"
    );

    // New/Search option
    private By newSearchOption = By.xpath(
        "//*[contains(concat(' ', normalize-space(@class), ' '), ' menuLabel ') and normalize-space()='New/Search']"
    );

    // Patient form fields (inside iframe)
    private By firstNameField = By.id("form_fname");
    private By lastNameField = By.id("form_lname");
    private By dobField = By.id("form_DOB");
    private By genderDropdown = By.id("form_sex");
    private By searchNameField = By.name("form_name");
    private By searchButton = By.xpath("//button[@id='btn-search' or contains(text(),'Search')]");
    private By resultsTable = By.xpath("//table[contains(@id,'patient') or contains(@class,'table')]");

    private By saveButton = By.id("create");

    public WebElement getPatientMenu() {
        return driver.findElement(patientMenu);
    }

    public WebElement getNewSearchOption() {
        return driver.findElement(newSearchOption);
    }

    public void clickPatientMenu() {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(patientMenu)).click();
        } catch (TimeoutException e) {
            throw new TimeoutException(e.getMessage() + DiagnosticsHelper.describePage(driver), e);
        }
    }

    public void clickNewSearch() {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(newSearchOption)).click();
        } catch (TimeoutException e) {
            throw new TimeoutException(e.getMessage() + DiagnosticsHelper.describePage(driver), e);
        }
    }

    public WebElement getFirstNameField() {
        return driver.findElement(firstNameField);
    }

    public void enterFirstName(String fname) {
        driver.findElement(firstNameField).sendKeys(fname);
    }

    public void enterLastName(String lname) {
        driver.findElement(lastNameField).sendKeys(lname);
    }

    public void enterDOB(String dob) {
        driver.findElement(dobField).sendKeys(dob);
    }

    public void selectGender(String gender) {
        Select select = new Select(driver.findElement(genderDropdown));
        select.selectByVisibleText(gender);
    }

    public List<String> getGenderOptions() {
        Select select = new Select(driver.findElement(genderDropdown));
        return select.getOptions().stream().map(WebElement::getText).collect(Collectors.toList());
    }

    public WebElement getSaveButton() {
        return driver.findElement(saveButton);
    }

    public void clickSave() {
        driver.findElement(saveButton).click();
    }

    public void clearForm() {
        driver.findElement(firstNameField).clear();
        driver.findElement(lastNameField).clear();
        driver.findElement(dobField).clear();
    }

    public String getFirstNameValue() {
        return driver.findElement(firstNameField).getAttribute("value");
    }

    public boolean isMandatoryFieldMarked(By field) {
        WebElement el = driver.findElement(field);
        String required = el.getAttribute("required");
        return required != null;
    }

    public void searchPatientByName(String name) {
        driver.findElement(searchNameField).sendKeys(name);
        driver.findElement(searchButton).click();
    }

    public boolean areSearchResultsDisplayed() {
        return driver.findElements(resultsTable).size() > 0;
    }
}
