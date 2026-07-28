package runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions(
    features = "src/test/resources/features",
    glue = {"stepdefinitions"},
    tags = "@interrupt",
    plugin = {"pretty", "html:target/interrupt-report.html"}
)
public class InterruptRunner extends AbstractTestNGCucumberTests {
}
