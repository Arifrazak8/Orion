package com.orion.constants;

/**
 * FrameworkConstants — Centralised, immutable constants used across the
 * Orion QA automation framework.
 *
 * <p>All magic numbers, default paths, and format strings that were previously
 * scattered across test and utility classes are consolidated here so they can
 * be maintained in one place.
 *
 * <p>These are compile-time defaults. Runtime overrides should be read via
 * {@link com.orion.utils.ConfigReader} (which already supports default values).
 */
public final class FrameworkConstants {

    private FrameworkConstants() {
        // Utility class — not instantiable
    }

    // -------------------------------------------------------------------------
    // Timeouts (seconds)
    // -------------------------------------------------------------------------

    /** Default implicit wait applied to the WebDriver. */
    public static final int DEFAULT_IMPLICIT_WAIT = 10;

    /** Default explicit wait used by {@code BasePage} and {@code WaitUtils}. */
    public static final int DEFAULT_EXPLICIT_WAIT = 15;

    /** Default polling interval for fluent waits (milliseconds). */
    public static final long DEFAULT_POLL_INTERVAL_MS = 500;

    /** Default page-load timeout (seconds). */
    public static final int DEFAULT_PAGE_LOAD_TIMEOUT = 30;

    // -------------------------------------------------------------------------
    // Retry defaults
    // -------------------------------------------------------------------------

    /** Maximum retries for stale-element operations in TableUtils. */
    public static final int STALE_ELEMENT_RETRY_COUNT = 3;

    /** Delay between stale-element retries (milliseconds). */
    public static final long STALE_ELEMENT_RETRY_DELAY_MS = 500;

    /** Maximum retries for database query execution. */
    public static final int DB_QUERY_RETRY_COUNT = 3;

    /** Delay between database query retries (milliseconds). */
    public static final long DB_QUERY_RETRY_DELAY_MS = 1000;

    // -------------------------------------------------------------------------
    // File / Directory paths (defaults — overridable via config.properties)
    // -------------------------------------------------------------------------

    /** Default screenshot output directory. */
    public static final String DEFAULT_SCREENSHOT_PATH = "./screenshots/";

    /** Default Extent HTML report path. */
    public static final String DEFAULT_REPORT_PATH = "./reports/ExtentReport.html";

    /** Default Excel comparison report directory. */
    public static final String DEFAULT_EXCEL_REPORT_PATH = "./reports/comparison/";

    /** Default directory for externalized SQL query files. */
    public static final String DEFAULT_QUERIES_PATH = "src/test/resources/queries/";

    /** Default directory for table-mapping JSON files. */
    public static final String DEFAULT_MAPPINGS_PATH = "src/test/resources/mappings/";

    /** Default path for the test-data Excel workbook. */
    public static final String DEFAULT_TESTDATA_PATH = "src/test/resources/testdata.xlsx";

    /** Default config.properties path. */
    public static final String DEFAULT_CONFIG_PATH = "src/test/resources/config.properties";

    /** Default log output directory. */
    public static final String DEFAULT_LOG_PATH = "./logs/";

    // -------------------------------------------------------------------------
    // Date / Number formatting
    // -------------------------------------------------------------------------

    /** Timestamp format used in screenshot filenames and report names. */
    public static final String TIMESTAMP_FORMAT = "yyyyMMdd_HHmmss";

    /** ISO-8601 date-time format for structured logging. */
    public static final String ISO_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    /** US currency format pattern (e.g. "#,###"). */
    public static final String CURRENCY_FORMAT_PATTERN = "#,###";

    // -------------------------------------------------------------------------
    // Database connection pool defaults
    // -------------------------------------------------------------------------

    /** Default database type when not specified in config. */
    public static final String DEFAULT_DB_TYPE = "mysql";

    /** Default connection pool size. */
    public static final int DEFAULT_DB_POOL_SIZE = 5;

    /** Default connection pool idle timeout (milliseconds). */
    public static final long DEFAULT_DB_POOL_IDLE_TIMEOUT_MS = 30_000;

    /** Default connection pool max lifetime (milliseconds). */
    public static final long DEFAULT_DB_POOL_MAX_LIFETIME_MS = 600_000;

    /** Default connection pool connection timeout (milliseconds). */
    public static final long DEFAULT_DB_POOL_CONNECTION_TIMEOUT_MS = 10_000;

    // -------------------------------------------------------------------------
    // Comparison statuses
    // -------------------------------------------------------------------------

    /** Status label for a passing comparison. */
    public static final String STATUS_PASS = "PASS";

    /** Status label for a failing comparison. */
    public static final String STATUS_FAIL = "FAIL";

    /** Status label for a value missing on one side. */
    public static final String STATUS_MISSING = "MISSING";

    /** Status label for a skipped comparison. */
    public static final String STATUS_SKIPPED = "SKIPPED";

    // -------------------------------------------------------------------------
    // Browser / WebDriver
    // -------------------------------------------------------------------------

    /** Chrome remote-debugging address used for browser reuse. */
    public static final String CHROME_DEBUG_ADDRESS = "127.0.0.1:9222";

    /** Default browser when none is configured. */
    public static final String DEFAULT_BROWSER = "chrome";
}
