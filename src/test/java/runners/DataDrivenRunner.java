package runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions(
    features = "src/test/resources/features",
    glue = {"stepdefinitions"},
    tags = "@data-driven",
    plugin = {"pretty", "html:target/data-driven-report.html", "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"}
)
public class DataDrivenRunner extends AbstractTestNGCucumberTests {
}
