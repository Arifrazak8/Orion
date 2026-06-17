package com.orion.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import java.io.File;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class ExtentReportManager implements ITestListener {
    private static final Logger logger = LogManager.getLogger(ExtentReportManager.class);
    private static ExtentReports extent;
    private static ThreadLocal<ExtentTest> testLogger = new ThreadLocal<>();

    @Override
    public void onStart(ITestContext context) {
        logger.info("Initializing Extent Reports...");
        String reportPath = ConfigReader.getProperty("report.path", "./reports/ExtentReport.html");
        
        // Ensure report folder exists
        File reportFile = new File(reportPath);
        File parentDir = reportFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportPath);
        sparkReporter.config().setDocumentTitle("Orion CRM Test Automation Report");
        sparkReporter.config().setReportName("Functional Web Test Execution Report");
        sparkReporter.config().setTheme(Theme.DARK);

        extent = new ExtentReports();
        extent.attachReporter(sparkReporter);
        extent.setSystemInfo("Host Name", System.getProperty("user.name"));
        extent.setSystemInfo("Environment", "QA Staging");
        extent.setSystemInfo("Browser", ConfigReader.getProperty("browser", "Chrome"));
        extent.setSystemInfo("Operating System", System.getProperty("os.name"));
    }

    @Override
    public void onTestStart(ITestResult result) {
        logger.info("Test Started: {}", result.getName());
        ExtentTest test = extent.createTest(result.getMethod().getMethodName(), result.getMethod().getDescription());
        // Assign categories based on TestNG groups
        String[] groups = result.getMethod().getGroups();
        for (String group : groups) {
            test.assignCategory(group);
        }
        testLogger.set(test);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        logger.info("Test Passed: {}", result.getName());
        testLogger.get().log(Status.PASS, "Test Case Passed: " + result.getName());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        logger.error("Test Failed: {}", result.getName());
        testLogger.get().log(Status.FAIL, "Test Case Failed: " + result.getName());
        testLogger.get().log(Status.FAIL, result.getThrowable());

        WebDriver driver = null;
        try {
            // Retrieve driver instance dynamically from running test class instance
            Object testClassInstance = result.getInstance();
            
            // Using reflection to get driver to avoid circular compile time dependencies if needed, 
            // but we can also cast directly or check for getDriver method.
            java.lang.reflect.Method getDriverMethod = testClassInstance.getClass().getMethod("getDriver");
            driver = (WebDriver) getDriverMethod.invoke(testClassInstance);
        } catch (Exception e) {
            logger.warn("Could not retrieve WebDriver instance from test class for screenshot capture: {}", e.getMessage());
        }

        if (driver != null) {
            String screenshotPath = ScreenshotUtils.captureScreenshot(driver, result.getName());
            if (screenshotPath != null) {
                // Attach screenshot using the absolute path (or standard file path)
                testLogger.get().addScreenCaptureFromPath(screenshotPath, "Failure Screenshot");
            }
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        logger.warn("Test Skipped: {}", result.getName());
        testLogger.get().log(Status.SKIP, "Test Case Skipped: " + result.getName());
        testLogger.get().log(Status.SKIP, result.getThrowable());
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        // Not implemented
    }

    @Override
    public void onFinish(ITestContext context) {
        logger.info("Test execution finished. Flushing Extent Reports...");
        if (extent != null) {
            extent.flush();
        }
    }

    /**
     * Gets the ExtentTest instance for logging custom messages inside tests.
     */
    public static ExtentTest getTest() {
        return testLogger.get();
    }
}
