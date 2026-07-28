package runners;

@io.cucumber.testng.CucumberOptions(
    features = "src/test/resources/features",
    glue = {"stepdefinitions", "hooks"},
    tags = "@regression",
    plugin = {"pretty", "html:target/regression-report.html", "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"}
)
public class RegressionRunner extends io.cucumber.testng.AbstractTestNGCucumberTests {
}