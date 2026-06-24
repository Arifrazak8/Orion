package com.orion.database;

import com.orion.constants.FrameworkConstants;
import com.orion.utils.ConfigReader;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * ConnectionPoolManager — HikariCP-based connection pool manager.
 *
 * <p>Provides a shared, high-performance connection pool whose lifecycle is
 * managed explicitly by the test framework (typically opened in
 * {@code @BeforeSuite} and closed in {@code @AfterSuite}).
 *
 * <p>Configuration keys read from {@code config.properties}:
 * <ul>
 *   <li>{@code db.url}                       — JDBC URL (required)</li>
 *   <li>{@code db.username}                  — DB username (required)</li>
 *   <li>{@code db.password}                  — DB password (defaults to "")</li>
 *   <li>{@code db.type}                      — mysql / postgresql / sqlserver</li>
 *   <li>{@code db.pool.size}                 — Maximum pool size (default 5)</li>
 *   <li>{@code db.pool.idle.timeout}         — Idle timeout in ms (default 30000)</li>
 *   <li>{@code db.pool.max.lifetime}         — Max connection lifetime in ms (default 600000)</li>
 *   <li>{@code db.pool.connection.timeout}   — Connection timeout in ms (default 10000)</li>
 * </ul>
 */
public class ConnectionPoolManager {

    private static final Logger logger = LogManager.getLogger(ConnectionPoolManager.class);

    private static volatile HikariDataSource dataSource;
    private static final Object LOCK = new Object();

    private ConnectionPoolManager() {
        // Utility class — not instantiable
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Initialises the connection pool using values from {@code config.properties}.
     * Safe to call multiple times — subsequent calls are no-ops if the pool
     * is already active.
     */
    public static void initialize() {
        if (dataSource != null && !dataSource.isClosed()) {
            logger.debug("Connection pool already initialised — skipping.");
            return;
        }

        synchronized (LOCK) {
            if (dataSource != null && !dataSource.isClosed()) {
                return;
            }

            String url = ConfigReader.getProperty("db.url");
            String username = ConfigReader.getProperty("db.username");
            String password = ConfigReader.getProperty("db.password", "");

            if (url == null || username == null) {
                throw new IllegalStateException(
                        "Database configuration is missing. Please set db.url and "
                                + "db.username in config.properties.");
            }

            ConnectionFactory.DatabaseType dbType = ConnectionFactory.getConfiguredDatabaseType();

            int poolSize = Integer.parseInt(ConfigReader.getProperty(
                    "db.pool.size",
                    String.valueOf(FrameworkConstants.DEFAULT_DB_POOL_SIZE)));
            long idleTimeout = Long.parseLong(ConfigReader.getProperty(
                    "db.pool.idle.timeout",
                    String.valueOf(FrameworkConstants.DEFAULT_DB_POOL_IDLE_TIMEOUT_MS)));
            long maxLifetime = Long.parseLong(ConfigReader.getProperty(
                    "db.pool.max.lifetime",
                    String.valueOf(FrameworkConstants.DEFAULT_DB_POOL_MAX_LIFETIME_MS)));
            long connectionTimeout = Long.parseLong(ConfigReader.getProperty(
                    "db.pool.connection.timeout",
                    String.valueOf(FrameworkConstants.DEFAULT_DB_POOL_CONNECTION_TIMEOUT_MS)));

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(url);
            config.setUsername(username);
            config.setPassword(password);
            config.setDriverClassName(dbType.getDriverClass());
            config.setMaximumPoolSize(poolSize);
            config.setIdleTimeout(idleTimeout);
            config.setMaxLifetime(maxLifetime);
            config.setConnectionTimeout(connectionTimeout);
            config.setPoolName("OrionQA-Pool");
            config.setReadOnly(true);                       // QA is read-only
            config.setAutoCommit(true);
            config.setLeakDetectionThreshold(60_000);       // Log warning if connection not return
            // ed within 60s

            dataSource = new HikariDataSource(config);
            logger.info("Connection pool initialised — type: {}, poolSize: {}, url: {}",
                    dbType, poolSize, url);
        }
    }

    /**
     * Borrows a connection from the pool.
     *
     * <p><strong>Important:</strong> Always return the connection using
     * try-with-resources or by calling {@code connection.close()} — HikariCP
     * returns it to the pool rather than physically closing it.
     *
     * @return A pooled JDBC Connection.
     * @throws SQLException if the pool is not initialised or a connection
     *                      cannot be obtained within the configured timeout.
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            throw new IllegalStateException(
                    "Connection pool is not initialised. Call ConnectionPoolManager.initialize() first.");
        }
        return dataSource.getConnection();
    }

    /**
     * Shuts down the connection pool, releasing all connections.
     * Safe to call even if the pool was never initialised.
     */
    public static void shutdown() {
        synchronized (LOCK) {
            if (dataSource != null && !dataSource.isClosed()) {
                dataSource.close();
                logger.info("Connection pool shut down.");
            }
            dataSource = null;
        }
    }

    /**
     * Returns {@code true} if the pool is currently active and accepting
     * connection requests.
     */
    public static boolean isActive() {
        return dataSource != null && !dataSource.isClosed();
    }

    /**
     * Returns the number of connections currently in use.
     */
    public static int getActiveConnections() {
        if (dataSource == null) return 0;
        return dataSource.getHikariPoolMXBean().getActiveConnections();
    }

    /**
     * Returns the number of idle connections waiting in the pool.
     */
    public static int getIdleConnections() {
        if (dataSource == null) return 0;
        return dataSource.getHikariPoolMXBean().getIdleConnections();
    }
}
