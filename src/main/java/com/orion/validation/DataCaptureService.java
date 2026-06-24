package com.orion.validation;

import com.orion.database.QueryExecutor;
import com.orion.database.QueryStore;
import com.orion.utils.TableUtils;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * DataCaptureService — Orchestrates data capture from both the UI
 * (via {@link TableUtils}) and the database (via {@link QueryExecutor}
 * + {@link QueryStore}).
 *
 * <p>Returns paired datasets ready for comparison by
 * {@link TableComparisonEngine}.
 */
public class DataCaptureService {

    private static final Logger logger = LogManager.getLogger(DataCaptureService.class);

    private final QueryExecutor queryExecutor;

    public DataCaptureService() {
        this.queryExecutor = new QueryExecutor();
    }

    public DataCaptureService(QueryExecutor queryExecutor) {
        this.queryExecutor = queryExecutor;
    }

    // -------------------------------------------------------------------------
    // UI data capture
    // -------------------------------------------------------------------------

    /**
     * Captures all table data from the UI using the provided {@link TableUtils}.
     *
     * @param tableUtils Initialised TableUtils pointing to the target table.
     * @return Map of RowKey → Map of ColumnName → CellValue.
     */
    public Map<String, Map<String, String>> captureUiData(TableUtils tableUtils) {
        logger.info("Capturing UI table data...");
        Map<String, Map<String, String>> data = tableUtils.getTableData();
        logger.info("Captured {} UI rows.", data.size());
        return data;
    }

    /**
     * Captures column headers from the UI table.
     *
     * @param tableUtils Initialised TableUtils.
     * @return List of column header names.
     */
    public List<String> captureUiHeaders(TableUtils tableUtils) {
        logger.info("Capturing UI column headers...");
        return tableUtils.getHeaders();
    }

    // -------------------------------------------------------------------------
    // DB data capture
    // -------------------------------------------------------------------------

    /**
     * Captures data from the database using a named query from the QueryStore.
     *
     * @param queryName Name of the query in the QueryStore.
     * @param params    Optional bind parameters for the query.
     * @return List of row maps.
     * @throws SQLException if the query fails.
     */
    public List<Map<String, String>> captureDbData(String queryName, String... params)
            throws SQLException {
        logger.info("Capturing DB data using query: '{}'", queryName);
        String sql = QueryStore.getQuery(queryName);
        List<Map<String, String>> data = queryExecutor.executeQuery(sql, params);
        logger.info("Captured {} DB rows.", data.size());
        return data;
    }

    /**
     * Captures data from the database using a raw SQL string.
     *
     * @param sql    SQL query string.
     * @param params Optional bind parameters.
     * @return List of row maps.
     * @throws SQLException if the query fails.
     */
    public List<Map<String, String>> captureDbDataWithSql(String sql, String... params)
            throws SQLException {
        logger.info("Capturing DB data using raw SQL...");
        List<Map<String, String>> data = queryExecutor.executeQuery(sql, params);
        logger.info("Captured {} DB rows.", data.size());
        return data;
    }

    // -------------------------------------------------------------------------
    // Convenience — capture both
    // -------------------------------------------------------------------------

    /**
     * Captures both UI and DB data in a single call and returns them
     * wrapped in a {@link CapturedData} record.
     *
     * @param tableUtils Table utils for UI capture.
     * @param queryName  Query store name for DB capture.
     * @param params     Optional bind parameters.
     * @return Captured paired data.
     * @throws SQLException if the DB query fails.
     */
    public CapturedData captureBoth(TableUtils tableUtils, String queryName, String... params)
            throws SQLException {
        Map<String, Map<String, String>> uiData = captureUiData(tableUtils);
        List<Map<String, String>> dbData = captureDbData(queryName, params);
        return new CapturedData(uiData, dbData);
    }

    /**
     * Container for paired UI + DB data.
     */
    public record CapturedData(
            Map<String, Map<String, String>> uiData,
            List<Map<String, String>> dbData
    ) {
    }
}
