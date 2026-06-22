package com.orion.pages;

import com.orion.base.BasePage;
import com.orion.utils.TableUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class SalesPipelinePage extends BasePage {

    // Locator for the summary table
    @FindBy(css = "table.summary-table")
    private WebElement weightedPipelineSummaryTable;

    // Locator for the "Excl. B/Bs" checkbox/toggle
    @FindBy(id = "toggleWeightedSummary")
    private WebElement toggleWeightedSummaryCheckbox;

    // Locator for the "Excl. B/Bs" checkbox label to click
    @FindBy(xpath = "//label[@for='toggleWeightedSummary']")
    private WebElement toggleWeightedSummaryLabel;

    public SalesPipelinePage(WebDriver driver) {
        super(driver);
    }

    /**
     * Gets the TableUtils helper instance for the Weighted Pipeline Summary table.
     */
    public TableUtils getWeightedPipelineSummaryTable() {
        return new TableUtils(driver, weightedPipelineSummaryTable);
    }

    /**
     * Gets the cell value dynamically by row (division) name and column name.
     * 
     * @param division Name of the division (e.g., "05 - Hospitality")
     * @param column   Name of the column (e.g., "90% Confirm")
     * @return Cell text value
     */
    public String getWeightedPipelineSummaryValue(String division, String column) {
        return getWeightedPipelineSummaryTable().getCellValue(division, column);
    }

    /**
     * Toggles the "Excl. B/Bs" option.
     * 
     * @param check true to check, false to uncheck
     */
    public void toggleWeightedSummary(boolean check) {
        if (toggleWeightedSummaryCheckbox.isSelected() != check) {
            click(toggleWeightedSummaryLabel);
        }
    }
}
