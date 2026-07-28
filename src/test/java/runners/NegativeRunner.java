package runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions(
    features = "src/test/resources/features",
    glue = {"stepdefinitions"},
    tags = "@negative",
    plugin = {"pretty", "html:target/negative-report.html"}
)
public class NegativeRunner extends AbstractTestNGCucumberTests {
}
