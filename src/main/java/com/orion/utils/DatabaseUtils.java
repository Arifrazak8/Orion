package com.orion.utils;

import java.sql.Connection;
import java.sql.DriverManager;
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
 * DatabaseUtils — Read-only JDBC utility for the Orion QA automation framework.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Open / close a MySQL connection using values from {@code config.properties}</li>
 *   <li>Execute SELECT queries and return results as a {@code List<Map<String,String>>}
 *       so individual columns can be accessed by name in any test.</li>
 *   <li>Provide convenience helpers to fetch a single cell value or a whole column.</li>
 * </ul>
 *
 * <p>Usage example inside a test:
 * <pre>{@code
 *   DatabaseUtils db = new DatabaseUtils();
 *   try {
 *       db.connect();
 *
 *       // Fetch all rows
 *       List<Map<String,String>> rows = db.executeQuery(
 *           "SELECT name, email FROM users WHERE id = ?", "42");
 *
 *       // Validate against UI value
 *       String dbName = rows.get(0).get("name");
 *       Assert.assertEquals(uiName, dbName, "Name mismatch between DB and UI");
 *
 *   } finally {
 *       db.disconnect();
 *   }
 * }</pre>
 */
public class DatabaseUtils {

    private static final Logger logger = LogManager.getLogger(DatabaseUtils.class);

    private Connection connection;

    // -------------------------------------------------------------------------
    // Connection management
    // -------------------------------------------------------------------------

    /**
     * Opens a MySQL connection using credentials from {@code config.properties}.
     * <p>Required keys: {@code db.url}, {@code db.username}, {@code db.password}.
     *
     * @throws SQLException if the connection cannot be established.
     */
    public void connect() throws SQLException {
        String url      = ConfigReader.getProperty("db.url");
        String username = ConfigReader.getProperty("db.username");
        String password = ConfigReader.getProperty("db.password", "");

        if (url == null || username == null) {
            throw new IllegalStateException(
                "Database configuration is missing. Please set db.url and db.username in config.properties.");
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC driver not found on the classpath.", e);
        }

        connection = DriverManager.getConnection(url, username, password);
        logger.info("Database connection established to: {}", url);
    }

    /**
     * Closes the active database connection if it is open.
     */
    public void disconnect() {
        if (connection != null) {
            try {
                connection.close();
                logger.info("Database connection closed.");
            } catch (SQLException e) {
                logger.warn("Failed to close database connection cleanly.", e);
            } finally {
                connection = null;
            }
        }
    }

    /**
     * Returns {@code true} if the connection is currently open.
     */
    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    // -------------------------------------------------------------------------
    // Query execution (SELECT only)
    // -------------------------------------------------------------------------

    /**
     * Executes a parameterised SELECT query and returns all result rows.
     *
     * <p>Each row is represented as a {@code Map<columnName, cellValue>}.
     * Column names are taken directly from the {@link ResultSetMetaData} so
     * they match the column aliases used in your query.
     *
     * @param sql    A SELECT statement, optionally containing {@code ?} placeholders.
     * @param params Zero or more parameter values to bind (in order).
     * @return A {@code List} of rows; empty list if no data found.
     * @throws SQLException           if the query fails or returns an error.
     * @throws IllegalStateException  if {@link #connect()} has not been called yet.
     */
    public List<Map<String, String>> executeQuery(String sql, String... params) throws SQLException {
        ensureConnected();
        validateSelectOnly(sql);

        List<Map<String, String>> resultList = new ArrayList<>();

        logger.info("Executing DB query: {}", sql);
        if (params.length > 0) {
            logger.info("Query parameters: {}", (Object) params);
        }

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setString(i + 1, params[i]);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                int columnCount = meta.getColumnCount();

                while (rs.next()) {
                    Map<String, String> row = new LinkedHashMap<>();
                    for (int col = 1; col <= columnCount; col++) {
                        String colName  = meta.getColumnLabel(col);
                        String colValue = rs.getString(col);
                        row.put(colName, colValue != null ? colValue : "");
                    }
                    resultList.add(row);
                }
            }
        }

        logger.info("Query returned {} row(s).", resultList.size());
        return resultList;
    }

    // -------------------------------------------------------------------------
    // Convenience helpers
    // -------------------------------------------------------------------------

    /**
     * Returns the value of a single cell — the first row, named column.
     * Useful when you expect exactly one result (e.g. {@code SELECT COUNT(*)}).
     *
     * @param sql        A SELECT statement (may contain {@code ?} placeholders).
     * @param columnName The column name or alias to read from the result.
     * @param params     Optional bind parameters.
     * @return The cell value as a String, or {@code null} if no rows are returned.
     * @throws SQLException if the query fails.
     */
    public String getSingleValue(String sql, String columnName, String... params) throws SQLException {
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
     * Useful for building a list of IDs or names to compare against the UI.
     *
     * @param sql        A SELECT statement (may contain {@code ?} placeholders).
     * @param columnName The column name or alias to extract.
     * @param params     Optional bind parameters.
     * @return A {@code List<String>} of cell values (may be empty).
     * @throws SQLException if the query fails.
     */
    public List<String> getColumnValues(String sql, String columnName, String... params) throws SQLException {
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
     * Convenience wrapper for {@code SELECT COUNT(*) AS cnt ...} style queries.
     *
     * @param sql    A {@code SELECT COUNT(*) AS cnt ...} statement.
     * @param params Optional bind parameters.
     * @return The integer count, or {@code 0} if no rows returned.
     * @throws SQLException if the query fails.
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
     * Asserts the connection is open before running any query.
     */
    private void ensureConnected() {
        if (!isConnected()) {
            throw new IllegalStateException(
                "No active database connection. Call DatabaseUtils.connect() first.");
        }
    }

    /**
     * Guards against accidental write queries being executed through this utility.
     * Only SELECT statements are allowed.
     */
    private void validateSelectOnly(String sql) {
        String trimmed = sql.trim().toUpperCase();
        if (!trimmed.startsWith("SELECT") && !trimmed.startsWith("WITH")) {
            throw new UnsupportedOperationException(
                "DatabaseUtils is READ-ONLY. Only SELECT and WITH queries are permitted. Got: " + sql);
        }
    }
}
