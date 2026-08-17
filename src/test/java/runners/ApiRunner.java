package runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

// All @api scenarios except the ones needing OAuth2 Password Grant enabled
// on the target instance -- use AuthenticatedApiRunner for those, since
// whether they pass depends on that instance's admin configuration (see
// ApiAuthHelper.java).
@CucumberOptions(
    features = "src/test/resources/features",
    glue = {"stepdefinitions"},
    tags = "@api and not @requires-password-grant",
    plugin = {"pretty", "html:target/api-report.html", "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"}
)
public class ApiRunner extends AbstractTestNGCucumberTests {
}
