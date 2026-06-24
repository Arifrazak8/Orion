package com.orion.database;

import com.orion.constants.FrameworkConstants;
import com.orion.utils.ConfigReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * QueryExecutor — Retry-enabled, pooled query execution engine.
 *
 * <p>Key differences from the legacy {@link com.orion.utils.DatabaseUtils}:
 * <ul>
 *   <li>Uses {@link ConnectionPoolManager} instead of single-connection
 *       {@code DriverManager.getConnection()}</li>
 *   <li>Automatic retry with configurable count and back-off delay</li>
 *   <li>Structured logging with query execution time</li>
 *   <li>Read-only enforcement (SELECT / WITH queries only)</li>
 * </ul>
 *
 * <p>The legacy {@code DatabaseUtils} is <b>not</b> removed — tests that
 * already use it continue to work. New tests should prefer this class.
 */
public class QueryExecutor {

    private static final Logger logger = LogManager.getLogger(QueryExecutor.class);

    private final int maxRetries;
    private final long retryDelayMs;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Creates a QueryExecutor with retry settings from {@code config.properties}.
     */
    public QueryExecutor() {
        this.maxRetries = Integer.parseInt(
                ConfigReader.getProperty("db.retry.count",
                        String.valueOf(FrameworkConstants.DB_QUERY_RETRY_COUNT)));
        this.retryDelayMs = Long.parseLong(
                ConfigReader.getProperty("db.retry.delay.ms",
                        String.valueOf(FrameworkConstants.DB_QUERY_RETRY_DELAY_MS)));
    }

    /**
     * Creates a QueryExecutor with explicit retry settings.
     *
     * @param maxRetries   Maximum number of retry attempts.
     * @param retryDelayMs Delay between retries in milliseconds.
     */
    public QueryExecutor(int maxRetries, long retryDelayMs) {
        this.maxRetries = maxRetries;
        this.retryDelayMs = retryDelayMs;
    }

    // -------------------------------------------------------------------------
    // Query execution
    // -------------------------------------------------------------------------

    /**
     * Executes a parameterised SELECT query and returns all result rows.
     *
     * <p>Each row is a {@code Map<columnName, cellValue>} using
     * {@link LinkedHashMap} to preserve column order.
     *
     * <p>Retries on transient {@link SQLException}s up to {@code maxRetries}
     * times with a configurable delay between attempts.
     *
     * @param sql    A SELECT statement, optionally containing {@code ?} placeholders.
     * @param params Zero or more parameter values to bind (in order).
     * @return A {@code List} of rows; empty list if no data found.
     * @throws SQLException if the query fails after all retries.
     */
    public List<Map<String, String>> executeQuery(String sql, String... params)
            throws SQLException {
        validateSelectOnly(sql);

        SQLException lastException = null;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            long startTime = System.currentTimeMillis();
            try (Connection conn = ConnectionPoolManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                for (int i = 0; i < params.length; i++) {
                    stmt.setString(i + 1, params[i]);
                }

                logger.info("Executing query (attempt {}/{}): {}", attempt, maxRetries, sql);
                if (params.length > 0) {
                    logger.debug("Query parameters: {}", (Object) params);
                }

                try (ResultSet rs = stmt.executeQuery()) {
                    List<Map<String, String>> resultList = mapResultSet(rs);
                    long elapsed = System.currentTimeMillis() - startTime;
                    logger.info("Query returned {} row(s) in {}ms.", resultList.size(), elapsed);
                    return resultList;
                }

            } catch (SQLException e) {
                lastException = e;
                long elapsed = System.currentTimeMillis() - startTime;
                logger.warn("Query attempt {}/{} failed after {}ms: {}",
                        attempt, maxRetries, elapsed, e.getMessage());

                if (attempt < maxRetries) {
                    sleep(retryDelayMs);
                }
            }
        }
        throw new SQLException("Query failed after " + maxRetries + " attempts.", lastException);
    }

    // -------------------------------------------------------------------------
    // Convenience helpers
    // -------------------------------------------------------------------------

    /**
     * Returns the value of a single cell — the first row, named column.
     */
    public String getSingleValue(String sql, String columnName, String... params)
            throws SQLException {
        List<Map<String, String>> rows = executeQuery(sql, params);
        if (rows.isEmpty()) {
            logger.warn("Query returned no rows. SQL: {}", sql);
            return null;
        }
        String value = rows.get(0).get(columnName);
        logger.info("Single value fetched — column: '{}', value: '{}'", columnName, value);
        return value;
    }

    /**
     * Returns all values from a single named column across all result rows.
     */
    public List<String> getColumnValues(String sql, String columnName, String... params)
            throws SQLException {
        List<Map<String, String>> rows = executeQuery(sql, params);
        List<String> values = new ArrayList<>();
        for (Map<String, String> row : rows) {
            values.add(row.get(columnName));
        }
        logger.info("Extracted {} value(s) from column '{}'.", values.size(), columnName);
        return values;
    }

    /**
     * Returns the total row count for a query.
     * Assumes the query returns a column aliased as {@code cnt}.
     */
    public int getRowCount(String sql, String... params) throws SQLException {
        String value = getSingleValue(sql, "cnt", params);
        if (value == null) return 0;
        return Integer.parseInt(value);
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /**
     * Maps a ResultSet into a list of row maps.
     */
    private List<Map<String, String>> mapResultSet(ResultSet rs) throws SQLException {
        List<Map<String, String>> resultList = new ArrayList<>();
        ResultSetMetaData meta = rs.getMetaData();
        int columnCount = meta.getColumnCount();

        while (rs.next()) {
            Map<String, String> row = new LinkedHashMap<>();
            for (int col = 1; col <= columnCount; col++) {
                String colName = meta.getColumnLabel(col);
                String colValue = rs.getString(col);
                row.put(colName, colValue != null ? colValue : "");
            }
            resultList.add(row);
        }
        return resultList;
    }

    /**
     * Guards against accidental write queries.
     */
    private void validateSelectOnly(String sql) {
        String trimmed = sql.trim().toUpperCase();
        if (!trimmed.startsWith("SELECT") && !trimmed.startsWith("WITH")) {
            throw new UnsupportedOperationException(
                    "QueryExecutor is READ-ONLY. Only SELECT and WITH queries are permitted. Got: " + sql);
        }
    }

    /**
     * Sleep without checked-exception noise.
     */
    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
