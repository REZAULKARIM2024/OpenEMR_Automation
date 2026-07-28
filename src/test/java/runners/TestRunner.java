package runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

@CucumberOptions(
        features = "src/test/resources/features",
        glue = {"stepdefinitions"},
        plugin = {
                "pretty",
                "html:target/cucumber-report.html",
                "json:target/cucumber.json",
                "junit:target/cucumber.xml"
        },
        monochrome = true
        // No tag filter: running the full 80-scenario suite by default.
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
