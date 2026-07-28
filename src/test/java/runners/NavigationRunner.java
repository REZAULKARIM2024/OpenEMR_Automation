package runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions(
    features = "src/test/resources/features",
    glue = {"stepdefinitions"},
    tags = "@navigation",
    plugin = {"pretty", "html:target/navigation-report.html"}
)
public class NavigationRunner extends AbstractTestNGCucumberTests {
}
