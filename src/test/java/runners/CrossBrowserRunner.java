package runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions(
    features = "src/test/resources/features",
    glue = {"stepdefinitions"},
    tags = "@cross-browser",
    plugin = {"pretty", "html:target/cross-browser-report.html"}
)
public class CrossBrowserRunner extends AbstractTestNGCucumberTests {
}
