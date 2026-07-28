package runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions(
    features = "src/test/resources/features",
    glue = {"stepdefinitions"},
    tags = "@interrupt",
    plugin = {"pretty", "html:target/interrupt-report.html", "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"}
)
public class InterruptRunner extends AbstractTestNGCucumberTests {
}
