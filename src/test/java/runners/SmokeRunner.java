package runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions(
    features = "src/test/resources/features",
    glue = {"stepdefinitions", "hooks"},
    tags = "@smoke",
    plugin = {"pretty", "html:target/smoke-report.html"}
)
public class SmokeRunner extends AbstractTestNGCucumberTests {
}