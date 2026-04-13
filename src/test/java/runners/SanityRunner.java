package runners;

@io.cucumber.testng.CucumberOptions(
    features = "src/test/resources/features",
    glue = {"stepdefinitions", "hooks"},
    tags = "@sanity",
    plugin = {"pretty", "html:target/sanity-report.html"}
)
public class SanityRunner extends io.cucumber.testng.AbstractTestNGCucumberTests {
}