package com.orion.tests;

import com.orion.pages.PivotReportPage;
import com.orion.pages.PivotReportPage.BusinessUnit;
import com.orion.pages.PivotReportPage.DurationType;
import com.orion.utils.TableUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

public class PivotReportTest extends BaseTest { // Assuming you have a BaseTest handling driver setup

    @Test
    public void testDynamicTableValidation() {
        // 1. Initialize Page
        PivotReportPage pivotPage = new PivotReportPage(driver);
        
        // 2. Interact with generic Select2 components
        pivotPage.selectBusinessUnit(BusinessUnit.HEALTHCARE);
        pivotPage.selectDuration(DurationType.LAST_SIX_MONTHS);
        pivotPage.clickGenerateReport();
        
        // 3. Get the dynamic TableUtils instance
        TableUtils weightedTable = pivotPage.getWeightedTable();
        
        // 4. Validate specific dynamic cells by row name and column name
        String healthcareTotal = weightedTable.getCellValue("08 - Healthcare", "Total");
        Assert.assertEquals(healthcareTotal, "89,580,407", "Healthcare Total mismatch!");
        
        String commercialBbs = weightedTable.getCellValue("03 - Commercial", "B/BS");
        Assert.assertEquals(commercialBbs, "9,791,221", "Commercial B/BS mismatch!");
        
        // 5. Validate Grand Total dynamically
        String grandTotalBbs = weightedTable.getGrandTotalValue("B/BS");
        Assert.assertEquals(grandTotalBbs, "56,551,899", "Grand Total B/BS mismatch!");
        
        // 6. Verify boolean shortcut
        boolean isValid = weightedTable.verifyCellValue("02 - Multi Family", "Confirm 90%", "6,399,000");
        Assert.assertTrue(isValid, "Multi Family Confirm 90% is incorrect.");
    }
}
