package com.orion.mapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orion.constants.FrameworkConstants;
import com.orion.utils.ConfigReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * TableMappingLoader — Loads {@link TableMappingConfig} instances from
 * JSON files in the mappings directory.
 *
 * <p>Mappings are cached after first load.  The directory is configurable
 * via the {@code mappings.path} property in {@code config.properties}
 * (default: {@code src/test/resources/mappings/}).
 *
 * <p>Usage:
 * <pre>{@code
 *   TableMappingConfig config = TableMappingLoader.getMapping("pivot_positive_bids");
 *   List<ColumnMapping> columns = config.getColumnMappings();
 * }</pre>
 */
public class TableMappingLoader {

    private static final Logger logger = LogManager.getLogger(TableMappingLoader.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /** Cached mapping configs: key = mapping name, value = parsed config. */
    private static final Map<String, TableMappingConfig> cache = new ConcurrentHashMap<>();

    private TableMappingLoader() {
        // Utility class — not instantiable
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Returns the {@link TableMappingConfig} for the given mapping name.
     *
     * <p>Resolution order:
     * <ol>
     *   <li>In-memory cache</li>
     *   <li>JSON file at {@code <mappings.path>/<mappingName>.json}</li>
     *   <li>Classpath resource at {@code /mappings/<mappingName>.json}</li>
     * </ol>
     *
     * @param mappingName Logical name (filename without {@code .json}).
     * @return Parsed TableMappingConfig.
     * @throws IllegalArgumentException if the mapping file cannot be found.
     */
    public static TableMappingConfig getMapping(String mappingName) {
        return cache.computeIfAbsent(mappingName, TableMappingLoader::loadMapping);
    }

    /**
     * Registers an in-memory mapping config that is not backed by a file.
     * Useful for programmatic / dynamically built mappings in tests.
     *
     * @param mappingName Logical name.
     * @param config      The mapping config object.
     */
    public static void registerMapping(String mappingName, TableMappingConfig config) {
        cache.put(mappingName, config);
        logger.info("Registered in-memory mapping: '{}'", mappingName);
    }

    /**
     * Clears the mapping cache.
     */
    public static void clearCache() {
        cache.clear();
        logger.debug("Mapping cache cleared.");
    }

    // -------------------------------------------------------------------------
    // Internal
    // -------------------------------------------------------------------------

    /**
     * Loads a mapping from disk or classpath.
     */
    private static TableMappingConfig loadMapping(String mappingName) {
        String mappingsDir = ConfigReader.getProperty("mappings.path",
                FrameworkConstants.DEFAULT_MAPPINGS_PATH);

        // 1. Try file system
        Path filePath = Paths.get(mappingsDir, mappingName + ".json");
        if (Files.exists(filePath)) {
            try {
                TableMappingConfig config = objectMapper.readValue(
                        filePath.toFile(), TableMappingConfig.class);
                logger.info("Loaded mapping '{}' from file: {}", mappingName, filePath);
                return config;
            } catch (IOException e) {
                logger.warn("Failed to read mapping file: {}", filePath, e);
            }
        }

        // 2. Try classpath resource
        String resourcePath = "/mappings/" + mappingName + ".json";
        try (InputStream is = TableMappingLoader.class.getResourceAsStream(resourcePath)) {
            if (is != null) {
                TableMappingConfig config = objectMapper.readValue(is, TableMappingConfig.class);
                logger.info("Loaded mapping '{}' from classpath: {}", mappingName, resourcePath);
                return config;
            }
        } catch (IOException e) {
            logger.warn("Failed to read mapping from classpath: {}", resourcePath, e);
        }

        throw new IllegalArgumentException(
                "Mapping not found: '" + mappingName + "'. Searched: "
                        + filePath.toAbsolutePath() + " and classpath:" + resourcePath);
    }
}
