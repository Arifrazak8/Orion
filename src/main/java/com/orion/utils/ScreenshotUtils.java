package com.orion.utils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

public class ScreenshotUtils {
    private static final Logger logger = LogManager.getLogger(ScreenshotUtils.class);

    /**
     * Captures a screenshot of the current browser state and saves it to the configured directory.
     * @param driver The active WebDriver instance.
     * @param screenshotName The base name for the screenshot file.
     * @return The absolute path to the saved screenshot file, or null if failed.
     */
    public static String captureScreenshot(WebDriver driver, String screenshotName) {
        if (driver == null) {
            logger.warn("WebDriver is null; cannot capture screenshot for: {}", screenshotName);
            return null;
        }

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String folderPath = ConfigReader.getProperty("screenshot.path", "./screenshots/");
        
        // Ensure folder ends with / or \
        if (!folderPath.endsWith("/") && !folderPath.endsWith("\\")) {
            folderPath += "/";
        }

        // Ensure target directory exists
        File directory = new File(folderPath);
        if (!directory.exists()) {
            if (directory.mkdirs()) {
                logger.info("Screenshot directory created at: {}", folderPath);
            }
        }

        String fileName = screenshotName + "_" + timestamp + ".png";
        String destinationPath = folderPath + fileName;
        File destinationFile = new File(destinationPath);

        try {
            TakesScreenshot ts = (TakesScreenshot) driver;
            File sourceFile = ts.getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(sourceFile, destinationFile);
            
            String absolutePath = destinationFile.getAbsolutePath();
            logger.info("Screenshot captured and saved to: {}", absolutePath);
            return absolutePath;
        } catch (IOException e) {
            logger.error("Failed to capture and save screenshot: {}", fileName, e);
            return null;
        }
    }
}
