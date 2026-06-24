package com.orion.tests;

import com.orion.pages.PivotReportPage;
import com.orion.utils.DatabaseUtils;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;

public class PivotReportPositiveTableTest extends BaseTest {
    private static final Logger logger = LogManager.getLogger(PivotReportPositiveTableTest.class);

    @Test(description = "Verify cells in the Positive table in Pivot Report match the database values")
    public void testPositiveTableCellsMatchDatabase() throws Exception {
        logger.info("Executing testPositiveTableCellsMatchDatabase...");

        // 1. Navigate to the Pivot Summary page
        String pivotUrl = "https://stag-crm.norlee.io/report/pivot-summary";
        logger.info("Navigating directly to Pivot Summary: {}", pivotUrl);
        driver.get(pivotUrl);
        Thread.sleep(10000);
        // 2. Select filters
        PivotReportPage pivotPage = new PivotReportPage(driver);
        // logger.info("Selecting Business Unit: MULTI_FAMILY");
        pivotPage.clickClearFilter();
        try {
            Thread.sleep(2000);
        } catch (Exception e) {
        } // Wait for clear filter reload

        pivotPage.selectBusinessUnit(PivotReportPage.BusinessUnit.COMMERCIAL);
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
        } // Wait for select2 reload

        logger.info("Selecting Duration: LAST_THIRTY_DAYS");
        pivotPage.selectDuration(PivotReportPage.DurationType.LAST_THIRTY_DAYS);
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
        }

        pivotPage.clickGenerateReport();

        Thread.sleep(10000);

        logger.info("Toggling 'Treat LOI as Awarded' to true");

        WebElement toggleInput = driver.findElement(By.xpath("//label[@for='toggleLOI']"));
        toggleInput.click();

        WebDriverWait wait = new WebDriverWait(driver, java.time.Duration.ofSeconds(15));
        wait.ignoring(org.openqa.selenium.StaleElementReferenceException.class)
                .until(org.openqa.selenium.support.ui.ExpectedConditions
                        .visibilityOfElementLocated(By.id("positive-total-cell")));

        try {
            new WebDriverWait(driver, java.time.Duration.ofSeconds(10))
                    .ignoring(org.openqa.selenium.StaleElementReferenceException.class)
                    .until(d -> {
                        String text = d.findElement(By.id("positive-total-cell")).getText().trim();
                        return !text.equals("$0") && !text.isEmpty();
                    });
        } catch (org.openqa.selenium.TimeoutException e) {
            logger.info("Wait timed out or positive total cell is genuinely $0.");
        }

        String uiTotalText = "";
        for (int i = 0; i < 3; i++) {
            try {
                WebElement totalCell = driver.findElement(By.id("positive-total-cell"));
                uiTotalText = totalCell.getText().trim();
                break;
            } catch (org.openqa.selenium.StaleElementReferenceException e) {
                logger.info("Encountered stale element while retrieving text, retrying (Attempt " + (i + 1) + ")");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        logger.info("UI Positive Table Total: '{}'", uiTotalText);

        // Clean UI total for comparison (e.g. "$74,665,217" -> "74665217")
        String uiTotalClean = uiTotalText.replace("$", "").replace(",", "").trim();
        if (uiTotalClean.contains(".")) {
            uiTotalClean = uiTotalClean.substring(0, uiTotalClean.indexOf("."));
        }

        // 4. Fetch the expected sum from the database for verification
        DatabaseUtils db = new DatabaseUtils();
        db.connect();
        try {
            String query = """
                    SELECT
                        SUM(CAST(t.job_amount AS DECIMAL(18,2))) AS total_job_amount
                    FROM (
                        SELECT
                            p.*,
                            ROW_NUMBER() OVER (
                                PARTITION BY COALESCE(
                                    NULLIF(p.bid_id,''),
                                    NULLIF(p.buildingconnected_id,'')
                                )
                                ORDER BY p.date DESC, p.id DESC
                            ) AS rn
                        FROM pipeline_bids p
                        WHERE p.business_unit_id = 3
                    ) t
                    WHERE t.rn = 1
                      AND t.status = 8
                      AND t.date >= CURDATE() - INTERVAL 30 DAY
                    """;
            List<Map<String, String>> dbRows = db.executeQuery(query);
            Assert.assertFalse(dbRows.isEmpty(), "Database query returned no rows.");

            String dbTotalValStr = dbRows.get(0).get("total_job_amount");
            String dbTotalClean = "0";
            if (dbTotalValStr != null && !dbTotalValStr.trim().isEmpty()) {
                dbTotalClean = dbTotalValStr.trim();
                if (dbTotalClean.contains(".")) {
                    dbTotalClean = dbTotalClean.substring(0, dbTotalClean.indexOf("."));
                }
            }

            logger.info("DB Positive Table Total (Cleaned): '{}'", dbTotalClean);
            logger.info("UI Positive Table Total (Cleaned): '{}'", uiTotalClean);

            // Assertions
            Assert.assertEquals(uiTotalClean, dbTotalClean,
                    "Total Job Amount mismatch! Expected (DB): " + dbTotalClean
                            + ", Found (UI): " + uiTotalClean);
            logger.info("PASS: UI total matches DB total!");
        } finally {
            db.disconnect();
        }
    }
}
