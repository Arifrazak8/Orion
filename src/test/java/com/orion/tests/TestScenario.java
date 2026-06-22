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

        // Fetch the values for debugging and verification
        String value = salesPipelinePage.getWeightedPipelineSummaryValue(division, column);

        logger.info("Fetched UI value for division '{}' and column '{}': {}", division, column, value);

        // Fetch the expected value from the database
        DatabaseUtils db = new DatabaseUtils();
        db.connect();
        String expectedDbValue = null;
        try {
            String query = """
                    SELECT SUM(CAST(p.job_amount AS DECIMAL(18,2))) AS total_job_amount
                    FROM pipeline_bids p
                    INNER JOIN (
                        SELECT MAX(id) AS latest_id
                        FROM pipeline_bids
                        WHERE business_unit_id = 3
                        GROUP BY COALESCE(
                            NULLIF(bid_id,''),
                            NULLIF(buildingconnected_id,'')
                        )
                    ) latest
                    ON p.id = latest.latest_id;
                    """;
            expectedDbValue = db.getSingleValue(query, "total_job_amount");
            System.out.println(expectedDbValue);

            if (expectedDbValue != null) {
                logger.info("Database query returned total_job_amount: {}", expectedDbValue);
            } else {
                logger.warn("Database query returned null.");
            }
        } catch (Exception e) {
            logger.error("Failed to fetch value from database. Error: {}", e.getMessage(), e);
        } finally {
            db.disconnect();
        }

        // Format expected DB value if needed to match UI formatting (removing decimals if UI shows whole numbers, adding commas)
        if (expectedDbValue != null && value != null) {
            try {
                double dbValDouble = Double.parseDouble(expectedDbValue);
                // Convert double to integer string representation with commas if needed, or format
                java.text.DecimalFormat df = new java.text.DecimalFormat("#,###");
                String formattedDbValue = df.format(dbValDouble);
                logger.info("Formatted DB expected value: {} vs UI value: {}", formattedDbValue, value);
                Assert.assertEquals(value, formattedDbValue, "Value for " + division + " under " + column + " did not match!");
            } catch (NumberFormatException e) {
                Assert.assertEquals(value, expectedDbValue, "Value for " + division + " under " + column + " did not match!");
            }
        } else {
            Assert.assertEquals(value, expectedDbValue, "Value for " + division + " under " + column + " did not match!");
        }
    }
}
