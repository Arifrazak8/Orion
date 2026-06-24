package com.orion.mapping;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * TableMappingConfig — POJO representing the full mapping configuration
 * for a single table/page comparison.
 *
 * <p>Loaded from a JSON file in {@code src/test/resources/mappings/}.
 *
 * <p>Example JSON:
 * <pre>{@code
 * {
 *   "name": "pivot_positive_bids",
 *   "page": "Pivot Report — Positive Bids",
 *   "tableIdentifier": "positive-table",
 *   "queryName": "pivot_positive_total",
 *   "rowIdentifierUiColumn": "Bid Name",
 *   "rowIdentifierDbColumn": "bid_name",
 *   "columnMappings": [
 *     { "uiColumn": "Job Amount", "dbColumn": "job_amount", "valueTransformer": "currency" },
 *     { "uiColumn": "Status",     "dbColumn": "status",     "valueTransformer": "text" }
 *   ]
 * }
 * }</pre>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TableMappingConfig {

    /** Unique logical name for this mapping (also used as the JSON filename). */
    @JsonProperty("name")
    private String name;

    /** Human-readable page / section name (for reports). */
    @JsonProperty("page")
    private String page;

    /** Identifier for the table on the UI (CSS id, xpath hint, etc.). */
    @JsonProperty("tableIdentifier")
    private String tableIdentifier;

    /** Name of the SQL query in {@link com.orion.database.QueryStore}. */
    @JsonProperty("queryName")
    private String queryName;

    /**
     * The UI column whose values act as the "row key" for matching UI rows
     * to DB rows (e.g. "Bid Name", "Division").
     */
    @JsonProperty("rowIdentifierUiColumn")
    private String rowIdentifierUiColumn;

    /**
     * The DB column whose values act as the "row key" for matching DB rows
     * to UI rows (e.g. "bid_name", "division_name").
     */
    @JsonProperty("rowIdentifierDbColumn")
    private String rowIdentifierDbColumn;

    /** List of column-level mappings (UI ↔ DB). */
    @JsonProperty("columnMappings")
    private List<ColumnMapping> columnMappings;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /** Default constructor required by Jackson. */
    public TableMappingConfig() {
    }

    // -------------------------------------------------------------------------
    // Getters and setters
    // -------------------------------------------------------------------------

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public String getTableIdentifier() {
        return tableIdentifier;
    }

    public void setTableIdentifier(String tableIdentifier) {
        this.tableIdentifier = tableIdentifier;
    }

    public String getQueryName() {
        return queryName;
    }

    public void setQueryName(String queryName) {
        this.queryName = queryName;
    }

    public String getRowIdentifierUiColumn() {
        return rowIdentifierUiColumn;
    }

    public void setRowIdentifierUiColumn(String rowIdentifierUiColumn) {
        this.rowIdentifierUiColumn = rowIdentifierUiColumn;
    }

    public String getRowIdentifierDbColumn() {
        return rowIdentifierDbColumn;
    }

    public void setRowIdentifierDbColumn(String rowIdentifierDbColumn) {
        this.rowIdentifierDbColumn = rowIdentifierDbColumn;
    }

    public List<ColumnMapping> getColumnMappings() {
        return columnMappings;
    }

    public void setColumnMappings(List<ColumnMapping> columnMappings) {
        this.columnMappings = columnMappings;
    }

    @Override
    public String toString() {
        return "TableMappingConfig{" +
                "name='" + name + '\'' +
                ", page='" + page + '\'' +
                ", queryName='" + queryName + '\'' +
                ", columnMappings=" + (columnMappings != null ? columnMappings.size() : 0) +
                '}';
    }
}
