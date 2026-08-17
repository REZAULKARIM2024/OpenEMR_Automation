package runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

/**
 * Opt-in runner for the Standard REST API scenarios that need a real Bearer
 * token (OAuth2 Password Grant). Deliberately NOT wired into the smoke or
 * regression CI jobs -- see TestRunner's tag exclusion and ApiAuthHelper's
 * class comment for why. Run explicitly against an OpenEMR instance known
 * to have Password Grant enabled:
 *
 *   mvn -B test -DsuiteXmlFile=testng-authenticated-api.xml
 *   mvn -B test -Dcucumber.filter.tags="@requires-password-grant"
 */
@CucumberOptions(
        features = "src/test/resources/features",
        glue = {"stepdefinitions"},
        tags = "@requires-password-grant",
        plugin = {"pretty", "html:target/authenticated-api-report.html", "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"}
)
public class AuthenticatedApiRunner extends AbstractTestNGCucumberTests {
}
