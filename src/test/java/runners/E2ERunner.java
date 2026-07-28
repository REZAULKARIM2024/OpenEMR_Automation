package runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions(
    features = "src/test/resources/features",
    glue = {"stepdefinitions"},
    tags = "@e2e",
    plugin = {"pretty", "html:target/e2e-report.html", "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"}
)
public class E2ERunner extends AbstractTestNGCucumberTests {
}
