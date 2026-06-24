package com.orion.database;

import com.orion.constants.FrameworkConstants;
import com.orion.utils.ConfigReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * ConnectionFactory — Multi-database connection factory supporting MySQL,
 * PostgreSQL, and SQL Server.
 *
 * <p>Determines the database type from the {@code db.type} property in
 * {@code config.properties} and loads the appropriate JDBC driver.
 * Falls back to MySQL when no type is specified, preserving backward
 * compatibility with the existing {@link com.orion.utils.DatabaseUtils}.
 *
 * <p>Supported values for {@code db.type}:
 * <ul>
 *   <li>{@code mysql}      — MySQL (default)</li>
 *   <li>{@code postgresql}  — PostgreSQL</li>
 *   <li>{@code sqlserver}   — Microsoft SQL Server</li>
 * </ul>
 */
public class ConnectionFactory {

    private static final Logger logger = LogManager.getLogger(ConnectionFactory.class);

    /**
     * Enum of supported database types with their JDBC driver class names.
     */
    public enum DatabaseType {
        MYSQL("com.mysql.cj.jdbc.Driver"),
        POSTGRESQL("org.postgresql.Driver"),
        SQLSERVER("com.microsoft.sqlserver.jdbc.SQLServerDriver");

        private final String driverClass;

        DatabaseType(String driverClass) {
            this.driverClass = driverClass;
        }

        public String getDriverClass() {
            return driverClass;
        }

        /**
         * Resolves a {@code DatabaseType} from a case-insensitive string.
         *
         * @param type String like "mysql", "postgresql", "sqlserver".
         * @return Matching enum value, or {@code MYSQL} as fallback.
         */
        public static DatabaseType fromString(String type) {
            if (type == null || type.isBlank()) {
                return MYSQL;
            }
            return switch (type.trim().toLowerCase()) {
                case "postgresql", "postgres", "pg" -> POSTGRESQL;
                case "sqlserver", "mssql" -> SQLSERVER;
                default -> MYSQL;
            };
        }
    }

    private ConnectionFactory() {
        // Utility class — not instantiable
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Creates a new JDBC {@link Connection} using credentials from
     * {@code config.properties}.
     *
     * <p>Required config keys: {@code db.url}, {@code db.username}.
     * Optional: {@code db.password} (defaults to empty), {@code db.type}
     * (defaults to "mysql").
     *
     * @return An open JDBC Connection.
     * @throws SQLException if the connection cannot be established.
     */
    public static Connection createConnection() throws SQLException {
        String url = ConfigReader.getProperty("db.url");
        String username = ConfigReader.getProperty("db.username");
        String password = ConfigReader.getProperty("db.password", "");
        String dbTypeStr = ConfigReader.getProperty("db.type",
                FrameworkConstants.DEFAULT_DB_TYPE);

        return createConnection(url, username, password, dbTypeStr);
    }

    /**
     * Creates a JDBC connection with explicit parameters.
     *
     * @param url      JDBC URL.
     * @param username Database username.
     * @param password Database password.
     * @param dbType   Database type string (mysql/postgresql/sqlserver).
     * @return An open JDBC Connection.
     * @throws SQLException if the connection cannot be established.
     */
    public static Connection createConnection(String url, String username,
                                               String password, String dbType)
            throws SQLException {
        if (url == null || username == null) {
            throw new IllegalStateException(
                    "Database configuration is missing. Please set db.url and "
                            + "db.username in config.properties.");
        }

        DatabaseType type = DatabaseType.fromString(dbType);
        loadDriver(type);

        logger.info("Creating {} connection to: {}", type, url);
        Connection connection = DriverManager.getConnection(url, username, password);
        logger.info("Database connection established successfully.");
        return connection;
    }

    /**
     * Returns the resolved {@link DatabaseType} based on the current config.
     */
    public static DatabaseType getConfiguredDatabaseType() {
        String dbTypeStr = ConfigReader.getProperty("db.type",
                FrameworkConstants.DEFAULT_DB_TYPE);
        return DatabaseType.fromString(dbTypeStr);
    }

    // -------------------------------------------------------------------------
    // Internal
    // -------------------------------------------------------------------------

    /**
     * Loads the JDBC driver class for the given database type.
     */
    private static void loadDriver(DatabaseType type) throws SQLException {
        try {
            Class.forName(type.getDriverClass());
            logger.debug("Loaded JDBC driver: {}", type.getDriverClass());
        } catch (ClassNotFoundException e) {
            throw new SQLException(
                    type.name() + " JDBC driver not found on the classpath: "
                            + type.getDriverClass(), e);
        }
    }
}
