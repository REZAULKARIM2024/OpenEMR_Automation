package runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions(
    features = "src/test/resources/features",
    glue = {"stepdefinitions"},
    tags = "@accessibility",
    plugin = {"pretty", "html:target/accessibility-report.html"}
)
public class AccessibilityRunner extends AbstractTestNGCucumberTests {
}
