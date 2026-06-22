package com.orion.tests;

import com.orion.utils.ConfigReader;
import java.time.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import com.orion.pages.LoginPage;

public class BaseTest {
    protected WebDriver driver;
    private static final Logger logger = LogManager.getLogger(BaseTest.class);

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        String browser = ConfigReader.getProperty("browser", "chrome").toLowerCase();
        boolean headless = Boolean.parseBoolean(ConfigReader.getProperty("headless", "false"));
        boolean reuseBrowser = Boolean.parseBoolean(ConfigReader.getProperty("reuse.browser", "true"));
        boolean forceLogin = Boolean.parseBoolean(ConfigReader.getProperty("force.login", "false"));
        boolean connected = false;

        logger.info("Setting up WebDriver. Browser: {}, Headless: {}, ReuseBrowser: {}, ForceLogin: {}", 
                    browser, headless, reuseBrowser, forceLogin);

        if (browser.equals("chrome") && reuseBrowser && !forceLogin) {
            try {
                ChromeOptions options = new ChromeOptions();
                options.setExperimentalOption("debuggerAddress", "127.0.0.1:9222");
                driver = new ChromeDriver(options);
                // Verify session is responsive and has an active window
                driver.getCurrentUrl();
                connected = true;
                logger.info("Successfully connected to existing Chrome session at 127.0.0.1:9222");
            } catch (Exception e) {
                logger.warn("Could not connect to existing Chrome session or target window is closed. Error: {}. Starting a new one...", e.getMessage());
                if (driver != null) {
                    try {
                        driver.quit();
                    } catch (Exception ignored) {}
                    driver = null;
                }
            }
        }

        if (!connected) {
            switch (browser) {
                case "chrome":
                    ChromeOptions chromeOptions = new ChromeOptions();
                    chromeOptions.addArguments("--remote-allow-origins=*");
                    if (reuseBrowser) {
                        chromeOptions.addArguments("--remote-debugging-port=9222");
                        String profilePath = System.getProperty("user.dir") + "/target/chrome-profile";
                        chromeOptions.addArguments("--user-data-dir=" + profilePath);
                        logger.info("Configured Chrome to launch with debugging port 9222 and user-data-dir: {}", profilePath);
                    }
                    if (headless) {
                        chromeOptions.addArguments("--headless=new");
                    }
                    driver = new ChromeDriver(chromeOptions);
                    break;
                case "firefox":
                    FirefoxOptions firefoxOptions = new FirefoxOptions();
                    if (headless) {
                        firefoxOptions.addArguments("-headless");
                    }
                    driver = new FirefoxDriver(firefoxOptions);
                    break;
                case "edge":
                    EdgeOptions edgeOptions = new EdgeOptions();
                    if (headless) {
                        edgeOptions.addArguments("--headless=new");
                    }
                    driver = new EdgeDriver(edgeOptions);
                    break;
                default:
                    logger.error("Unsupported browser type: {}. Defaulting to Chrome.", browser);
                    ChromeOptions defOptions = new ChromeOptions();
                    defOptions.addArguments("--remote-allow-origins=*");
                    driver = new ChromeDriver(defOptions);
                    break;
            }

            // Navigate to URL only for newly created driver sessions
            String url = ConfigReader.getProperty("url");
            logger.info("Navigating to application URL: {}", url);
            driver.get(url);
        } else {
            // Check if we need to navigate (if the browser is blank/empty)
            String currentUrl = "";
            try {
                currentUrl = driver.getCurrentUrl();
            } catch (Exception ignored) {}

            if (currentUrl == null || currentUrl.isEmpty() || currentUrl.equals("about:blank")) {
                String url = ConfigReader.getProperty("url");
                logger.info("Browser is blank. Navigating to application URL: {}", url);
                driver.get(url);
            } else {
                logger.info("Reusing browser already on page: {}", currentUrl);
            }
        }

        // Configure basic browser behaviors
        try {
            driver.manage().window().maximize();
        } catch (Exception e) {
            logger.warn("Could not maximize window. Error: {}", e.getMessage());
        }
        
        int implicitWait = Integer.parseInt(ConfigReader.getProperty("implicit.wait", "10"));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));

        // Auto login if not already logged in
        LoginPage loginPage = new LoginPage(driver);
        if (!loginPage.isLoggedIn()) {
            logger.info("User is not logged in. Performing login flow...");
            loginPage.login("weavers_test@norleegroup.com", "4bj\"u>T=h4AZ");
        } else {
            logger.info("User is already logged in. Skipping login flow.");
        }
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        boolean reuseBrowser = Boolean.parseBoolean(ConfigReader.getProperty("reuse.browser", "true"));
        if (driver != null) {
            if (reuseBrowser) {
                logger.info("Reusing browser option is enabled. Leaving browser open.");
            } else {
                logger.info("Quitting WebDriver instance.");
                driver.quit();
            }
        }
    }

    /**
     * Exposes the active WebDriver instance.
     * This is retrieved by ExtentReportManager to capture screenshots on failures.
     */
    public WebDriver getDriver() {
        return this.driver;
    }
}
