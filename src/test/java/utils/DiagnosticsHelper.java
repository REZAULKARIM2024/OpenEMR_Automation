package utils;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Every menu-navigation failure we've seen against the live demo so far has
 * only told us "the element wasn't there" -- never what page we were
 * actually looking at. Two full runs (local and CI) failed on every single
 * top-level menu item (Patient, Admin, Calendar, Messages, Reports, Fees,
 * Documents) at once, which strongly suggests we're not landing on the real
 * post-login dashboard at all (e.g. a forced password-change or lockout
 * interstitial instead), rather than seven locators breaking simultaneously.
 *
 * describePage() captures the URL, title, and a short snippet of visible
 * body text at the moment a wait times out, so the *next* failure message
 * answers that question directly -- no manual browser inspection or Chrome
 * extension needed.
 */
public class DiagnosticsHelper {

    private static final int BODY_SNIPPET_LENGTH = 500;

    public static String describePage(WebDriver driver) {
        String url;
        String title;
        String bodySnippet;

        try {
            url = driver.getCurrentUrl();
        } catch (Exception e) {
            url = "<unavailable: " + e.getMessage() + ">";
        }

        try {
            title = driver.getTitle();
        } catch (Exception e) {
            title = "<unavailable>";
        }

        try {
            String bodyText = driver.findElement(By.tagName("body")).getText();
            String normalized = bodyText.replaceAll("\\s+", " ").trim();
            bodySnippet = normalized.length() > BODY_SNIPPET_LENGTH
                    ? normalized.substring(0, BODY_SNIPPET_LENGTH) + "..."
                    : normalized;
        } catch (Exception e) {
            bodySnippet = "<unavailable: " + e.getMessage() + ">";
        }

        return String.format(
                "%n  >>> DIAGNOSTIC (actual page at failure time) -- URL: %s | Title: %s | Visible body text (first %d chars): %s%n",
                url, title, BODY_SNIPPET_LENGTH, bodySnippet
        );
    }
}
