package runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions(
    features = "src/test/resources/features",
    glue = {"stepdefinitions"},
    tags = "@performance",
    plugin = {"pretty", "html:target/performance-report.html", "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"}
)
public class PerformanceRunner extends AbstractTestNGCucumberTests {
}
