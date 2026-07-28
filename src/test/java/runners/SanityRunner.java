package runners;

@io.cucumber.testng.CucumberOptions(
    features = "src/test/resources/features",
    glue = {"stepdefinitions", "hooks"},
    tags = "@sanity",
    plugin = {"pretty", "html:target/sanity-report.html", "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"}
)
public class SanityRunner extends io.cucumber.testng.AbstractTestNGCucumberTests {
}