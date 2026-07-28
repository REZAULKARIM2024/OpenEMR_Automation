package runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions(
    features = "src/test/resources/features",
    glue = {"stepdefinitions"},
    tags = "@security",
    plugin = {"pretty", "html:target/security-report.html", "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"}
)
public class SecurityRunner extends AbstractTestNGCucumberTests {
}
