package runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions(
    features = "src/test/resources/features",
    glue = {"stepdefinitions"},
    tags = "@permission",
    plugin = {"pretty", "html:target/permission-report.html", "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"}
)
public class PermissionRunner extends AbstractTestNGCucumberTests {
}
