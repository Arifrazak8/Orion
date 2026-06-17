package com.orion.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConfigReader {
    private static final Logger logger = LogManager.getLogger(ConfigReader.class);
    private static Properties properties;

    static {
        try {
            // Check for file in src/test/resources/config.properties
            String filePath = "src/test/resources/config.properties";
            FileInputStream fileInputStream = new FileInputStream(filePath);
            properties = new Properties();
            properties.load(fileInputStream);
            fileInputStream.close();
            logger.info("Configuration properties loaded successfully from: {}", filePath);
        } catch (IOException e) {
            logger.error("Failed to load config.properties file. Please check if the file exists.", e);
            throw new RuntimeException("Could not load configuration file.", e);
        }
    }

    public static String getProperty(String key) {
        String value = properties.getProperty(key);
        if (value != null) {
            return value.trim();
        } else {
            logger.warn("Property for key '{}' was not found in config.properties", key);
            return null;
        }
    }

    public static String getProperty(String key, String defaultValue) {
        String value = getProperty(key);
        return value != null ? value : defaultValue;
    }
}
