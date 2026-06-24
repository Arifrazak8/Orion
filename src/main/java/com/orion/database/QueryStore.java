package com.orion.database;

import com.orion.constants.FrameworkConstants;
import com.orion.utils.ConfigReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * QueryStore — External SQL file loader and cache.
 *
 * <p>Loads SQL from {@code .sql} files stored in the queries directory
 * (default: {@code src/test/resources/queries/}).  Each query is identified
 * by its filename (without the {@code .sql} extension).
 *
 * <p>Example usage:
 * <pre>{@code
 *   // File: src/test/resources/queries/pivot_positive_total.sql
 *   String sql = QueryStore.getQuery("pivot_positive_total");
 *   List<Map<String,String>> rows = executor.executeQuery(sql, "3");
 * }</pre>
 *
 * <p>Loaded queries are cached in a {@link ConcurrentHashMap} so each file
 * is read only once per JVM lifetime.
 */
public class QueryStore {

    private static final Logger logger = LogManager.getLogger(QueryStore.class);

    /** Cached queries: key = query name, value = SQL string. */
    private static final Map<String, String> queryCache = new ConcurrentHashMap<>();

    private QueryStore() {
        // Utility class — not instantiable
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Returns the SQL content for the given query name.
     *
     * <p>Resolution order:
     * <ol>
     *   <li>In-memory cache</li>
     *   <li>File on disk at {@code <queries.path>/<queryName>.sql}</li>
     *   <li>Classpath resource at {@code /queries/<queryName>.sql}</li>
     * </ol>
     *
     * @param queryName Filename without the {@code .sql} extension
     *                  (e.g. "pivot_positive_total").
     * @return The SQL content as a trimmed string.
     * @throws IllegalArgumentException if the query file cannot be found.
     */
    public static String getQuery(String queryName) {
        return queryCache.computeIfAbsent(queryName, QueryStore::loadQuery);
    }

    /**
     * Registers an in-memory query that is not backed by a file.
     * Useful for dynamically generated or parameterised queries in tests.
     *
     * @param queryName Logical name.
     * @param sql       SQL content.
     */
    public static void registerQuery(String queryName, String sql) {
        queryCache.put(queryName, sql);
        logger.info("Registered in-memory query: '{}'", queryName);
    }

    /**
     * Clears the query cache.  Primarily useful in tests to force a reload.
     */
    public static void clearCache() {
        queryCache.clear();
        logger.debug("Query cache cleared.");
    }

    // -------------------------------------------------------------------------
    // Internal
    // -------------------------------------------------------------------------

    /**
     * Loads a query from disk or classpath.
     */
    private static String loadQuery(String queryName) {
        String queriesDir = ConfigReader.getProperty("queries.path",
                FrameworkConstants.DEFAULT_QUERIES_PATH);

        // 1. Try file system
        Path filePath = Paths.get(queriesDir, queryName + ".sql");
        if (Files.exists(filePath)) {
            try {
                String sql = Files.readString(filePath, StandardCharsets.UTF_8).trim();
                logger.info("Loaded query '{}' from file: {}", queryName, filePath);
                return sql;
            } catch (IOException e) {
                logger.warn("Failed to read query file: {}", filePath, e);
            }
        }

        // 2. Try classpath resource
        String resourcePath = "/queries/" + queryName + ".sql";
        try (InputStream is = QueryStore.class.getResourceAsStream(resourcePath)) {
            if (is != null) {
                String sql = new String(is.readAllBytes(), StandardCharsets.UTF_8).trim();
                logger.info("Loaded query '{}' from classpath: {}", queryName, resourcePath);
                return sql;
            }
        } catch (IOException e) {
            logger.warn("Failed to read query from classpath: {}", resourcePath, e);
        }

        throw new IllegalArgumentException(
                "Query not found: '" + queryName + "'. Searched: "
                        + filePath.toAbsolutePath() + " and classpath:" + resourcePath);
    }
}
