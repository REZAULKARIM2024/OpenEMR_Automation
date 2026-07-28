package runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions(
    features = "src/test/resources/features",
    glue = {"stepdefinitions"},
    tags = "@accessibility",
    plugin = {"pretty", "html:target/accessibility-report.html", "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"}
)
public class AccessibilityRunner extends AbstractTestNGCucumberTests {
}
