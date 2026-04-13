package runners;

@io.cucumber.testng.CucumberOptions(
    features = "src/test/resources/features",
    glue = {"stepdefinitions", "hooks"},
    tags = "@regression",
    plugin = {"pretty", "html:target/regression-report.html"}
)
public class RegressionRunner extends io.cucumber.testng.AbstractTestNGCucumberTests {
}