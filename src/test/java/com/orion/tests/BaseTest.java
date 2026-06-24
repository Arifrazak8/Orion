package com.orion.tests;

import com.orion.database.ConnectionPoolManager;
import com.orion.reporting.ReportingService;
import com.orion.utils.ConfigReader;
import com.orion.validation.ValidationService;
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
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import com.orion.pages.LoginPage;

public class BaseTest {
    /**
     * ThreadLocal driver for parallel-execution safety.
     * Each test thread gets its own WebDriver instance.
     */
    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();

    /**
     * Backward-compatible field — existing test subclasses that reference
     * {@code driver} directly continue to work.
     */
    protected WebDriver driver;

    private static final Logger logger = LogManager.getLogger(BaseTest.class);

    /** Shared reporting service for accumulating comparison results across tests. */
    private static final ReportingService sharedReportingService = new ReportingService();

    // =========================================================================
    // Suite-level lifecycle — connection pool
    // =========================================================================

    @BeforeSuite(alwaysRun = true)
    public void initSuite() {
        try {
            ConnectionPoolManager.initialize();
            logger.info("Connection pool initialised for test suite.");
        } catch (Exception e) {
            logger.warn("Connection pool initialisation skipped: {}", e.getMessage());
        }
    }

    @AfterSuite(alwaysRun = true)
    public void tearDownSuite() {
        // Generate consolidated Excel report for all accumulated comparisons
        try {
            String reportPath = sharedReportingService.generateConsolidatedExcelReport();
            if (reportPath != null) {
                logger.info("Consolidated Excel comparison report: {}", reportPath);
            }
        } catch (Exception e) {
            logger.warn("Failed to generate consolidated report: {}", e.getMessage());
        }

        // Shutdown connection pool
        try {
            ConnectionPoolManager.shutdown();
            logger.info("Connection pool shut down.");
        } catch (Exception e) {
            logger.warn("Connection pool shutdown error: {}", e.getMessage());
        }
    }

    // =========================================================================
    // Method-level lifecycle — WebDriver setup / teardown
    // =========================================================================

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

        // Store driver in ThreadLocal for parallel safety
        driverThreadLocal.set(driver);

        // Configure basic browser behaviors
        try {
            driver.manage().window().maximize();
        } catch (Exception e) {
            logger.warn("Could not maximize window. Error: {}", e.getMessage());
        }
        
        int implicitWait = Integer.parseInt(ConfigReader.getProperty("implicit.wait", "10"));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));

        // Auto login if not already logged in — credentials from config.properties
        LoginPage loginPage = new LoginPage(driver);
        if (!loginPage.isLoggedIn()) {
            String username = ConfigReader.getProperty("login.username");
            String password = ConfigReader.getProperty("login.password");
            logger.info("User is not logged in. Performing login flow...");
            loginPage.login(username, password);
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
        driverThreadLocal.remove();
    }

    // =========================================================================
    // Public accessors
    // =========================================================================

    /**
     * Exposes the active WebDriver instance.
     * This is retrieved by ExtentReportManager to capture screenshots on failures.
     */
    public WebDriver getDriver() {
        return this.driver;
    }

    /**
     * Returns the thread-local WebDriver (for parallel execution).
     */
    public static WebDriver getThreadDriver() {
        return driverThreadLocal.get();
    }

    /**
     * Provides a pre-configured {@link ValidationService} for subclasses.
     * Uses the shared reporting service so results are accumulated across tests.
     *
     * @return A new ValidationService using the shared ReportingService.
     */
    protected ValidationService getValidationService() {
        return new ValidationService(
                new com.orion.validation.DataCaptureService(),
                new com.orion.validation.TableComparisonEngine(),
                sharedReportingService
        );
    }

    /**
     * Returns the shared reporting service for direct access.
     */
    protected ReportingService getReportingService() {
        return sharedReportingService;
    }
}

