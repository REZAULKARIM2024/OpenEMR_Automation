package runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

/**
 * Runs the full suite with Cucumber scenarios distributed across a thread
 * pool via TestNG's DataProvider(parallel = true). Thread count is
 * controlled by the standard TestNG/Surefire system property:
 *
 *   mvn test -DsuiteXmlFile=testng-parallel.xml -Ddataproviderthreadcount=4
 *
 * Each scenario still gets its own WebDriver instance (Hooks.setup() runs
 * per-scenario), so parallel scenarios do not share browser state.
 */
@CucumberOptions(
        features = "src/test/resources/features",
        glue = {"stepdefinitions"},
        plugin = {
                "pretty",
                "html:target/parallel-report.html",
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
        },
        monochrome = true
)
public class ParallelRunner extends AbstractTestNGCucumberTests {

    @Override
    @DataProvider(parallel = true)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
