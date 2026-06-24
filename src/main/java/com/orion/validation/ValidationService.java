package com.orion.validation;

import com.orion.database.QueryExecutor;
import com.orion.database.QueryStore;
import com.orion.mapping.TableMappingConfig;
import com.orion.mapping.TableMappingLoader;
import com.orion.reporting.ReportingService;
import com.orion.utils.TableUtils;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * ValidationService — High-level facade that orchestrates the full
 * table-validation workflow in a single method call:
 *
 * <ol>
 *   <li>Load table mapping config from JSON</li>
 *   <li>Capture UI data via {@link TableUtils}</li>
 *   <li>Capture DB data via {@link QueryExecutor} + {@link QueryStore}</li>
 *   <li>Compare using {@link TableComparisonEngine}</li>
 *   <li>Generate HTML + Excel reports via {@link ReportingService}</li>
 * </ol>
 *
 * <p>Usage in a test:
 * <pre>{@code
 *   PivotReportPage page = new PivotReportPage(driver);
 *   TableUtils positiveTable = page.getPositiveTable();
 *
 *   ValidationService vs = new ValidationService();
 *   TableComparisonResult result = vs.validateTable("pivot_positive_bids", positiveTable);
 *
 *   Assert.assertTrue(result.isAllPassed(), result.toSummaryString());
 * }</pre>
 */
public class ValidationService {

    private static final Logger logger = LogManager.getLogger(ValidationService.class);

    private final DataCaptureService captureService;
    private final TableComparisonEngine comparisonEngine;
    private final ReportingService reportingService;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /** Creates a ValidationService with default dependencies. */
    public ValidationService() {
        this.captureService = new DataCaptureService();
        this.comparisonEngine = new TableComparisonEngine();
        this.reportingService = new ReportingService();
    }

    /** Creates a ValidationService with injected dependencies (for testability). */
    public ValidationService(DataCaptureService captureService,
                              TableComparisonEngine comparisonEngine,
                              ReportingService reportingService) {
        this.captureService = captureService;
        this.comparisonEngine = comparisonEngine;
        this.reportingService = reportingService;
    }

    // -------------------------------------------------------------------------
    // Full validation workflow
    // -------------------------------------------------------------------------

    /**
     * Validates a UI table against its database counterpart using the
     * named mapping configuration.
     *
     * <p>This is the primary entry point for table validation.
     *
     * @param mappingName Name of the mapping file (without {@code .json}).
     * @param tableUtils  {@link TableUtils} instance pointing to the UI table.
     * @return The comparison result.
     * @throws SQLException if the DB query fails.
     */
    public TableComparisonResult validateTable(String mappingName, TableUtils tableUtils)
            throws SQLException {
        logger.info("=== Starting full validation for mapping: '{}' ===", mappingName);

        // 1. Load mapping config
        TableMappingConfig mapping = TableMappingLoader.getMapping(mappingName);
        logger.info("Loaded mapping: {}", mapping);

        // 2. Capture UI data
        Map<String, Map<String, String>> uiData = captureService.captureUiData(tableUtils);

        // 3. Capture DB data
        String sql = QueryStore.getQuery(mapping.getQueryName());
        List<Map<String, String>> dbData = captureService.captureDbDataWithSql(sql);

        // 4. Compare
        TableComparisonResult result = comparisonEngine.compare(mapping, uiData, dbData);

        // 5. Report
        try {
            reportingService.reportComparison(result);
        } catch (Exception e) {
            logger.warn("Report generation failed (non-fatal): {}", e.getMessage(), e);
        }

        logger.info("=== Validation complete: {} ===", result.toSummaryString());
        return result;
    }

    /**
     * Validates a UI table using a raw SQL query and a mapping config,
     * without requiring the query to be in the QueryStore.
     *
     * @param mappingName Name of the mapping file.
     * @param tableUtils  TableUtils for the UI table.
     * @param sql         Raw SQL query.
     * @param params      Optional bind parameters.
     * @return The comparison result.
     * @throws SQLException if the DB query fails.
     */
    public TableComparisonResult validateTableWithSql(String mappingName,
                                                       TableUtils tableUtils,
                                                       String sql,
                                                       String... params)
            throws SQLException {
        logger.info("=== Starting validation for mapping: '{}' (custom SQL) ===", mappingName);

        TableMappingConfig mapping = TableMappingLoader.getMapping(mappingName);
        Map<String, Map<String, String>> uiData = captureService.captureUiData(tableUtils);
        List<Map<String, String>> dbData = captureService.captureDbDataWithSql(sql, params);

        TableComparisonResult result = comparisonEngine.compare(mapping, uiData, dbData);

        try {
            reportingService.reportComparison(result);
        } catch (Exception e) {
            logger.warn("Report generation failed (non-fatal): {}", e.getMessage(), e);
        }

        logger.info("=== Validation complete: {} ===", result.toSummaryString());
        return result;
    }

    /**
     * Performs a direct-match comparison (assumes UI and DB column names are
     * identical) without needing a mapping file.
     *
     * @param tableName      Logical name for the comparison.
     * @param tableUtils     TableUtils for the UI table.
     * @param sql            SQL query returning matching column names.
     * @param rowIdDbColumn  DB column used as the row key.
     * @param params         Optional bind parameters.
     * @return The comparison result.
     * @throws SQLException if the DB query fails.
     */
    public TableComparisonResult validateTableDirect(String tableName,
                                                      TableUtils tableUtils,
                                                      String sql,
                                                      String rowIdDbColumn,
                                                      String... params)
            throws SQLException {
        logger.info("=== Starting direct validation for: '{}' ===", tableName);

        Map<String, Map<String, String>> uiData = captureService.captureUiData(tableUtils);
        List<Map<String, String>> dbData = captureService.captureDbDataWithSql(sql, params);

        TableComparisonResult result = comparisonEngine.compareDirectMatch(
                tableName, uiData, dbData, rowIdDbColumn);

        try {
            reportingService.reportComparison(result);
        } catch (Exception e) {
            logger.warn("Report generation failed (non-fatal): {}", e.getMessage(), e);
        }

        logger.info("=== Validation complete: {} ===", result.toSummaryString());
        return result;
    }

    // -------------------------------------------------------------------------
    // Component accessors (for advanced usage)
    // -------------------------------------------------------------------------

    public DataCaptureService getCaptureService()          { return captureService; }
    public TableComparisonEngine getComparisonEngine()     { return comparisonEngine; }
    public ReportingService getReportingService()           { return reportingService; }
}
