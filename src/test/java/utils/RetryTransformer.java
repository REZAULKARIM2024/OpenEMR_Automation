package utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

/**
 * Applies RetryAnalyzer to every @Test method in the suite automatically,
 * including the single generated runScenario(...) test method that
 * Cucumber's AbstractTestNGCucumberTests produces -- that method isn't
 * written by us, so we can't put @Test(retryAnalyzer = ...) on it directly.
 * Registering this class as a <listener> in the suite XML is the standard
 * TestNG mechanism for injecting a retry analyzer without touching
 * generated or third-party test methods.
 */
public class RetryTransformer implements IAnnotationTransformer {

    @Override
    public void transform(ITestAnnotation annotation, Class testClass,
                           Constructor testConstructor, Method testMethod) {
        annotation.setRetryAnalyzer(RetryAnalyzer.class);
    }
}
