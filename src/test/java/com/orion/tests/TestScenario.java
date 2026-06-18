package com.orion.tests;

import com.orion.pages.MenuPage;
import com.orion.pages.SalesPipelinePage;
import com.orion.constants.TableConstants;
import com.orion.utils.DatabaseUtils;

import java.sql.SQLException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestScenario extends BaseTest {
    private static final Logger logger = LogManager.getLogger(TestScenario.class);

    @Test(description = "Verify successful login and validate dynamic table cells on Sales Pipeline")
    public void testSuccessfulLogin() throws SQLException {
        logger.info("Executing testSuccessfulLogin...");

        MenuPage menuPage = new MenuPage(driver);
        SalesPipelinePage salesPipelinePage = menuPage.clickSalesPipeline();   

        // Define division and column dynamically using Constants
        String division = TableConstants.DIVISION_MULTI_FAMILY;
        String column = TableConstants.COLUMN_CONFIRM_50;

        // Fetch the value using TableUtils via SalesPipelinePage
        String value = salesPipelinePage.getWeightedPipelineSummaryValue(division, column);

        logger.info("Fetched value for division '{}' and column '{}': {}", division, column, value);

        // Fetch the expected value from the database
        DatabaseUtils db = new DatabaseUtils();
        db.connect();
        String expectedDbValue = null;
        try {
            String query = "SELECT sum_value FROM pipeline_summary WHERE division = ? AND column_name = ?";
            expectedDbValue = db.getSingleValue(query, "sum_value", division, column);

            if (expectedDbValue == null) {
                logger.warn("Database query returned null.");

            }
        } catch (Exception e) {
            logger.error("Failed to fetch value from database. Error: {}", e.getMessage());

        } finally {
            db.disconnect();
        }

        // Assert the UI value matches the DB value
        Assert.assertEquals(value, expectedDbValue, "Value for " + division + " under " + column + " did not match!");
    }
}
