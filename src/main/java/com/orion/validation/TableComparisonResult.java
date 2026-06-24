package com.orion.validation;

import com.orion.constants.FrameworkConstants;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * TableComparisonResult — Aggregate result of comparing an entire table's
 * UI data against its database data.
 *
 * <p>Contains metadata (table name, timestamps, row/column counts) and
 * a list of individual {@link ComparisonResult} entries for each cell.
 */
public class TableComparisonResult {

    private final String tableName;
    private final LocalDateTime timestamp;
    private final int uiRowCount;
    private final int dbRowCount;
    private final int uiColumnCount;
    private final int dbColumnCount;
    private final List<ComparisonResult> results;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Full constructor.
     *
     * @param tableName     Logical table name (from mapping config).
     * @param timestamp     When the comparison was executed.
     * @param uiRowCount    Number of rows captured from UI.
     * @param dbRowCount    Number of rows returned from DB.
     * @param uiColumnCount Number of columns captured from UI.
     * @param dbColumnCount Number of mapped columns in the DB query.
     * @param results       List of cell-level comparison results.
     */
    public TableComparisonResult(String tableName, LocalDateTime timestamp,
                                  int uiRowCount, int dbRowCount,
                                  int uiColumnCount, int dbColumnCount,
                                  List<ComparisonResult> results) {
        this.tableName = tableName;
        this.timestamp = timestamp;
        this.uiRowCount = uiRowCount;
        this.dbRowCount = dbRowCount;
        this.uiColumnCount = uiColumnCount;
        this.dbColumnCount = dbColumnCount;
        this.results = results != null ? new ArrayList<>(results) : new ArrayList<>();
    }

    // -------------------------------------------------------------------------
    // Computed metrics
    // -------------------------------------------------------------------------

    /** Total number of cell comparisons. */
    public int getTotalComparisons() {
        return results.size();
    }

    /** Number of PASS results. */
    public int getPassCount() {
        return (int) results.stream()
                .filter(r -> FrameworkConstants.STATUS_PASS.equals(r.getStatus()))
                .count();
    }

    /** Number of FAIL results. */
    public int getFailCount() {
        return (int) results.stream()
                .filter(r -> FrameworkConstants.STATUS_FAIL.equals(r.getStatus()))
                .count();
    }

    /** Number of MISSING results. */
    public int getMissingCount() {
        return (int) results.stream()
                .filter(r -> FrameworkConstants.STATUS_MISSING.equals(r.getStatus()))
                .count();
    }

    /** Number of SKIPPED results. */
    public int getSkippedCount() {
        return (int) results.stream()
                .filter(r -> FrameworkConstants.STATUS_SKIPPED.equals(r.getStatus()))
                .count();
    }

    /** Pass percentage (0.0 – 100.0). */
    public double getPassPercentage() {
        if (results.isEmpty()) return 100.0;
        return (getPassCount() * 100.0) / results.size();
    }

    /** Returns {@code true} if all comparisons passed (no FAILs or MISSINGs). */
    public boolean isAllPassed() {
        return getFailCount() == 0 && getMissingCount() == 0;
    }

    /** Returns only the FAIL results. */
    public List<ComparisonResult> getFailures() {
        return results.stream()
                .filter(ComparisonResult::isFailed)
                .toList();
    }

    /** Returns only the MISSING results. */
    public List<ComparisonResult> getMissing() {
        return results.stream()
                .filter(ComparisonResult::isMissing)
                .toList();
    }

    /** Whether the UI and DB row counts match. */
    public boolean isRowCountMatch() {
        return uiRowCount == dbRowCount;
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public String getTableName()        { return tableName; }
    public LocalDateTime getTimestamp()  { return timestamp; }
    public int getUiRowCount()          { return uiRowCount; }
    public int getDbRowCount()          { return dbRowCount; }
    public int getUiColumnCount()       { return uiColumnCount; }
    public int getDbColumnCount()       { return dbColumnCount; }

    /** Returns an unmodifiable view of all comparison results. */
    public List<ComparisonResult> getResults() {
        return Collections.unmodifiableList(results);
    }

    // -------------------------------------------------------------------------
    // Summary
    // -------------------------------------------------------------------------

    /**
     * Returns a human-readable summary suitable for logging.
     */
    public String toSummaryString() {
        return String.format(
                "TableComparisonResult[table='%s', uiRows=%d, dbRows=%d, "
                        + "total=%d, pass=%d, fail=%d, missing=%d, skipped=%d, passRate=%.1f%%]",
                tableName, uiRowCount, dbRowCount,
                getTotalComparisons(), getPassCount(), getFailCount(),
                getMissingCount(), getSkippedCount(), getPassPercentage());
    }

    @Override
    public String toString() {
        return toSummaryString();
    }
}
