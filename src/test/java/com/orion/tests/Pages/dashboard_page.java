package com.orion.tests.Pages;

import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.Select;

public class dashboard_page {

    WebDriver driver;

    public dashboard_page(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    // Header Elements
    @FindBy(id = "page-header")
    WebElement pageHeader;

    @FindBy(css = ".badge.bg-light.text-dark")
    WebElement versionBadge;

    @FindBy(css = ".user")
    WebElement userProfileMenu;

    @FindBy(css = ".user .image img")
    WebElement userProfileImage;

    @FindBy(id = "userDropdown")
    WebElement userDropdown;

    @FindBy(xpath = "//div[@id='userDropdown']//button[normalize-space()='Profile']")
    WebElement profileButton;

    @FindBy(id = "logoutBtn")
    WebElement logoutButton;

    // Dashboard Tabs
    @FindBy(css = ".dashboard-tab-wrapper-box")
    WebElement dashboardTabWrapper;

    @FindBy(css = ".dashboard-tab-wrapper-box button")
    List<WebElement> dashboardTabButtons;

    @FindBy(xpath = "//div[contains(@class,'dashboard-tab-wrapper-box')]//button[normalize-space()='Sales Pipeline Dashboard']")
    WebElement salesPipelineDashboardTab;

    @FindBy(xpath = "//div[contains(@class,'dashboard-tab-wrapper-box')]//button[normalize-space()='Projects Report Dashboard']")
    WebElement projectsReportDashboardTab;

    @FindBy(xpath = "//div[contains(@class,'dashboard-tab-wrapper-box')]//button[normalize-space()='Ar Pipeline Dashboard']")
    WebElement arPipelineDashboardTab;

    // KPI Cards
    @FindBy(css = ".dashboard-progress-each")
    List<WebElement> kpiCards;

    @FindBy(xpath = "//h2[normalize-space()='Registered Division Count']/following-sibling::div//h1")
    WebElement registeredDivisionCountValue;

    @FindBy(xpath = "//h2[normalize-space()='Summary Start Date']/following-sibling::div//h1")
    WebElement summaryStartDateValue;

    @FindBy(xpath = "//h2[normalize-space()='Summary End Date']/following-sibling::div//h1")
    WebElement summaryEndDateValue;

    @FindBy(xpath = "//h2[normalize-space()='YTD Won Job Count']/following-sibling::div//h1")
    WebElement ytdWonJobCountValue;

    @FindBy(xpath = "//h2[normalize-space()='YTD Won Amount ($)']/following-sibling::div//h1")
    WebElement ytdWonAmountValue;

    // Pipeline Summary Tables
    @FindBy(xpath = "//h2[normalize-space()='Weighted Pipeline Summary']")
    WebElement weightedPipelineSummaryHeader;

    @FindBy(id = "toggleWeightedSummary")
    WebElement weightedPipelineSummaryExclBBsToggle;

    @FindBy(xpath = "//h2[normalize-space()='Weighted Pipeline Summary']/ancestor::div[contains(@class,'card-wrp')]//table")
    WebElement weightedPipelineSummaryTable;

    @FindBy(xpath = "//h2[normalize-space()='Weighted Pipeline Summary']/ancestor::div[contains(@class,'card-wrp')]//tbody/tr")
    List<WebElement> weightedPipelineSummaryRows;

    @FindBy(xpath = "//h2[normalize-space()='Weighted Pipeline Summary']/ancestor::div[contains(@class,'card-wrp')]//tfoot//tr")
    WebElement weightedPipelineSummaryGrandTotalRow;

    @FindBy(xpath = "//h2[normalize-space()='Non-Weighted Pipeline Summary']")
    WebElement nonWeightedPipelineSummaryHeader;

    @FindBy(id = "toggleNonWeightedSummary")
    WebElement nonWeightedPipelineSummaryExclBBsToggle;

    @FindBy(xpath = "//h2[normalize-space()='Non-Weighted Pipeline Summary']/ancestor::div[contains(@class,'card-wrp')]//table")
    WebElement nonWeightedPipelineSummaryTable;

    @FindBy(xpath = "//h2[normalize-space()='Non-Weighted Pipeline Summary']/ancestor::div[contains(@class,'card-wrp')]//tbody/tr")
    List<WebElement> nonWeightedPipelineSummaryRows;

    @FindBy(xpath = "//h2[normalize-space()='Non-Weighted Pipeline Summary']/ancestor::div[contains(@class,'card-wrp')]//tfoot//tr")
    WebElement nonWeightedPipelineSummaryGrandTotalRow;

    // Total Soft Backlog By Status
    @FindBy(xpath = "//h2[normalize-space()='Total Soft Backlog By Status']")
    WebElement totalSoftBacklogByStatusHeader;

    @FindBy(id = "toggleNonWeightedOpCoJobAmountStaus")
    WebElement totalSoftBacklogByStatusWeightedValuesToggle;

    @FindBy(id = "toggleNonWeightedOpCoJobAmountExcl3BsStstus")
    WebElement totalSoftBacklogByStatusExclBBsToggle;

    @FindBy(id = "chartStatus")
    WebElement totalSoftBacklogByStatusChart;

    // Won and Lost Card
    @FindBy(css = "[data-component='won-lost']")
    WebElement wonLostCard;

    @FindBy(css = "[data-component='won-lost'] select#statusFilter")
    WebElement wonLostDurationDropdown;

    @FindBy(css = "[data-component='won-lost'] input.filter-src")
    WebElement wonLostSearchInput;

    @FindBy(css = "[data-component='won-lost'] button.view-btn")
    WebElement wonLostClearFilterButton;

    @FindBy(xpath = "//div[@data-component='won-lost']//h6[normalize-space()='Won']")
    WebElement wonSectionHeader;

    @FindBy(css = "table[data-table-key='won']")
    WebElement wonTable;

    @FindBy(css = "table[data-table-key='won'] tbody tr")
    List<WebElement> wonTableRows;

    @FindBy(css = "table[data-table-key='won'] tfoot tr")
    WebElement wonTableTotalRow;

    @FindBy(xpath = "//div[@data-component='won-lost']//h6[normalize-space()='Lost']")
    WebElement lostSectionHeader;

    @FindBy(css = "table[data-table-key='lost']")
    WebElement lostTable;

    @FindBy(css = "table[data-table-key='lost'] tbody tr")
    List<WebElement> lostTableRows;

    @FindBy(css = "table[data-table-key='lost'] tfoot tr")
    WebElement lostTableTotalRow;

    // Total Soft Backlog By Division
    @FindBy(xpath = "//h2[normalize-space()='Total Soft Backlog By Division']")
    WebElement totalSoftBacklogByDivisionHeader;

    @FindBy(id = "toggleNonWeightedOpCoJobAmount")
    WebElement totalSoftBacklogByDivisionWeightedValuesToggle;

    @FindBy(id = "toggleNonWeightedOpCoJobAmountExcl3Bs")
    WebElement totalSoftBacklogByDivisionExclBBsToggle;

    @FindBy(id = "totalCompanyWiseJobAmount")
    WebElement totalSoftBacklogByDivisionChart;

    // LOI, 90% Confirm, 50% Confirm Card
    @FindBy(xpath = "//h6[normalize-space()='LOI, 90% Confirm, 50% Confirm']")
    WebElement statusBidCardHeader;

    @FindBy(xpath = "//table[@data-table-key='status_bid']/ancestor::div[contains(@class,'sales-dashboard-award-lost')]//select[@id='statusFilter']")
    WebElement statusBidDurationDropdown;

    @FindBy(xpath = "//table[@data-table-key='status_bid']/ancestor::div[contains(@class,'sales-dashboard-award-lost')]//input[contains(@class,'filter-src')]")
    WebElement statusBidSearchInput;

    @FindBy(xpath = "//table[@data-table-key='status_bid']/ancestor::div[contains(@class,'sales-dashboard-award-lost')]//button[contains(@class,'view-btn')]")
    WebElement statusBidClearFilterButton;

    @FindBy(css = "table[data-table-key='status_bid']")
    WebElement statusBidTable;

    @FindBy(css = "table[data-table-key='status_bid'] tbody tr")
    List<WebElement> statusBidTableRows;

    @FindBy(css = "table[data-table-key='status_bid'] tfoot tr")
    WebElement statusBidTableTotalRow;

    // Pipeline Trend Charts
    @FindBy(xpath = "//h2[normalize-space()='Non-Weighted Pipeline Trend']")
    WebElement nonWeightedPipelineTrendHeader;

    @FindBy(id = "toggleNonWeightedAll")
    WebElement nonWeightedPipelineTrendAllDivisionsToggle;

    @FindBy(id = "toggleNonWeighted")
    WebElement nonWeightedPipelineTrendExclBBsToggle;

    @FindBy(id = "nonWeightedChart")
    WebElement nonWeightedPipelineTrendChart;

    @FindBy(id = "nonWeightedChartAll")
    WebElement nonWeightedPipelineTrendAllDivisionsChart;

    @FindBy(xpath = "//h2[normalize-space()='Weighted Pipeline Trend']")
    WebElement weightedPipelineTrendHeader;

    @FindBy(id = "toggleWeightedAll")
    WebElement weightedPipelineTrendAllDivisionsToggle;

    @FindBy(id = "toggleWeighted")
    WebElement weightedPipelineTrendExclBBsToggle;

    @FindBy(id = "weightedChart")
    WebElement weightedPipelineTrendChart;

    @FindBy(id = "weightedChartAll")
    WebElement weightedPipelineTrendAllDivisionsChart;

    // Job Amount By Division and Status
    @FindBy(xpath = "//h2[normalize-space()='Job Amount($) By Division & Status']")
    WebElement jobAmountByDivisionAndStatusHeader;

    @FindBy(id = "toggleNonWeightedOpCoStatusJobAmount")
    WebElement jobAmountByDivisionAndStatusWeightedValuesToggle;

    @FindBy(id = "statusByOpCop")
    WebElement jobAmountByDivisionAndStatusChart;

    public boolean isPageHeaderDisplayed() {
        return pageHeader.isDisplayed();
    }

    public String getPageHeaderText() {
        return pageHeader.getText();
    }

    public String getVersionBadgeText() {
        return versionBadge.getText();
    }

    public void openUserProfileMenu() {
        userProfileMenu.click();
    }

    public boolean isUserDropdownDisplayed() {
        return userDropdown.isDisplayed();
    }

    public void clickProfileButton() {
        profileButton.click();
    }

    public void clickLogoutButton() {
        logoutButton.click();
    }

    public boolean isDashboardTabWrapperDisplayed() {
        return dashboardTabWrapper.isDisplayed();
    }

    public void clickSalesPipelineDashboardTab() {
        salesPipelineDashboardTab.click();
    }

    public void clickProjectsReportDashboardTab() {
        projectsReportDashboardTab.click();
    }

    public void clickArPipelineDashboardTab() {
        arPipelineDashboardTab.click();
    }

    public int getDashboardTabCount() {
        return dashboardTabButtons.size();
    }

    public String getRegisteredDivisionCount() {
        return registeredDivisionCountValue.getText();
    }

    public String getSummaryStartDate() {
        return summaryStartDateValue.getText();
    }

    public String getSummaryEndDate() {
        return summaryEndDateValue.getText();
    }

    public String getYtdWonJobCount() {
        return ytdWonJobCountValue.getText();
    }

    public String getYtdWonAmount() {
        return ytdWonAmountValue.getText();
    }

    public int getKpiCardCount() {
        return kpiCards.size();
    }

    public boolean isWeightedPipelineSummaryDisplayed() {
        return weightedPipelineSummaryTable.isDisplayed();
    }

    public void clickWeightedPipelineSummaryExclBBsToggle() {
        weightedPipelineSummaryExclBBsToggle.click();
    }

    public String getWeightedPipelineSummaryGrandTotalText() {
        return weightedPipelineSummaryGrandTotalRow.getText();
    }

    public int getWeightedPipelineSummaryRowCount() {
        return weightedPipelineSummaryRows.size();
    }

    public boolean isNonWeightedPipelineSummaryDisplayed() {
        return nonWeightedPipelineSummaryTable.isDisplayed();
    }

    public void clickNonWeightedPipelineSummaryExclBBsToggle() {
        nonWeightedPipelineSummaryExclBBsToggle.click();
    }

    public String getNonWeightedPipelineSummaryGrandTotalText() {
        return nonWeightedPipelineSummaryGrandTotalRow.getText();
    }

    public int getNonWeightedPipelineSummaryRowCount() {
        return nonWeightedPipelineSummaryRows.size();
    }

    public boolean isTotalSoftBacklogByStatusDisplayed() {
        return totalSoftBacklogByStatusHeader.isDisplayed();
    }

    public void clickTotalSoftBacklogByStatusWeightedValuesToggle() {
        totalSoftBacklogByStatusWeightedValuesToggle.click();
    }

    public void clickTotalSoftBacklogByStatusExclBBsToggle() {
        totalSoftBacklogByStatusExclBBsToggle.click();
    }

    public boolean isTotalSoftBacklogByStatusChartDisplayed() {
        return totalSoftBacklogByStatusChart.isDisplayed();
    }

    public void selectWonLostDuration(String visibleText) {
        new Select(wonLostDurationDropdown).selectByVisibleText(visibleText);
    }

    public void searchWonLostTable(String searchText) {
        wonLostSearchInput.clear();
        wonLostSearchInput.sendKeys(searchText);
    }

    public void clickWonLostClearFilterButton() {
        wonLostClearFilterButton.click();
    }

    public boolean isWonTableDisplayed() {
        return wonTable.isDisplayed();
    }

    public int getWonTableRowCount() {
        return wonTableRows.size();
    }

    public String getWonTableTotalText() {
        return wonTableTotalRow.getText();
    }

    public boolean isLostTableDisplayed() {
        return lostTable.isDisplayed();
    }

    public int getLostTableRowCount() {
        return lostTableRows.size();
    }

    public String getLostTableTotalText() {
        return lostTableTotalRow.getText();
    }

    public boolean isTotalSoftBacklogByDivisionDisplayed() {
        return totalSoftBacklogByDivisionHeader.isDisplayed();
    }

    public void clickTotalSoftBacklogByDivisionWeightedValuesToggle() {
        totalSoftBacklogByDivisionWeightedValuesToggle.click();
    }

    public void clickTotalSoftBacklogByDivisionExclBBsToggle() {
        totalSoftBacklogByDivisionExclBBsToggle.click();
    }

    public boolean isTotalSoftBacklogByDivisionChartDisplayed() {
        return totalSoftBacklogByDivisionChart.isDisplayed();
    }

    public void selectStatusBidDuration(String visibleText) {
        new Select(statusBidDurationDropdown).selectByVisibleText(visibleText);
    }

    public void searchStatusBidTable(String searchText) {
        statusBidSearchInput.clear();
        statusBidSearchInput.sendKeys(searchText);
    }

    public void clickStatusBidClearFilterButton() {
        statusBidClearFilterButton.click();
    }

    public boolean isStatusBidTableDisplayed() {
        return statusBidTable.isDisplayed();
    }

    public int getStatusBidTableRowCount() {
        return statusBidTableRows.size();
    }

    public String getStatusBidTableTotalText() {
        return statusBidTableTotalRow.getText();
    }

    public boolean isNonWeightedPipelineTrendDisplayed() {
        return nonWeightedPipelineTrendHeader.isDisplayed();
    }

    public void clickNonWeightedPipelineTrendAllDivisionsToggle() {
        nonWeightedPipelineTrendAllDivisionsToggle.click();
    }

    public void clickNonWeightedPipelineTrendExclBBsToggle() {
        nonWeightedPipelineTrendExclBBsToggle.click();
    }

    public boolean isNonWeightedPipelineTrendChartDisplayed() {
        return nonWeightedPipelineTrendChart.isDisplayed();
    }

    public boolean isNonWeightedPipelineTrendAllDivisionsChartDisplayed() {
        return nonWeightedPipelineTrendAllDivisionsChart.isDisplayed();
    }

    public boolean isWeightedPipelineTrendDisplayed() {
        return weightedPipelineTrendHeader.isDisplayed();
    }

    public void clickWeightedPipelineTrendAllDivisionsToggle() {
        weightedPipelineTrendAllDivisionsToggle.click();
    }

    public void clickWeightedPipelineTrendExclBBsToggle() {
        weightedPipelineTrendExclBBsToggle.click();
    }

    public boolean isWeightedPipelineTrendChartDisplayed() {
        return weightedPipelineTrendChart.isDisplayed();
    }

    public boolean isWeightedPipelineTrendAllDivisionsChartDisplayed() {
        return weightedPipelineTrendAllDivisionsChart.isDisplayed();
    }

    public boolean isJobAmountByDivisionAndStatusDisplayed() {
        return jobAmountByDivisionAndStatusHeader.isDisplayed();
    }

    public void clickJobAmountByDivisionAndStatusWeightedValuesToggle() {
        jobAmountByDivisionAndStatusWeightedValuesToggle.click();
    }

    public boolean isJobAmountByDivisionAndStatusChartDisplayed() {
        return jobAmountByDivisionAndStatusChart.isDisplayed();
    }
}
