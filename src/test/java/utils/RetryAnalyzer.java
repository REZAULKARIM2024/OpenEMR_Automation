package utils;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * Retries a failed scenario once before letting TestNG report it as failed.
 *
 * demo.openemr.io is a shared public demo instance with no SLA -- an
 * occasional slow page load or a request that lands mid-reload on the
 * server's side is expected noise, not a defect in this framework or in
 * OpenEMR itself. A single retry absorbs that noise without hiding a
 * genuine, repeatable failure (which will fail again on the retry and
 * still get reported).
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    private int attempts = 0;
    private static final int MAX_RETRIES = 1;

    @Override
    public boolean retry(ITestResult result) {
        if (attempts < MAX_RETRIES) {
            attempts++;
            return true;
        }
        return false;
    }
}
