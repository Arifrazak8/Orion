package com.orion.pages;

import com.orion.base.BasePage;
import com.orion.utils.Select2Utils;
import com.orion.utils.TableUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

public class PivotReportPage extends BasePage {

    public enum BusinessUnit {
        MECHANICAL("20 - Mechanical"),
        COMMERCIAL("03 - Commercial"),
        HOSPITALITY("05 - Hospitality"),
        HEALTHCARE("08 - Healthcare"),
        TECHNOLOGY("07 - Technology"),
        SPECIALTY_PROJECTS("11 - Specialty Projects"),
        UNDERGROUND("04 - Underground"),
        ENGINEERING("30 - Engineering"),
        MULTI_FAMILY("02 - Multi Family"),
        MECHANICAL_SERVICE("21 - Mechanical Service"),
        SERVICE("10 - Service"),
        RESIDENTIAL("09 - Residential");

        private final String value;

        BusinessUnit(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum DurationType {
        LAST_SEVEN_DAYS("Last Seven Days"),
        LAST_THIRTY_DAYS("Last Thirty Days"),
        LAST_THREE_MONTHS("Last Three Months"),
        LAST_SIX_MONTHS("Last Six Months"),
        YEAR_TO_DATE("Year To Date"),
        LAST_YEAR("Last Year");

        private final String value;

        DurationType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    // ---------------------------------------------------------
    // Locators
    // ---------------------------------------------------------

    // Profile & Logout
    @FindBy(xpath = "//button[text()='Profile']")
    private WebElement profileBtn;

    @FindBy(id = "logoutBtn")
    private WebElement logoutBtn;

    // Report Filters
    @FindBy(css = "select[wire\\:model='selectedCompanies']")
    private WebElement businessUnitSelect;

    @FindBy(css = "select[wire\\:model='selectedDurationType']")
    private WebElement durationSelect;

    @FindBy(id = "generate-report")
    private WebElement generateReportBtn;

    @FindBy(css = "a[wire\\:click='clearFilter()']")
    private WebElement clearFilterBtn;

    @FindBy(css = "a[wire\\:click='setDefaultFilter()']")
    private WebElement defaultFilterBtn;

    // Toggles
    @FindBy(id = "toggleLOI")
    private WebElement treatLOIasAwardedToggle;

    @FindBy(xpath = "//label[@for='toggleLOI']")
    private WebElement treatLOIasAwardedToggleLabel;

    @FindBy(id = "toggleWeightedForOpcoTableValue")
    private WebElement opcoWeightedValuesToggle;

    @FindBy(xpath = "//label[@for='toggleWeightedForOpcoTableValue']")
    private WebElement opcoWeightedValuesToggleLabel;

    @FindBy(id = "toggleNonWeighted")
    private WebElement nonWeightedChartToggle;

    @FindBy(id = "toggleWeighted")
    private WebElement weightedChartToggle;

    // Search and Data Table
    @FindBy(id = "bidListKeyword")
    private WebElement keywordSearchInput;

    @FindBy(id = "userListFilterBtn")
    private WebElement keywordSearchBtn;

    @FindBy(css = "a[wire\\:click*='exportBid']")
    private WebElement exportToExcelBtn;

    @FindBy(xpath = "//button[text()='Select Columns']")
    private WebElement selectColumnsBtn;

    @FindBy(name = "myTable_length")
    private WebElement showEntriesDropdown;

    @FindBy(id = "myTable")
    private WebElement dataTable;

    @FindBy(xpath = "//table[contains(@class, 'weighted-table')]")
    private WebElement weightedTable;

    @FindBy(xpath = "//table[descendant::tr[@id='positive-footer-label']]")
    private WebElement positiveTable;

    @FindBy(xpath = "//table[descendant::tr[@id='negative-footer-label']]")
    private WebElement negativeTable;

    // Dynamic locators
    private String businessUnitTabXpath = "//a[contains(normalize-space(), '%s')]";

    // ---------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------
    public PivotReportPage(WebDriver driver) {
        super(driver);
    }

    // ---------------------------------------------------------
    // Actions
    // ---------------------------------------------------------

    public void clickProfile() {
        click(profileBtn);
    }

    public void clickLogout() {
        click(logoutBtn);
    }

    public void selectBusinessUnit(BusinessUnit unit) {
        new Select2Utils(driver).selectOption(businessUnitSelect, unit.getValue());
    }

    public void selectDuration(DurationType durationType) {
        new Select2Utils(driver).selectOption(durationSelect, durationType.getValue());
    }

    public TableUtils getWeightedTable() {
        return new TableUtils(driver, weightedTable);
    }

    public TableUtils getPositiveTable() {
        return new TableUtils(driver, positiveTable);
    }

    public TableUtils getNegativeTable() {
        return new TableUtils(driver, negativeTable);
    }

    public void clickGenerateReport() {
        click(generateReportBtn);
    }

    public void clickClearFilter() {
        click(clearFilterBtn);
    }

    public void clickDefaultFilter() {
        click(defaultFilterBtn);
    }

    public void toggleTreatLOIasAwarded(boolean check) {
        if (treatLOIasAwardedToggle.isSelected() != check) {
            click(treatLOIasAwardedToggleLabel);
        }
    }

    public void toggleOpcoWeightedValues(boolean check) {
        if (opcoWeightedValuesToggle.isSelected() != check) {
            click(opcoWeightedValuesToggleLabel);
        }
    }

    public void clickNonWeightedChartToggle() {
        click(nonWeightedChartToggle);
    }

    public void clickWeightedChartToggle() {
        click(weightedChartToggle);
    }

    public void clickBusinessUnitTab(BusinessUnit unit) {
        By dynamicTab = By.xpath(String.format(businessUnitTabXpath, unit.getValue()));
        click(driver.findElement(dynamicTab));
    }

    public void searchKeyword(String keyword) {
        sendKeys(keywordSearchInput, keyword);
        click(keywordSearchBtn);
    }

    public void clickExportToExcel() {
        click(exportToExcelBtn);
    }

    public void openSelectColumns() {
        click(selectColumnsBtn);
    }

    public void selectShowEntries(String entries) {
        // DataTables show entries dropdown might not be hidden, but using the helper handles both safely
        new Select2Utils(driver).selectOption(showEntriesDropdown, entries);
    }

    public boolean isDataTableDisplayed() {
        return dataTable.isDisplayed();
    }
}
