package runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions(
    features = "src/test/resources/features",
    glue = {"stepdefinitions"},
    tags = "@lifecycle",
    plugin = {"pretty", "html:target/lifecycle-report.html", "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"}
)
public class LifecycleRunner extends AbstractTestNGCucumberTests {
}
