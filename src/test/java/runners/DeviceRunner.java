package runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions(
    features = "src/test/resources/features",
    glue = {"stepdefinitions"},
    tags = "@device",
    plugin = {"pretty", "html:target/device-report.html"}
)
public class DeviceRunner extends AbstractTestNGCucumberTests {
}
