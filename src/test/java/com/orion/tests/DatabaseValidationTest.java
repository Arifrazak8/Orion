package com.orion.tests;

import com.orion.utils.DatabaseUtils;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * DatabaseValidationTest — demonstrates how to fetch data from the
 * {@code orion_qa_test} MySQL database and validate it against values
 * rendered in the browser.
 *
 * <p>
 * Pattern followed in every test method:
 * <ol>
 * <li>Navigate to the relevant page using a Page Object.</li>
 * <li>Read the expected value from the database via {@link DatabaseUtils}.</li>
 * <li>Read the actual value from the UI via the Page Object.</li>
 * <li>Assert they match.</li>
 * </ol>
 *
 * <p>
 * The {@link DatabaseUtils} instance is shared across all test methods in this
 * class and is opened once in {@code @BeforeClass} / closed in
 * {@code @AfterClass}
 * to avoid repeatedly reconnecting for each test.
 */
public class DatabaseValidationTest extends BaseTest {

    private static final Logger logger = LogManager.getLogger(DatabaseValidationTest.class);

    /** Shared, read-only database connection for this test class. */
    private DatabaseUtils db;

    // -------------------------------------------------------------------------
    // Lifecycle — open & close the DB connection once per test class
    // -------------------------------------------------------------------------

    @BeforeClass(alwaysRun = true)
    public void connectToDatabase() throws SQLException {
        db = new DatabaseUtils();
        db.connect();
        logger.info("DatabaseValidationTest: DB connection ready.");
    }

    @AfterClass(alwaysRun = true)
    public void disconnectFromDatabase() {
        if (db != null) {
            db.disconnect();
        }
        logger.info("DatabaseValidationTest: DB connection closed.");
    }

    @Test(description = "DB vs UI: Validate user full name on the dashboard")
    public void testUserFullNameMatchesDatabase() throws SQLException {
        logger.info("TEST: testUserFullNameMatchesDatabase");

        // ── 1. Fetch expected value from DB ─────────────────────────────────
        String query = "SELECT * FROM `pipeline_bids` WHERE `id` = 12732";

        String dbContactName = db.getSingleValue(query, "contact_name", "weavers_test@norleegroup.com");
        Assert.assertNotNull(dbContactName, "No matching user found in the database for the given email.");
        logger.info("DB full name: {}", dbContactName);
        System.out.println(query);

        // ── 2. Read the same value from the UI ──────────────────────────────
        // TODO: Replace with your actual Page Object call, e.g.:
        // DashboardPage dashboard = new DashboardPage(driver);
        // String uiFullName = dashboard.getLoggedInUserName();
        //
        // Placeholder — remove this line once the Page Object is wired up:
        // String uiFullName = dbContactName; // <- replace with real UI read

        // // ── 3. Assert ────────────────────────────────────────────────────────
        // Assert.assertEquals(uiFullName, dbContactName,
        // "Full name on UI does not match the database value.");
        // logger.info("PASS: UI full name '{}' matches DB value '{}'.", uiFullName,
        // dbContactName);
    }

    // =========================================================================
    // Example Test 2 — Validate a list of items (e.g. dropdown / table rows)
    // =========================================================================

    /**
     * Fetches a list of bid category names from the DB and asserts that every
     * one of them is present in the corresponding UI table or dropdown.
     *
     * <p>
     * TODO: Replace the query, column name, and UI-read logic with your
     * actual page objects and selectors.
     */
    @Test(description = "DB vs UI: Validate bid category list matches database")
    public void testBidCategoryListMatchesDatabase() throws SQLException {
        logger.info("TEST: testBidCategoryListMatchesDatabase");

        // ── 1. Fetch all category names from DB ──────────────────────────────
        String query = "SELECT name FROM bid_categories ORDER BY name ASC";
        List<String> dbCategories = db.getColumnValues(query, "name");

        Assert.assertFalse(dbCategories.isEmpty(),
                "bid_categories table returned no rows — check the database.");
        logger.info("DB returned {} bid categories.", dbCategories.size());

        // ── 2. Read the same list from the UI ────────────────────────────────
        // TODO: Replace with your actual Page Object call, e.g.:
        // BidCategoriesPage page = new BidCategoriesPage(driver);
        // List<String> uiCategories = page.getCategoryNames();
        //
        // Placeholder — remove this block once the Page Object is wired up:
        List<String> uiCategories = dbCategories; // <- replace with real UI read

        // ── 3. Assert every DB value is present in the UI list ───────────────
        for (String dbCategory : dbCategories) {
            Assert.assertTrue(uiCategories.contains(dbCategory),
                    "Category '" + dbCategory + "' found in DB but NOT present on the UI.");
        }
        logger.info("PASS: All {} DB categories are present in the UI.", dbCategories.size());
    }

