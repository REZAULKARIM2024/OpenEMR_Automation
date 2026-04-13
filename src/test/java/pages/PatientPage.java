package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

public class PatientPage {

    private WebDriver driver;

    public PatientPage(WebDriver driver) {
        this.driver = driver;
    }

    // Patient dropdown toggle
    private By patientMenu = By.xpath(
        "//div[@class='menuLabel px-1 dropdown-toggle oe-dropdown-toggle' and normalize-space()='Patient']"
    );

    // New/Search option
    private By newSearchOption = By.xpath(
        "//div[@class='menuLabel px-1' and normalize-space()='New/Search']"
    );

    // Patient form fields (inside iframe)
    private By firstNameField = By.id("form_fname");
    private By lastNameField = By.id("form_lname");
    private By dobField = By.id("form_DOB");
    private By genderDropdown = By.id("form_sex");

    private By saveButton = By.id("create");

    public WebElement getPatientMenu() {
        return driver.findElement(patientMenu);
    }

    public WebElement getNewSearchOption() {
        return driver.findElement(newSearchOption);
    }

    public void clickPatientMenu() {
        getPatientMenu().click();
    }

    public void clickNewSearch() {
        getNewSearchOption().click();
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

    public WebElement getSaveButton() {
        return driver.findElement(saveButton);
    }

    public void clickSave() {
        driver.findElement(saveButton).click();
    }
}
