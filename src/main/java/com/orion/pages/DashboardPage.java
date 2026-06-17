package com.orion.pages;

import com.orion.base.BasePage;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public class DashboardPage extends BasePage {

     WebDriver driver;
     private final By crmPageHeader = By.id("page-header");
     private final By internetSecureAreaHeader = By.cssSelector("h2");
     private final By internetSecureAreaLogoutLink = By.cssSelector("a[href='/logout']");
     private final By internetSecureAreaFlashMessage = By.id("flash");
     private final By weightedPipelineSummaryHeaderBy = By.xpath("//h2[contains(.,'Weighted Pipeline Summary')]");
     private final By weightedPipelineSummaryTableFooterRowsBy = By.xpath("//h2[contains(.,'Weighted Pipeline Summary')]/ancestor::*[contains(@class,'card-wrp')]//tfoot//tr");
     private final By weightedPipelineSummaryTableBodyRowsBy = By.xpath("//h2[contains(.,'Weighted Pipeline Summary')]/ancestor::*[contains(@class,'card-wrp')]//tbody/tr");
     private final By nonWeightedPipelineSummaryHeaderBy = By.xpath("//h2[contains(.,'Non-Weighted Pipeline Summary')]");
     private final By nonWeightedPipelineSummaryTableFooterRowsBy = By.xpath("//h2[contains(.,'Non-Weighted Pipeline Summary')]/ancestor::*[contains(@class,'card-wrp')]//tfoot//tr");
     private final By nonWeightedPipelineSummaryTableBodyRowsBy = By.xpath("//h2[contains(.,'Non-Weighted Pipeline Summary')]/ancestor::*[contains(@class,'card-wrp')]//tbody/tr");

    public DashboardPage(WebDriver driver) {
        super(driver);
        this.driver = driver;
        PageFactory.initElements(driver, this);
        waitForDashboardToLoad();
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

    @FindBy(id = "flash")
    WebElement flashMessage;

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
    @FindBy(id = "toggleWeightedSummary")
    WebElement weightedPipelineSummaryExclBBsToggle;

    @FindBy(xpath = "//h2[normalize-space()='Weighted Pipeline Summary']/ancestor::div[contains(@class,'card-wrp')]//table")
    WebElement weightedPipelineSummaryTable;

    @FindBy(id = "toggleNonWeightedSummary")
    WebElement nonWeightedPipelineSummaryExclBBsToggle;

    @FindBy(xpath = "//h2[normalize-space()='Non-Weighted Pipeline Summary']/ancestor::div[contains(@class,'card-wrp')]//table")
    WebElement nonWeightedPipelineSummaryTable;

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
        return isDisplayed(crmPageHeader) || isDisplayed(internetSecureAreaHeader);
    }

    public String getPageHeaderText() {
        if (isDisplayed(crmPageHeader)) {
            return pageHeader.getText();
        }
        return getText(internetSecureAreaHeader);
    }

    public String getHeaderText() {
        return getPageHeaderText();
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
        if (isElementDisplayed(logoutButton)) {
            logoutButton.click();
            return;
        }
        click(internetSecureAreaLogoutLink);
    }

    public boolean isLogoutButtonDisplayed() {
        return isElementDisplayed(logoutButton) || !driver.findElements(internetSecureAreaLogoutLink).isEmpty();
    }

    public LoginPage clickLogout() {
        clickLogoutButton();
        return new LoginPage(driver);
    }

    public String getFlashMessageText() {
        if (isElementDisplayed(flashMessage)) {
            return flashMessage.getText();
        }
        if (!driver.findElements(internetSecureAreaFlashMessage).isEmpty()) {
            return getText(internetSecureAreaFlashMessage);
        }
        return "";
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
        debugWeightedPipelineSummaryState();
        List<WebElement> footerRows = findElementsSafely(weightedPipelineSummaryTableFooterRowsBy);
        if (!footerRows.isEmpty()) {
            return footerRows.get(0).getText();
        }

        List<WebElement> bodyRows = getVisibleElements(weightedPipelineSummaryTableBodyRowsBy);
        if (!bodyRows.isEmpty()) {
            return bodyRows.get(bodyRows.size() - 1).getText();
        }

        return "";
    }

    public int getWeightedPipelineSummaryRowCount() {
        return getVisibleElements(weightedPipelineSummaryTableBodyRowsBy).size();
    }

    public boolean isNonWeightedPipelineSummaryDisplayed() {
        return nonWeightedPipelineSummaryTable.isDisplayed();
    }

    public void clickNonWeightedPipelineSummaryExclBBsToggle() {
        nonWeightedPipelineSummaryExclBBsToggle.click();
    }

    public String getNonWeightedPipelineSummaryGrandTotalText() {
        debugNonWeightedPipelineSummaryState();
        List<WebElement> footerRows = findElementsSafely(nonWeightedPipelineSummaryTableFooterRowsBy);
        if (!footerRows.isEmpty()) {
            return footerRows.get(0).getText();
        }

        List<WebElement> bodyRows = getVisibleElements(nonWeightedPipelineSummaryTableBodyRowsBy);
        if (!bodyRows.isEmpty()) {
            return bodyRows.get(bodyRows.size() - 1).getText();
        }

        return "";
    }

    public int getNonWeightedPipelineSummaryRowCount() {
        return getVisibleElements(nonWeightedPipelineSummaryTableBodyRowsBy).size();
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

    private void waitForDashboardToLoad() {
        new WebDriverWait(driver, Duration.ofSeconds(15))
                .until(ExpectedConditions.or(
                        ExpectedConditions.visibilityOfElementLocated(crmPageHeader),
                        ExpectedConditions.visibilityOfElementLocated(internetSecureAreaHeader)));
    }

    private boolean isElementDisplayed(WebElement element) {
        try {
            return element != null && element.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    private List<WebElement> getVisibleElements(By locator) {
        try {
            wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(locator));
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
        return findElementsSafely(locator);
    }

    private List<WebElement> findElementsSafely(By locator) {
        List<WebElement> elements = driver.findElements(locator);
        return elements == null ? Collections.emptyList() : elements;
    }

    private void debugWeightedPipelineSummaryState() {
        debugSummaryState("Weighted Pipeline Summary", weightedPipelineSummaryHeaderBy, weightedPipelineSummaryTableFooterRowsBy, weightedPipelineSummaryTableBodyRowsBy);
    }

    private void debugNonWeightedPipelineSummaryState() {
        debugSummaryState("Non-Weighted Pipeline Summary", nonWeightedPipelineSummaryHeaderBy, nonWeightedPipelineSummaryTableFooterRowsBy, nonWeightedPipelineSummaryTableBodyRowsBy);
    }

    private void debugSummaryState(String label, By headerBy, By footerBy, By bodyBy) {
        boolean headingExists = !findElementsSafely(headerBy).isEmpty();
        List<WebElement> footerRows = findElementsSafely(footerBy);
        List<WebElement> bodyRows = findElementsSafely(bodyBy);

        System.out.println(label + " heading exists: " + headingExists);
        System.out.println(label + " table exists: " + (!bodyRows.isEmpty() || !footerRows.isEmpty()));
        System.out.println(label + " footer rows found: " + footerRows.size());
        System.out.println(label + " body rows found: " + bodyRows.size());
    }
}
