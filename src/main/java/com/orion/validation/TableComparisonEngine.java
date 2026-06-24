package com.orion.validation;

import com.orion.mapping.ColumnMapping;
import com.orion.mapping.TableMappingConfig;
import com.orion.utils.ValueNormalizer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * TableComparisonEngine — Generic, reusable engine that compares UI table
 * data against database query results cell-by-cell.
 *
 * <p>This is the <b>core deliverable</b> of the enterprise framework.
 * It accepts:
 * <ul>
 *   <li>UI data — a {@code Map<rowKey, Map<colName, cellValue>>} as returned
 *       by {@link com.orion.utils.TableUtils#getTableData()}</li>
 *   <li>DB data — a {@code List<Map<colName, cellValue>>} as returned by
 *       {@link com.orion.database.QueryExecutor#executeQuery(String, String...)}</li>
 *   <li>Mapping config — a {@link TableMappingConfig} that maps UI columns
 *       to DB columns and specifies normalisation strategies</li>
 * </ul>
 *
 * <p>And produces a {@link TableComparisonResult} with cell-by-cell PASS/FAIL
 * outcomes, row count comparison, and aggregate statistics.
 *
 * <p>The engine is <b>stateless</b> and <b>thread-safe</b> — all state is
 * passed in via method parameters.
 *
 * <p>Usage:
 * <pre>{@code
 *   TableComparisonEngine engine = new TableComparisonEngine();
 *   TableMappingConfig mapping = TableMappingLoader.getMapping("pivot_positive_bids");
 *
 *   Map<String, Map<String, String>> uiData = tableUtils.getTableData();
 *   List<Map<String, String>> dbData = queryExecutor.executeQuery(sql);
 *
 *   TableComparisonResult result = engine.compare(mapping, uiData, dbData);
 *   logger.info(result.toSummaryString());
 * }</pre>
 */
public class TableComparisonEngine {

    private static final Logger logger = LogManager.getLogger(TableComparisonEngine.class);

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Compares UI table data against DB data using the provided mapping config.
     *
     * @param mapping  Table mapping configuration.
     * @param uiData   UI data: {@code Map<rowKey, Map<uiColName, cellValue>>}.
     * @param dbData   DB data: {@code List<Map<dbColName, cellValue>>}.
     * @return Aggregate comparison result.
     */
    public TableComparisonResult compare(TableMappingConfig mapping,
                                          Map<String, Map<String, String>> uiData,
                                          List<Map<String, String>> dbData) {

        logger.info("Starting comparison for table: '{}'", mapping.getName());
        logger.info("UI rows: {}, DB rows: {}", uiData.size(), dbData.size());

        List<ColumnMapping> columnMappings = mapping.getColumnMappings();
        String rowIdDbColumn = mapping.getRowIdentifierDbColumn();

        // Index DB rows by their row-identifier column for O(1) lookup
        Map<String, Map<String, String>> dbIndex = indexDbRows(dbData, rowIdDbColumn);

        List<ComparisonResult> results = new ArrayList<>();

        // 1. For each UI row, find the matching DB row and compare columns
        for (Map.Entry<String, Map<String, String>> uiEntry : uiData.entrySet()) {
            String uiRowKey = uiEntry.getKey();
            Map<String, String> uiRow = uiEntry.getValue();

            // Find matching DB row
            Map<String, String> dbRow = findMatchingDbRow(uiRowKey, dbIndex);

            if (dbRow == null) {
                // UI row has no matching DB row
                logger.warn("UI row '{}' has no matching DB row.", uiRowKey);
                for (ColumnMapping cm : columnMappings) {
                    String uiVal = uiRow.getOrDefault(cm.getUiColumn(), "");
                    results.add(ComparisonResult.missingInDb(uiRowKey, cm.getUiColumn(), uiVal));
                }
                continue;
            }

            // Compare each mapped column
            for (ColumnMapping cm : columnMappings) {
                String uiVal = uiRow.getOrDefault(cm.getUiColumn(), "");
                String dbVal = dbRow.getOrDefault(cm.getDbColumn(), "");

                boolean match = compareValues(uiVal, dbVal, cm.getValueTransformer());

                if (match) {
                    results.add(ComparisonResult.pass(uiRowKey, cm.getUiColumn(), uiVal, dbVal));
                } else {
                    results.add(ComparisonResult.fail(uiRowKey, cm.getUiColumn(), uiVal, dbVal));
                    logger.debug("FAIL: row='{}', col='{}', ui='{}', db='{}'",
                            uiRowKey, cm.getUiColumn(), uiVal, dbVal);
                }
            }
        }

        // 2. Find DB rows that don't have a matching UI row
        Set<String> uiKeys = uiData.keySet().stream()
                .map(ValueNormalizer::normalizeForComparison)
                .collect(Collectors.toSet());

        for (Map.Entry<String, Map<String, String>> dbEntry : dbIndex.entrySet()) {
            String dbRowKey = dbEntry.getKey();
            String normalizedDbKey = ValueNormalizer.normalizeForComparison(dbRowKey);

            if (!uiKeys.contains(normalizedDbKey)) {
                logger.warn("DB row '{}' has no matching UI row.", dbRowKey);
                Map<String, String> dbRow = dbEntry.getValue();
                for (ColumnMapping cm : columnMappings) {
                    String dbVal = dbRow.getOrDefault(cm.getDbColumn(), "");
                    results.add(ComparisonResult.missingOnUi(dbRowKey, cm.getUiColumn(), dbVal));
                }
            }
        }

        // Build result
        int uiColCount = columnMappings.size();
        int dbColCount = columnMappings.size(); // mapped columns only

        TableComparisonResult result = new TableComparisonResult(
                mapping.getName(),
                LocalDateTime.now(),
                uiData.size(),
                dbData.size(),
                uiColCount,
                dbColCount,
                results
        );

        logger.info("Comparison complete: {}", result.toSummaryString());
        return result;
    }

    /**
     * Simplified comparison that does not require a full mapping config.
     * Compares two datasets by assuming column names are identical.
     *
     * @param tableName      Logical table name for reporting.
     * @param uiData         UI data map.
     * @param dbData         DB data list.
     * @param rowIdDbColumn  DB column used as the row key.
     * @return Comparison result.
     */
    public TableComparisonResult compareDirectMatch(
            String tableName,
            Map<String, Map<String, String>> uiData,
            List<Map<String, String>> dbData,
            String rowIdDbColumn) {

        logger.info("Starting direct-match comparison for table: '{}'", tableName);

        Map<String, Map<String, String>> dbIndex = indexDbRows(dbData, rowIdDbColumn);
        List<ComparisonResult> results = new ArrayList<>();

        for (Map.Entry<String, Map<String, String>> uiEntry : uiData.entrySet()) {
            String uiRowKey = uiEntry.getKey();
            Map<String, String> uiRow = uiEntry.getValue();

            Map<String, String> dbRow = findMatchingDbRow(uiRowKey, dbIndex);

            if (dbRow == null) {
                for (String col : uiRow.keySet()) {
                    results.add(ComparisonResult.missingInDb(uiRowKey, col, uiRow.get(col)));
                }
                continue;
            }

            for (Map.Entry<String, String> colEntry : uiRow.entrySet()) {
                String col = colEntry.getKey();
                String uiVal = colEntry.getValue();
                String dbVal = dbRow.getOrDefault(col, "");

                if (ValueNormalizer.textEquals(uiVal, dbVal)) {
                    results.add(ComparisonResult.pass(uiRowKey, col, uiVal, dbVal));
                } else {
                    results.add(ComparisonResult.fail(uiRowKey, col, uiVal, dbVal));
                }
            }
        }

        return new TableComparisonResult(
                tableName, LocalDateTime.now(),
                uiData.size(), dbData.size(),
                uiData.isEmpty() ? 0 : uiData.values().iterator().next().size(),
                dbData.isEmpty() ? 0 : dbData.get(0).size(),
                results
        );
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /**
     * Indexes DB rows by the row-identifier column value.
     */
    private Map<String, Map<String, String>> indexDbRows(
            List<Map<String, String>> dbData, String rowIdColumn) {
        Map<String, Map<String, String>> index = new LinkedHashMap<>();
        for (Map<String, String> row : dbData) {
            String key = row.getOrDefault(rowIdColumn, "");
            if (!key.isEmpty()) {
                index.put(key, row);
            }
        }
        return index;
    }

    /**
     * Finds a matching DB row for a given UI row key using normalised comparison.
     */
    private Map<String, String> findMatchingDbRow(
            String uiRowKey, Map<String, Map<String, String>> dbIndex) {
        // Exact match first
        if (dbIndex.containsKey(uiRowKey)) {
            return dbIndex.get(uiRowKey);
        }
        // Normalised match
        String normalizedUiKey = ValueNormalizer.normalizeForComparison(uiRowKey);
        for (Map.Entry<String, Map<String, String>> entry : dbIndex.entrySet()) {
            if (ValueNormalizer.normalizeForComparison(entry.getKey()).equals(normalizedUiKey)) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * Compares two values using the specified transformation strategy.
     *
     * @param uiValue     UI value.
     * @param dbValue     DB value.
     * @param transformer Transformation type: "currency", "numeric", "text", "text_ignore_case".
     * @return {@code true} if the values are considered equal.
     */
    private boolean compareValues(String uiValue, String dbValue, String transformer) {
        if (transformer == null) {
            transformer = "text";
        }

        return switch (transformer.toLowerCase()) {
            case "currency", "numeric" -> ValueNormalizer.numericEquals(uiValue, dbValue);
            case "decimal", "decimal2" -> ValueNormalizer.numericEquals(uiValue, dbValue, 2);
            case "text_ignore_case"    -> ValueNormalizer.textEqualsIgnoreCase(uiValue, dbValue);
            default                    -> ValueNormalizer.textEquals(uiValue, dbValue);
        };
    }
}