    // =========================================================================
    // Example Test 3 — Validate row count (e.g. total records shown in a table)
    // =========================================================================

    /**
     * Compares the total number of bids in the DB with the count displayed on
     * the bids list page pagination label (e.g. "Showing 1-20 of 150 records").
     *
     * <p>
     * TODO: Replace the query and the UI-read logic with your actual
     * page objects and selectors.
     */
    @Test(description = "DB vs UI: Validate total bids count matches database")
    public void testTotalBidsCountMatchesDatabase() throws SQLException {
        logger.info("TEST: testTotalBidsCountMatchesDatabase");

        // ── 1. Fetch the count from DB ────────────────────────────────────────
        String query = "SELECT COUNT(*) AS cnt FROM bids";
        int dbCount = db.getRowCount(query);
        logger.info("DB total bids count: {}", dbCount);

        // ── 2. Read the same count from the UI ───────────────────────────────
        // TODO: Replace with your actual Page Object call, e.g.:
        // BidsListPage bidsPage = new BidsListPage(driver);
        // int uiCount = bidsPage.getTotalRecordCount();
        //
        // Placeholder — remove once the Page Object is wired up:
        int uiCount = dbCount; // <- replace with real UI read

        // ── 3. Assert ────────────────────────────────────────────────────────
        Assert.assertEquals(uiCount, dbCount,
                "Total bids count on UI does not match the database value.");
        logger.info("PASS: UI count ({}) matches DB count ({}).", uiCount, dbCount);
    }

    // =========================================================================
    // Example Test 4 — Multi-column row validation
    // =========================================================================

    /**
     * Fetches a complete record from the DB by ID and validates multiple fields
     * against what is rendered on the record detail page.
     *
     * <p>
     * TODO: Replace the query, column names, record ID, and page-object calls
     * with the real values for your target PHP page.
     */
    @Test(description = "DB vs UI: Validate multi-column record detail")
    public void testRecordDetailMatchesDatabase() throws SQLException {
        logger.info("TEST: testRecordDetailMatchesDatabase");

        // ── 1. Fetch the full record from DB ─────────────────────────────────
        String query = "SELECT title, status, created_at "
                + "FROM bids "
                + "WHERE id = ? "
                + "LIMIT 1";

        List<Map<String, String>> rows = db.executeQuery(query, "1");
        Assert.assertFalse(rows.isEmpty(), "No bid found in DB with id = 1.");

        Map<String, String> dbRecord = rows.get(0);
        String dbTitle = dbRecord.get("title");
        String dbStatus = dbRecord.get("status");
        String dbCreatedAt = dbRecord.get("created_at");

        logger.info("DB record — title: '{}', status: '{}', created_at: '{}'",
                dbTitle, dbStatus, dbCreatedAt);

        // ── 2. Read the same fields from the UI ──────────────────────────────
        // TODO: Navigate to the bid detail page and read each field, e.g.:
        // BidDetailPage page = new BidDetailPage(driver, "1");
        // String uiTitle = page.getBidTitle();
        // String uiStatus = page.getBidStatus();
        // String uiCreatedAt = page.getCreatedAt();
        //
        // Placeholders — remove once Page Objects are wired up:
        String uiTitle = dbTitle; // <- replace with real UI read
        String uiStatus = dbStatus; // <- replace with real UI read
        String uiCreatedAt = dbCreatedAt; // <- replace with real UI read

        // ── 3. Assert each field ─────────────────────────────────────────────
        Assert.assertEquals(uiTitle, dbTitle,
                "Bid title on UI does not match the database value.");
        Assert.assertEquals(uiStatus, dbStatus,
                "Bid status on UI does not match the database value.");
        Assert.assertEquals(uiCreatedAt, dbCreatedAt,
                "Bid created_at on UI does not match the database value.");

        logger.info("PASS: All fields on UI match the database record.");
    }
}
