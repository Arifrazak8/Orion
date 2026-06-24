package com.orion.mapping;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * ColumnMapping — POJO representing the mapping between a single UI column
 * name and its corresponding database column name.
 *
 * <p>Optionally includes a {@code valueTransformer} hint that tells the
 * comparison engine how to normalise values before comparing
 * (e.g. "currency", "date", "text", "percentage").
 *
 * <p>Example JSON:
 * <pre>{@code
 *   {
 *     "uiColumn": "Job Amount",
 *     "dbColumn": "job_amount",
 *     "valueTransformer": "currency"
 *   }
 * }</pre>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ColumnMapping {

    @JsonProperty("uiColumn")
    private String uiColumn;

    @JsonProperty("dbColumn")
    private String dbColumn;

    @JsonProperty("valueTransformer")
    private String valueTransformer;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /** Default constructor required by Jackson. */
    public ColumnMapping() {
    }

    /**
     * Full constructor.
     *
     * @param uiColumn         Column header as shown on the UI.
     * @param dbColumn         Corresponding column name / alias in the DB query.
     * @param valueTransformer Optional normalisation hint ("currency", "date", "text", etc.).
     */
    public ColumnMapping(String uiColumn, String dbColumn, String valueTransformer) {
        this.uiColumn = uiColumn;
        this.dbColumn = dbColumn;
        this.valueTransformer = valueTransformer;
    }

    /**
     * Convenience constructor without transformer (defaults to text comparison).
     */
    public ColumnMapping(String uiColumn, String dbColumn) {
        this(uiColumn, dbColumn, "text");
    }

    // -------------------------------------------------------------------------
    // Getters and setters
    // -------------------------------------------------------------------------

    public String getUiColumn() {
        return uiColumn;
    }

    public void setUiColumn(String uiColumn) {
        this.uiColumn = uiColumn;
    }

    public String getDbColumn() {
        return dbColumn;
    }

    public void setDbColumn(String dbColumn) {
        this.dbColumn = dbColumn;
    }

    public String getValueTransformer() {
        return valueTransformer != null ? valueTransformer : "text";
    }

    public void setValueTransformer(String valueTransformer) {
        this.valueTransformer = valueTransformer;
    }

    @Override
    public String toString() {
        return "ColumnMapping{" +
                "uiColumn='" + uiColumn + '\'' +
                ", dbColumn='" + dbColumn + '\'' +
                ", valueTransformer='" + getValueTransformer() + '\'' +
                '}';
    }
}
