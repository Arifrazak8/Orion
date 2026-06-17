package com.orion.tests;

import com.orion.pages.LoginPage;
import com.orion.pages.DashboardPage;
import java.nio.file.Paths;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Dashboard_test extends BaseTest {

    private static final String VALID_USERNAME = "tomsmith";
    private static final String VALID_PASSWORD = "SuperSecretPassword!";

    private DashboardPage loginAndOpenDashboard() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login(VALID_USERNAME, VALID_PASSWORD);
        driver.get(Paths.get("src", "test", "resources", "HTML_pages", "Dashboard_main.html")
                .toAbsolutePath()
                .toUri()
                .toString());
        return new DashboardPage(driver);
    }

    @Test(description = "Verify dashboard page header, version, KPI cards, and summary tables")
    public void verifyDashboardPage() {
        DashboardPage dashboardPage = loginAndOpenDashboard();

        Assert.assertTrue(dashboardPage.isPageHeaderDisplayed(), "Dashboard page header is not displayed");
        Assert.assertEquals(dashboardPage.getPageHeaderText(), "Dashboard", "Dashboard page header text mismatch");
        Assert.assertTrue(dashboardPage.getVersionBadgeText().contains("Version"), "Version badge text is not displayed");

        Assert.assertEquals(dashboardPage.getKpiCardCount(), 5, "Dashboard KPI card count mismatch");
        Assert.assertFalse(dashboardPage.getRegisteredDivisionCount().isBlank(), "Registered Division Count is blank");
        Assert.assertFalse(dashboardPage.getSummaryStartDate().isBlank(), "Summary Start Date is blank");
        Assert.assertFalse(dashboardPage.getSummaryEndDate().isBlank(), "Summary End Date is blank");
        Assert.assertFalse(dashboardPage.getYtdWonJobCount().isBlank(), "YTD Won Job Count is blank");
        Assert.assertFalse(dashboardPage.getYtdWonAmount().isBlank(), "YTD Won Amount is blank");

        Assert.assertTrue(dashboardPage.isWeightedPipelineSummaryDisplayed(), "Weighted Pipeline Summary table is not displayed");
        Assert.assertTrue(dashboardPage.getWeightedPipelineSummaryRowCount() > 0, "Weighted Pipeline Summary rows are not displayed");
        Assert.assertTrue(dashboardPage.getWeightedPipelineSummaryGrandTotalText().contains("Grand Total"),
                "Weighted Pipeline Summary grand total is not displayed");

        Assert.assertTrue(dashboardPage.isNonWeightedPipelineSummaryDisplayed(), "Non-Weighted Pipeline Summary table is not displayed");
        Assert.assertTrue(dashboardPage.getNonWeightedPipelineSummaryRowCount() > 0,
                "Non-Weighted Pipeline Summary rows are not displayed");
        Assert.assertTrue(dashboardPage.getNonWeightedPipelineSummaryGrandTotalText().contains("Grand Total"),
                "Non-Weighted Pipeline Summary grand total is not displayed");
    }

    @Test(description = "Verify dashboard widgets, charts, and data tables")
    public void verifyDashboardWidgets() {
        DashboardPage dashboardPage = loginAndOpenDashboard();

        Assert.assertTrue(dashboardPage.isTotalSoftBacklogByStatusDisplayed(),
                "Total Soft Backlog By Status widget is not displayed");
        Assert.assertTrue(dashboardPage.isTotalSoftBacklogByStatusChartDisplayed(),
                "Total Soft Backlog By Status chart is not displayed");

        Assert.assertTrue(dashboardPage.isWonTableDisplayed(), "Won table is not displayed");
        Assert.assertTrue(dashboardPage.getWonTableRowCount() > 0, "Won table rows are not displayed");
        Assert.assertTrue(dashboardPage.getWonTableTotalText().contains("Total"), "Won table total row is not displayed");

        Assert.assertTrue(dashboardPage.isLostTableDisplayed(), "Lost table is not displayed");
        Assert.assertTrue(dashboardPage.getLostTableRowCount() > 0, "Lost table rows are not displayed");
        Assert.assertTrue(dashboardPage.getLostTableTotalText().contains("Total"), "Lost table total row is not displayed");

        Assert.assertTrue(dashboardPage.isTotalSoftBacklogByDivisionDisplayed(),
                "Total Soft Backlog By Division widget is not displayed");
        Assert.assertTrue(dashboardPage.isTotalSoftBacklogByDivisionChartDisplayed(),
                "Total Soft Backlog By Division chart is not displayed");

        Assert.assertTrue(dashboardPage.isStatusBidTableDisplayed(), "Status bid table is not displayed");
        Assert.assertTrue(dashboardPage.getStatusBidTableRowCount() > 0, "Status bid table rows are not displayed");
        Assert.assertTrue(dashboardPage.getStatusBidTableTotalText().contains("Total"),
                "Status bid table total row is not displayed");

        Assert.assertTrue(dashboardPage.isNonWeightedPipelineTrendDisplayed(),
                "Non-Weighted Pipeline Trend widget is not displayed");
        Assert.assertTrue(dashboardPage.isNonWeightedPipelineTrendChartDisplayed(),
                "Non-Weighted Pipeline Trend chart is not displayed");

        Assert.assertTrue(dashboardPage.isWeightedPipelineTrendDisplayed(),
                "Weighted Pipeline Trend widget is not displayed");
        Assert.assertTrue(dashboardPage.isWeightedPipelineTrendChartDisplayed(),
                "Weighted Pipeline Trend chart is not displayed");

        Assert.assertTrue(dashboardPage.isJobAmountByDivisionAndStatusDisplayed(),
                "Job Amount By Division and Status widget is not displayed");
        Assert.assertTrue(dashboardPage.isJobAmountByDivisionAndStatusChartDisplayed(),
                "Job Amount By Division and Status chart is not displayed");
    }

    @Test(description = "Verify dashboard navigation tabs, profile menu, filters, and toggles")
    public void verifyNavigationMenus() {
        DashboardPage dashboardPage = loginAndOpenDashboard();

        Assert.assertTrue(dashboardPage.isDashboardTabWrapperDisplayed(), "Dashboard tab wrapper is not displayed");
        Assert.assertEquals(dashboardPage.getDashboardTabCount(), 3, "Dashboard tab count mismatch");

        dashboardPage.openUserProfileMenu();
        Assert.assertTrue(dashboardPage.isUserDropdownDisplayed(), "User dropdown is not displayed");

        dashboardPage.searchWonLostTable("TRG");
        dashboardPage.clickWonLostClearFilterButton();
        dashboardPage.selectWonLostDuration("Last Thirty Days");

        dashboardPage.searchStatusBidTable("Mayo");
        dashboardPage.clickStatusBidClearFilterButton();
        dashboardPage.selectStatusBidDuration("Last Thirty Days");

        dashboardPage.clickWeightedPipelineSummaryExclBBsToggle();
        dashboardPage.clickNonWeightedPipelineSummaryExclBBsToggle();
        dashboardPage.clickTotalSoftBacklogByStatusWeightedValuesToggle();
        dashboardPage.clickTotalSoftBacklogByStatusExclBBsToggle();
        dashboardPage.clickTotalSoftBacklogByDivisionWeightedValuesToggle();
        dashboardPage.clickTotalSoftBacklogByDivisionExclBBsToggle();
        dashboardPage.clickNonWeightedPipelineTrendAllDivisionsToggle();
        dashboardPage.clickNonWeightedPipelineTrendExclBBsToggle();
        dashboardPage.clickWeightedPipelineTrendAllDivisionsToggle();
        dashboardPage.clickWeightedPipelineTrendExclBBsToggle();
        dashboardPage.clickJobAmountByDivisionAndStatusWeightedValuesToggle();
    }
}
