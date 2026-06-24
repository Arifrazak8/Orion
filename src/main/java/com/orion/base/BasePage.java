package com.orion.base;

import com.orion.utils.ConfigReader;
import java.time.Duration;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class BasePage {
    protected WebDriver driver;
    protected WebDriverWait wait;
    private static final Logger logger = LogManager.getLogger(BasePage.class);

    public BasePage(WebDriver driver) {
        this.driver = driver;
        int explicitWaitSeconds = Integer.parseInt(ConfigReader.getProperty("explicit.wait", "15"));
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(explicitWaitSeconds));
        org.openqa.selenium.support.PageFactory.initElements(driver, this);
    }

    /**
     * Wait for element to be visible on the DOM and viewport.
     */
    protected WebElement waitForVisibility(By locator) {
        logger.debug("Waiting for visibility of element: {}", locator);
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /**
     * Wait for element to be visible on the DOM and viewport.
     */
    protected WebElement waitForVisibility(WebElement element) {
        logger.debug("Waiting for visibility of element: {}", element);
        return wait.until(ExpectedConditions.visibilityOf(element));
    }

    /**
     * Wait for element to be clickable.
     */
    protected WebElement waitForClickability(By locator) {
        logger.debug("Waiting for clickability of element: {}", locator);
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    /**
     * Wait for element to be clickable.
     */
    protected WebElement waitForClickability(WebElement element) {
        logger.debug("Waiting for clickability of element: {}", element);
        return wait.until(ExpectedConditions.elementToBeClickable(element));
    }

    /**
     * Wrapper click method with built-in logging and wait.
     */
    public void click(By locator) {
        try {
            waitForClickability(locator).click();
            logger.info("Clicked on element: {}", locator);
        } catch (Exception e) {
            logger.error("Failed to click on element: {}", locator, e);
            throw e;
        }
    }

    /**
     * Wrapper click method with built-in logging and wait.
     */
    public void click(WebElement element) {
        try {
            waitForClickability(element).click();
            logger.info("Clicked on element: {}", element);
        } catch (Exception e) {
            logger.error("Failed to click on element: {}", element, e);
            throw e;
        }
    }

    /**
     * Wrapper sendKeys method with built-in logging and wait.
     */
    public void sendKeys(By locator, String text) {
        try {
            WebElement element = waitForVisibility(locator);
            element.clear();
            element.sendKeys(text);
            logger.info("Typed '{}' into element: {}", text, locator);
        } catch (Exception e) {
            logger.error("Failed to type text into element: {}", locator, e);
            throw e;
        }
    }

    /**
     * Wrapper sendKeys method with built-in logging and wait.
     */
    public void sendKeys(WebElement element, String text) {
        try {
            WebElement el = waitForVisibility(element);
            el.clear();
            el.sendKeys(text);
            logger.info("Typed '{}' into element: {}", text, element);
        } catch (Exception e) {
            logger.error("Failed to type text into element: {}", element, e);
            throw e;
        }
    }

    /**
     * Wrapper getText method with built-in logging and wait.
     */
    public String getText(By locator) {
        try {
            String text = waitForVisibility(locator).getText();
            logger.info("Retrieved text '{}' from element: {}", text, locator);
            return text;
        } catch (Exception e) {
            logger.error("Failed to retrieve text from element: {}", locator, e);
            throw e;
        }
    }

    /**
     * Wrapper getText method with built-in logging and wait.
     */
    public String getText(WebElement element) {
        try {
            String text = waitForVisibility(element).getText();
            logger.info("Retrieved text '{}' from element: {}", text, element);
            return text;
        } catch (Exception e) {
            logger.error("Failed to retrieve text from element: {}", element, e);
            throw e;
        }
    }

    /**
     * Checks if the element is currently visible on the page.
     */
    public boolean isDisplayed(By locator) {
        try {
            boolean displayed = waitForVisibility(locator).isDisplayed();
            logger.info("Element {} is displayed: {}", locator, displayed);
            return displayed;
        } catch (Exception e) {
            logger.warn("Element {} is not displayed/found within the timeout", locator);
            return false;
        }
    }

    /**
     * Checks if the element is currently visible on the page.
     */
    public boolean isDisplayed(WebElement element) {
        try {
            boolean displayed = waitForVisibility(element).isDisplayed();
            logger.info("Element {} is displayed: {}", element, displayed);
            return displayed;
        } catch (Exception e) {
            logger.warn("Element {} is not displayed/found within the timeout", element);
            return false;
        }
    }

    /**
     * Gets the page title.
     */
    public String getPageTitle() {
        String title = driver.getTitle();
        logger.info("Current Page Title: {}", title);
        return title;
    }

    /**
     * Checks if the user is currently logged in.
     * 
     * @return true if the user is logged in, false if not (e.g. on the login page)
     */
    public boolean isLoggedIn() {
        try {
            String currentUrl = driver.getCurrentUrl();
            logger.info("Current URL check for login status: {}", currentUrl);
            return currentUrl != null && !currentUrl.contains("/login");
        } catch (Exception e) {
            logger.warn("Failed to check if user is logged in. Assuming not logged in. Error: {}", e.getMessage());
            return false;
        }
    }

    // =========================================================================
    // Enhanced utility methods (added for enterprise framework)
    // =========================================================================

    /**
     * Executes arbitrary JavaScript against the current page.
     *
     * @param script JavaScript code to execute.
     * @param args   Optional arguments accessible as {@code arguments[0..n]} in the script.
     * @return The value returned by the script, or {@code null}.
     */
    public Object executeJavaScript(String script, Object... args) {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            Object result = js.executeScript(script, args);
            logger.debug("Executed JS: {}", script.length() > 80 ? script.substring(0, 80) + "..." : script);
            return result;
        } catch (Exception e) {
            logger.error("Failed to execute JavaScript: {}", script, e);
            throw e;
        }
    }

    /**
     * Scrolls the viewport so the given element is visible.
     *
     * @param element Element to scroll into view.
     */
    public void scrollToElement(WebElement element) {
        try {
            executeJavaScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", element);
            logger.debug("Scrolled to element: {}", element);
        } catch (Exception e) {
            logger.warn("scrollToElement failed: {}", e.getMessage());
        }
    }

    /**
     * Scrolls the viewport so the element identified by the locator is visible.
     *
     * @param locator Locator identifying the target element.
     */
    public void scrollToElement(By locator) {
        scrollToElement(driver.findElement(locator));
    }

    /**
     * Clicks an element using JavaScript — useful when the standard click is
     * intercepted by an overlay.
     *
     * @param element Element to click.
     */
    public void jsClick(WebElement element) {
        try {
            executeJavaScript("arguments[0].click();", element);
            logger.info("JS-clicked on element: {}", element);
        } catch (Exception e) {
            logger.error("Failed to JS-click on element: {}", element, e);
            throw e;
        }
    }

    /**
     * Clicks an element using JavaScript.
     */
    public void jsClick(By locator) {
        jsClick(driver.findElement(locator));
    }

    /**
     * Temporarily highlights an element with a coloured border for visual
     * debugging (screenshot / demo purposes).
     *
     * @param element Element to highlight.
     * @param color   CSS border colour (e.g. "red", "#00FF00").
     */
    public void highlightElement(WebElement element, String color) {
        try {
            String originalStyle = element.getAttribute("style");
            executeJavaScript(
                    "arguments[0].setAttribute('style', arguments[1] + '; border: 3px solid " + color + " !important;');",
                    element, originalStyle != null ? originalStyle : "");
            logger.debug("Highlighted element with color: {}", color);
        } catch (Exception e) {
            logger.debug("Could not highlight element: {}", e.getMessage());
        }
    }

    /**
     * Highlights an element with a red border (convenience overload).
     */
    public void highlightElement(WebElement element) {
        highlightElement(element, "red");
    }

    /**
     * Switches the WebDriver focus to an iframe.
     *
     * @param frameLocator Locator for the iframe element.
     */
    public void switchToFrame(By frameLocator) {
        try {
            wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(frameLocator));
            logger.info("Switched to frame: {}", frameLocator);
        } catch (Exception e) {
            logger.error("Failed to switch to frame: {}", frameLocator, e);
            throw e;
        }
    }

    /**
     * Switches the WebDriver focus to an iframe by WebElement.
     */
    public void switchToFrame(WebElement frameElement) {
        try {
            wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(frameElement));
            logger.info("Switched to frame element");
        } catch (Exception e) {
            logger.error("Failed to switch to frame element", e);
            throw e;
        }
    }

    /**
     * Switches back to the default (top-level) content.
     */
    public void switchToDefaultContent() {
        driver.switchTo().defaultContent();
        logger.debug("Switched back to default content");
    }

    /**
     * Accepts (clicks OK on) a browser alert if one is present.
     *
     * @return The alert text, or {@code null} if no alert was present.
     */
    public String acceptAlert() {
        try {
            wait.until(ExpectedConditions.alertIsPresent());
            Alert alert = driver.switchTo().alert();
            String alertText = alert.getText();
            alert.accept();
            logger.info("Accepted alert with text: '{}'", alertText);
            return alertText;
        } catch (TimeoutException | NoAlertPresentException e) {
            logger.debug("No alert present to accept");
            return null;
        }
    }

    /**
     * Dismisses (clicks Cancel on) a browser alert if one is present.
     *
     * @return The alert text, or {@code null} if no alert was present.
     */
    public String dismissAlert() {
        try {
            wait.until(ExpectedConditions.alertIsPresent());
            Alert alert = driver.switchTo().alert();
            String alertText = alert.getText();
            alert.dismiss();
            logger.info("Dismissed alert with text: '{}'", alertText);
            return alertText;
        } catch (TimeoutException | NoAlertPresentException e) {
            logger.debug("No alert present to dismiss");
            return null;
        }
    }

    /**
     * Waits for the page to fully load (document.readyState == "complete").
     */
    public void waitForPageLoad() {
        try {
            wait.until(d -> ((JavascriptExecutor) d)
                    .executeScript("return document.readyState").equals("complete"));
            logger.debug("Page load complete");
        } catch (TimeoutException e) {
            logger.warn("Page load wait timed out");
        }
    }

    /**
     * Retrieves the value attribute of an element (useful for input fields).
     *
     * @param element The input element.
     * @return The value attribute text.
     */
    public String getValue(WebElement element) {
        try {
            String value = waitForVisibility(element).getAttribute("value");
            logger.info("Retrieved value '{}' from element: {}", value, element);
            return value;
        } catch (Exception e) {
            logger.error("Failed to retrieve value from element: {}", element, e);
            throw e;
        }
    }

    /**
     * Retrieves the value attribute of an element.
     */
    public String getValue(By locator) {
        return getValue(driver.findElement(locator));
    }

    /**
     * Finds all elements matching the locator.
     *
     * @param locator Element locator.
     * @return List of matching WebElements (may be empty).
     */
    public List<WebElement> findElements(By locator) {
        return driver.findElements(locator);
    }

    /**
     * Gets the current page URL.
     *
     * @return Current URL string.
     */
    public String getCurrentUrl() {
        String url = driver.getCurrentUrl();
        logger.debug("Current URL: {}", url);
        return url;
    }

    /**
     * Navigates the browser to the specified URL.
     *
     * @param url Target URL.
     */
    public void navigateTo(String url) {
        logger.info("Navigating to: {}", url);
        driver.get(url);
        waitForPageLoad();
    }
}
