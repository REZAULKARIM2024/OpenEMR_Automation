package runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

@CucumberOptions(
        features = "src/test/resources/features",
        glue = {"stepdefinitions"},
        tags = "not @requires-password-grant",
        plugin = {
                "pretty",
                "html:target/cucumber-report.html",
                "json:target/cucumber.json",
                "junit:target/cucumber.xml", "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"},
        monochrome = true
        // Everything except @requires-password-grant runs by default. Those
        // scenarios (standard_api.feature) need OpenEMR's OAuth2 Password
        // Grant enabled on the target instance -- an admin toggle that's OFF
        // by default and explicitly "not recommended for production" per
        // OpenEMR's own docs (see ApiAuthHelper.java). Excluding them here
        // means a demo with that toggle off doesn't turn into a permanently
        // red regression job; run them deliberately via AuthenticatedApiRunner
        // (or `mvn test -Dcucumber.filter.tags="@requires-password-grant"`)
        // against an instance known to have it enabled.
        // Use the dedicated runners (SmokeRunner, RegressionRunner, SecurityRunner,
        // etc.) or `mvn test -Dcucumber.filter.tags="@smoke"` to run a subset.
)
public class TestRunner extends AbstractTestNGCucumberTests {

    @Override
    @DataProvider(parallel = false)   // set true for parallel execution
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
