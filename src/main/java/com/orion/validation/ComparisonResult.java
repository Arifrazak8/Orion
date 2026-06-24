package com.orion.validation;

import com.orion.constants.FrameworkConstants;

/**
 * ComparisonResult — Immutable POJO representing the comparison outcome
 * for a single cell (one UI value vs one DB value).
 *
 * <p>Status values:
 * <ul>
 *   <li>{@code PASS}    — UI and DB values match after normalisation</li>
 *   <li>{@code FAIL}    — UI and DB values differ</li>
 *   <li>{@code MISSING} — Value is present on one side but not the other</li>
 *   <li>{@code SKIPPED} — Comparison was intentionally skipped</li>
 * </ul>
 */
public class ComparisonResult {

    private final String rowIdentifier;
    private final String columnName;
    private final String uiValue;
    private final String dbValue;
    private final String status;
    private final String message;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Full constructor.
     *
     * @param rowIdentifier Row key used for matching (e.g. division name, bid name).
     * @param columnName    Column being compared.
     * @param uiValue       Value from the UI.
     * @param dbValue       Value from the database.
     * @param status        Comparison status (PASS/FAIL/MISSING/SKIPPED).
     * @param message       Optional human-readable message.
     */
    public ComparisonResult(String rowIdentifier, String columnName,
                             String uiValue, String dbValue,
                             String status, String message) {
        this.rowIdentifier = rowIdentifier;
        this.columnName = columnName;
        this.uiValue = uiValue;
        this.dbValue = dbValue;
        this.status = status;
        this.message = message;
    }

    // -------------------------------------------------------------------------
    // Factory methods
    // -------------------------------------------------------------------------

    /** Creates a PASS result. */
    public static ComparisonResult pass(String rowId, String column,
                                         String uiValue, String dbValue) {
        return new ComparisonResult(rowId, column, uiValue, dbValue,
                FrameworkConstants.STATUS_PASS, "Values match");
    }

    /** Creates a FAIL result. */
    public static ComparisonResult fail(String rowId, String column,
                                         String uiValue, String dbValue) {
        return new ComparisonResult(rowId, column, uiValue, dbValue,
                FrameworkConstants.STATUS_FAIL,
                "Mismatch — UI: '" + uiValue + "', DB: '" + dbValue + "'");
    }

    /** Creates a MISSING result when a row exists in DB but not on UI. */
    public static ComparisonResult missingOnUi(String rowId, String column, String dbValue) {
        return new ComparisonResult(rowId, column, null, dbValue,
                FrameworkConstants.STATUS_MISSING,
                "Row/column present in DB but missing on UI");
    }

    /** Creates a MISSING result when a row exists on UI but not in DB. */
    public static ComparisonResult missingInDb(String rowId, String column, String uiValue) {
        return new ComparisonResult(rowId, column, uiValue, null,
                FrameworkConstants.STATUS_MISSING,
                "Row/column present on UI but missing in DB");
    }

    /** Creates a SKIPPED result. */
    public static ComparisonResult skipped(String rowId, String column, String reason) {
        return new ComparisonResult(rowId, column, null, null,
                FrameworkConstants.STATUS_SKIPPED, reason);
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public String getRowIdentifier() { return rowIdentifier; }
    public String getColumnName()    { return columnName; }
    public String getUiValue()       { return uiValue; }
    public String getDbValue()       { return dbValue; }
    public String getStatus()        { return status; }
    public String getMessage()       { return message; }

    public boolean isPassed()  { return FrameworkConstants.STATUS_PASS.equals(status); }
    public boolean isFailed()  { return FrameworkConstants.STATUS_FAIL.equals(status); }
    public boolean isMissing() { return FrameworkConstants.STATUS_MISSING.equals(status); }

    @Override
    public String toString() {
        return String.format("ComparisonResult{row='%s', col='%s', ui='%s', db='%s', status=%s}",
                rowIdentifier, columnName,
                uiValue != null ? uiValue : "N/A",
                dbValue != null ? dbValue : "N/A",
                status);
    }
}
