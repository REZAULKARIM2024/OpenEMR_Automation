package runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions(
    features = "src/test/resources/features",
    glue = {"stepdefinitions"},
    tags = "@navigation",
    plugin = {"pretty", "html:target/navigation-report.html", "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"}
)
public class NavigationRunner extends AbstractTestNGCucumberTests {
}
