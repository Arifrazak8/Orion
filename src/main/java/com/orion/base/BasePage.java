package com.orion.base;

import com.orion.utils.ConfigReader;
import java.time.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
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
    }

    /**
     * Wait for element to be visible on the DOM and viewport.
     */
    protected WebElement waitForVisibility(By locator) {
        logger.debug("Waiting for visibility of element: {}", locator);
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /**
     * Wait for element to be clickable.
     */
    protected WebElement waitForClickability(By locator) {
        logger.debug("Waiting for clickability of element: {}", locator);
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
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
     * Checks if the element is currently visible on the page.
     */
    public boolean isDisplayed(By internetSecureAreaLogoutLink) {
        try {
            boolean displayed = waitForVisibility(internetSecureAreaLogoutLink).isDisplayed();
            logger.info("Element {} is displayed: {}", internetSecureAreaLogoutLink, displayed);
            return displayed;
        } catch (Exception e) {
            logger.warn("Element {} is not displayed/found within the timeout", internetSecureAreaLogoutLink);
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
}
