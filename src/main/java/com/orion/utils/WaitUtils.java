package com.orion.utils;

import com.orion.constants.FrameworkConstants;
import java.time.Duration;
import java.util.function.Function;
import java.util.function.Supplier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;

/**
 * WaitUtils — Enterprise-grade wait utilities replacing {@code Thread.sleep()}
 * with intelligent polling strategies.
 *
 * <p>Provides:
 * <ul>
 *   <li>Fluent waits with configurable timeout and polling interval</li>
 *   <li>Stale-element-aware waiting</li>
 *   <li>AJAX / page-load completion detection</li>
 *   <li>Custom condition waiting with retry logic</li>
 *   <li>Table data stabilization (wait until table content stops changing)</li>
 * </ul>
 */
public final class WaitUtils {

    private static final Logger logger = LogManager.getLogger(WaitUtils.class);

    private WaitUtils() {
        // Utility class — not instantiable
    }

    // -------------------------------------------------------------------------
    // Core fluent wait
    // -------------------------------------------------------------------------

    /**
     * Creates a {@link FluentWait} with sensible defaults: ignores
     * {@link StaleElementReferenceException} and polls every 500ms.
     *
     * @param driver          WebDriver instance.
     * @param timeoutSeconds  Maximum wait time in seconds.
     * @return Configured FluentWait.
     */
    public static FluentWait<WebDriver> fluentWait(WebDriver driver, int timeoutSeconds) {
        return new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(timeoutSeconds))
                .pollingEvery(Duration.ofMillis(FrameworkConstants.DEFAULT_POLL_INTERVAL_MS))
                .ignoring(StaleElementReferenceException.class);
    }

    /**
     * Creates a FluentWait with the framework's default explicit wait timeout.
     */
    public static FluentWait<WebDriver> fluentWait(WebDriver driver) {
        int timeout = Integer.parseInt(
                ConfigReader.getProperty("explicit.wait",
                        String.valueOf(FrameworkConstants.DEFAULT_EXPLICIT_WAIT)));
        return fluentWait(driver, timeout);
    }

    // -------------------------------------------------------------------------
    // Element-specific waits
    // -------------------------------------------------------------------------

    /**
     * Waits for an element to be visible, tolerating stale-element references.
     */
    public static WebElement waitForVisibility(WebDriver driver, By locator, int timeoutSeconds) {
        logger.debug("Waiting for visibility of: {} (timeout: {}s)", locator, timeoutSeconds);
        return fluentWait(driver, timeoutSeconds)
                .until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /**
     * Waits for an element to be visible using default timeout.
     */
    public static WebElement waitForVisibility(WebDriver driver, By locator) {
        return waitForVisibility(driver, locator, FrameworkConstants.DEFAULT_EXPLICIT_WAIT);
    }

    /**
     * Waits for an element to be clickable, tolerating stale-element references.
     */
    public static WebElement waitForClickable(WebDriver driver, By locator, int timeoutSeconds) {
        logger.debug("Waiting for clickability of: {} (timeout: {}s)", locator, timeoutSeconds);
        return fluentWait(driver, timeoutSeconds)
                .until(ExpectedConditions.elementToBeClickable(locator));
    }

    /**
     * Waits for an element's text to become non-empty and not equal to a placeholder.
     *
     * @param locator          Element locator.
     * @param placeholderTexts Text values considered "not yet loaded" (e.g. "$0", "Loading...").
     * @param timeoutSeconds   Maximum wait time.
     * @return The non-placeholder text value.
     */
    public static String waitForNonPlaceholderText(WebDriver driver, By locator,
                                                    int timeoutSeconds, String... placeholderTexts) {
        logger.debug("Waiting for non-placeholder text at: {}", locator);
        return fluentWait(driver, timeoutSeconds)
                .until(d -> {
                    try {
                        String text = d.findElement(locator).getText().trim();
                        if (text.isEmpty()) return null;
                        for (String ph : placeholderTexts) {
                            if (text.equals(ph)) return null;
                        }
                        return text;
                    } catch (StaleElementReferenceException e) {
                        return null;
                    }
                });
    }

    // -------------------------------------------------------------------------
    // Page-level waits
    // -------------------------------------------------------------------------

    /**
     * Waits for the page's {@code document.readyState} to become "complete".
     */
    public static void waitForPageLoad(WebDriver driver, int timeoutSeconds) {
        logger.debug("Waiting for page load (timeout: {}s)", timeoutSeconds);
        fluentWait(driver, timeoutSeconds)
                .until(d -> ((JavascriptExecutor) d)
                        .executeScript("return document.readyState")
                        .equals("complete"));
    }

    /**
     * Waits for page load using the default page-load timeout.
     */
    public static void waitForPageLoad(WebDriver driver) {
        waitForPageLoad(driver, FrameworkConstants.DEFAULT_PAGE_LOAD_TIMEOUT);
    }

    /**
     * Waits for all jQuery AJAX requests to complete (if jQuery is present).
     * Silently returns if jQuery is not loaded on the page.
     */
    public static void waitForAjax(WebDriver driver, int timeoutSeconds) {
        logger.debug("Waiting for AJAX completion (timeout: {}s)", timeoutSeconds);
        try {
            fluentWait(driver, timeoutSeconds)
                    .until(d -> {
                        JavascriptExecutor js = (JavascriptExecutor) d;
                        Object jQueryDefined = js.executeScript(
                                "return typeof jQuery !== 'undefined'");
                        if (Boolean.TRUE.equals(jQueryDefined)) {
                            return (Boolean) js.executeScript("return jQuery.active == 0");
                        }
                        return true; // jQuery not present — nothing to wait for
                    });
        } catch (Exception e) {
            logger.debug("AJAX wait skipped or timed out: {}", e.getMessage());
        }
    }

    /**
     * Waits for Livewire (Laravel) to finish processing.
     */
    public static void waitForLivewire(WebDriver driver, int timeoutSeconds) {
        logger.debug("Waiting for Livewire completion (timeout: {}s)", timeoutSeconds);
        try {
            fluentWait(driver, timeoutSeconds)
                    .until(d -> {
                        JavascriptExecutor js = (JavascriptExecutor) d;
                        Object livewireDefined = js.executeScript(
                                "return typeof window.Livewire !== 'undefined'");
                        if (Boolean.TRUE.equals(livewireDefined)) {
                            // Livewire v3 uses a different API than v2
                            return (Boolean) js.executeScript(
                                    "try { return !document.querySelector('[wire\\\\:loading]:not([style*=\"display: none\"])'); } catch(e) { return true; }");
                        }
                        return true;
                    });
        } catch (Exception e) {
            logger.debug("Livewire wait skipped or timed out: {}", e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Table stabilization
    // -------------------------------------------------------------------------

    /**
     * Waits until the text content of a table element stabilizes (stops changing
     * between two consecutive polls).  Useful after report generation where
     * table data is loaded asynchronously.
     *
     * @param driver         WebDriver instance.
     * @param tableLocator   Locator for the table element.
     * @param timeoutSeconds Maximum wait time.
     */
    public static void waitForTableStabilization(WebDriver driver, By tableLocator,
                                                  int timeoutSeconds) {
        logger.debug("Waiting for table stabilization at: {}", tableLocator);
        final String[] previousContent = {""};
        fluentWait(driver, timeoutSeconds)
                .withMessage("Table content did not stabilize within " + timeoutSeconds + "s")
                .until(d -> {
                    try {
                        String currentContent = d.findElement(tableLocator).getText();
                        if (!currentContent.isEmpty() && currentContent.equals(previousContent[0])) {
                            return true;
                        }
                        previousContent[0] = currentContent;
                        return false;
                    } catch (StaleElementReferenceException e) {
                        previousContent[0] = "";
                        return false;
                    }
                });
        logger.debug("Table content stabilized at: {}", tableLocator);
    }

    // -------------------------------------------------------------------------
    // Generic condition wait with retry
    // -------------------------------------------------------------------------

    /**
     * Repeatedly evaluates a {@link Supplier} until it returns a non-null,
     * non-false value, or the timeout expires.
     *
     * <p>This is the most flexible wait — use it for any custom condition
     * not covered by the specialised methods above.
     *
     * @param <T>            Return type.
     * @param condition      Supplier that returns {@code null} or {@code false} while
     *                       the condition is not yet met, and the desired value once met.
     * @param timeoutSeconds Maximum wait time.
     * @param description    Human-readable description for logging / error messages.
     * @return The first non-null / non-false value returned by the supplier.
     */
    public static <T> T waitForCondition(WebDriver driver, Function<WebDriver, T> condition,
                                          int timeoutSeconds, String description) {
        logger.debug("Waiting for condition: '{}' (timeout: {}s)", description, timeoutSeconds);
        return fluentWait(driver, timeoutSeconds)
                .withMessage(description)
                .until(condition);
    }
}
