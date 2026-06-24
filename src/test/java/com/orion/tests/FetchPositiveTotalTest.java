package com.orion.tests;

import com.orion.pages.PivotReportPage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Test;

public class FetchPositiveTotalTest extends BaseTest {
    private static final Logger logger = LogManager.getLogger(FetchPositiveTotalTest.class);

    @Test(description = "Fetch and print the positive total value from Pivot Report UI")
    public void testFetchPositiveTotal() {
        logger.info("Starting testFetchPositiveTotal...");

        // 1. Navigate to the Pivot Summary page
        String pivotUrl = "https://stag-crm.norlee.io/report/pivot-summary";
        logger.info("Navigating to: {}", pivotUrl);
        driver.get(pivotUrl);

        // PivotReportPage pivotPage = new PivotReportPage(driver);
        // 2. Select filters
        // logger.info("Selecting Business Unit: MULTI_FAMILY");
        // pivotPage.clickClearFilter();
        // try { Thread.sleep(2000); } catch (Exception e) {} // Wait for clear filter reload

        // pivotPage.selectBusinessUnit(PivotReportPage.BusinessUnit.MULTI_FAMILY);
        // try { Thread.sleep(1000); } catch (Exception e) {} // Wait for select2 reload

        // logger.info("Selecting Duration: LAST_THIRTY_DAYS");
        // pivotPage.selectDuration(PivotReportPage.DurationType.LAST_THIRTY_DAYS);
        // try { Thread.sleep(1000); } catch (Exception e) {} // Wait for select2 reload

        // logger.info("Toggling 'Treat LOI as Awarded' to true");
        // WebElement toggleInput = driver.findElement(By.id("toggleLOI"));
        // if (!toggleInput.isSelected()) {
        //     driver.findElement(By.xpath("//label[@for='toggleLOI']")).click();
        //     try { Thread.sleep(1000); } catch (Exception e) {}
        // }
        
        // logger.info("Clicking Generate Report...");
        // pivotPage.clickGenerateReport();

        // 3. Wait for the positive total cell to update and display
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
        String firstRowJobAmount = "";
        String outerHTML = "";
        for (int i = 0; i < 3; i++) {
            try {
                WebElement totalCell = driver.findElement(By.id("positive-total-cell"));
                uiTotalText = totalCell.getText().trim();
                outerHTML = totalCell.getAttribute("outerHTML");

                WebElement firstRowCell = driver.findElement(By.xpath("//table[descendant::tr[@id='positive-footer-label']]/tbody/tr[1]/td[4]"));
                firstRowJobAmount = firstRowCell.getText().trim();
                break;
            } catch (org.openqa.selenium.StaleElementReferenceException e) {
                logger.info("Encountered stale element while retrieving text, retrying (Attempt " + (i + 1) + ")");
                try { Thread.sleep(500); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            }
        }

        logger.info("SUCCESS: Positive Table Footer Cell HTML: {}", outerHTML);
        logger.info("SUCCESS: First Row Job Amount from UI: '{}'", firstRowJobAmount);
        logger.info("SUCCESS: Fetched Positive Table Total from UI (Footer): '{}'", uiTotalText);
        System.out.println("First Row Job Amount: " + firstRowJobAmount);
        System.out.println("Fetched Positive Table Total (Footer): " + uiTotalText);
    }
}
