package com.orion.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Generic utility for dynamic interactions with HTML Tables.
 * Requires the table WebElement to be passed in.
 */
public class TableUtils {

    private WebDriver driver;
    private WebElement tableElement;

    public TableUtils(WebDriver driver, WebElement tableElement) {
        this.driver = driver;
        this.tableElement = tableElement;
    }

    /**
     * Executes the given action, retrying up to 3 times if a StaleElementReferenceException is caught.
     */
    private <T> T executeWithRetry(java.util.function.Supplier<T> action) {
        StaleElementReferenceException lastException = null;
        for (int i = 0; i < 3; i++) {
            try {
                return action.get();
            } catch (StaleElementReferenceException e) {
                lastException = e;
                try { Thread.sleep(500); } catch (InterruptedException ex) {}
            }
        }
        throw lastException != null ? lastException : new StaleElementReferenceException("Stale element reference on Table operation");
    }

    /**
     * Dynamically finds the 1-based index of a column by matching the header text.
     */
    private int getColumnIndex(String columnName) {
        return executeWithRetry(() -> {
            List<WebElement> headers = tableElement.findElements(By.xpath(".//thead//th | .//thead//td"));
            for (int i = 0; i < headers.size(); i++) {
                if (headers.get(i).getText().trim().equalsIgnoreCase(columnName.trim())) {
                    return i + 1; // XPath is 1-indexed
                }
            }
            throw new IllegalArgumentException("Column not found: " + columnName);
        });
    }

    /**
     * Locates a specific cell by row name (matched against the 1st column) and
     * column name.
     */
    private WebElement getCell(String rowName, String columnName) {
        return executeWithRetry(() -> {
            int colIndex = getColumnIndex(columnName);
            String xpath = String.format(".//tbody/tr[td[normalize-space(.)='%s']]/td[%d]", rowName, colIndex);
            try {
                return tableElement.findElement(By.xpath(xpath));
            } catch (org.openqa.selenium.NoSuchElementException e) {
                throw new RuntimeException("Cell not found for row: '" + rowName + "' and column: '" + columnName + "'");
            }
        });
    }

    /**
     * Gets the text value of a specific cell dynamically.
     */
    public String getCellValue(String rowName, String columnName) {
        return executeWithRetry(() -> getCell(rowName, columnName).getText().trim());
    }

    /**
     * Retrieves all data for a specific row as a Map of Column Name -> Cell Value.
     */
    public Map<String, String> getRowData(String rowName) {
        return executeWithRetry(() -> {
            List<WebElement> headers = tableElement.findElements(By.xpath(".//thead//th | .//thead//td"));
            Map<String, String> rowData = new LinkedHashMap<>();

            for (int i = 0; i < headers.size(); i++) {
                String colName = headers.get(i).getText().trim();
                if (!colName.isEmpty()) {
                    String cellXpath = String.format(".//tbody/tr[td[normalize-space(.)='%s']]/td[%d]", rowName, i + 1);
                    try {
                        String val = tableElement.findElement(By.xpath(cellXpath)).getText().trim();
                        rowData.put(colName, val);
                    } catch (org.openqa.selenium.NoSuchElementException e) {
                        rowData.put(colName, ""); // Handle missing td gracefully
                    }
                }
            }
            return rowData;
        });
    }

    /**
     * Gets all text values from a single column across all rows in the tbody.
     */
    public List<String> getColumnValues(String columnName) {
        return executeWithRetry(() -> {
            int colIndex = getColumnIndex(columnName);
            List<WebElement> cells = tableElement.findElements(By.xpath(".//tbody/tr/td[" + colIndex + "]"));
            return cells.stream()
                    .map(cell -> cell.getText().trim())
                    .collect(Collectors.toList());
        });
    }

    /**
     * Gets all column header names from the table's thead.
     *
     * @return List of header text values in display order.
     */
    public List<String> getHeaders() {
        return executeWithRetry(() -> {
            List<WebElement> headers = tableElement.findElements(By.xpath(".//thead//th | .//thead//td"));
            return headers.stream()
                    .map(h -> h.getText().trim())
                    .filter(text -> !text.isEmpty())
                    .collect(Collectors.toList());
        });
    }

    /**
     * Returns the number of data rows in the table body.
     *
     * @return Row count (excluding header and footer rows).
     */
    public int getRowCount() {
        return executeWithRetry(() -> {
            List<WebElement> rows = tableElement.findElements(By.xpath(".//tbody/tr"));
            return rows.size();
        });
    }

    /**
     * Reads footer row data as a Map of ColumnName → CellValue.
     * Looks for rows in tfoot, or falls back to tbody rows with
     * common footer identifiers.
     *
     * @return Map of column names to footer values, or empty map if no footer.
     */
    public Map<String, String> getFooterData() {
        return executeWithRetry(() -> {
            Map<String, String> footerData = new LinkedHashMap<>();
            List<WebElement> headers = tableElement.findElements(By.xpath(".//thead//th | .//thead//td"));

            // Try tfoot first
            List<WebElement> footerCells = tableElement.findElements(By.xpath(".//tfoot//td | .//tfoot//th"));
            if (footerCells.isEmpty()) {
                // Fallback: look for the last tbody row
                List<WebElement> bodyRows = tableElement.findElements(By.xpath(".//tbody/tr"));
                if (!bodyRows.isEmpty()) {
                    WebElement lastRow = bodyRows.get(bodyRows.size() - 1);
                    footerCells = lastRow.findElements(By.xpath("./td | ./th"));
                }
            }

            for (int i = 0; i < Math.min(headers.size(), footerCells.size()); i++) {
                String colName = headers.get(i).getText().trim();
                String cellVal = footerCells.get(i).getText().trim();
                if (!colName.isEmpty()) {
                    footerData.put(colName, cellVal);
                }
            }
            return footerData;
        });
    }

    /**
     * Specific helper to fetch a value from the Grand Total row.
     */
    public String getGrandTotalValue(String columnName) {
        return getCellValue("Grand Total", columnName);
    }

    /**
     * Validates a single cell's text.
     */
    public boolean verifyCellValue(String rowName, String columnName, String expectedValue) {
        String actualValue = getCellValue(rowName, columnName);
        return actualValue.equals(expectedValue);
    }

    /**
     * Reads the entire table body into a nested Map mapping RowName -> (ColumnName
     * -> CellValue).
     */
    public Map<String, Map<String, String>> getTableData() {
        return executeWithRetry(() -> {
            Map<String, Map<String, String>> tableData = new LinkedHashMap<>();
            List<WebElement> rows = tableElement.findElements(By.xpath(".//tbody/tr"));
            for (WebElement row : rows) {
                String rowName = row.findElement(By.xpath("./td[1]")).getText().trim();
                if (!rowName.isEmpty()) {
                    tableData.put(rowName, getRowData(rowName));
                }
            }
            return tableData;
        });
    }

    /**
     * Validates multiple cells across the table by comparing against expected map.
     */
    public void validateTableData(Map<String, Map<String, String>> expectedData) {
        for (Map.Entry<String, Map<String, String>> rowEntry : expectedData.entrySet()) {
            String rowName = rowEntry.getKey();
            for (Map.Entry<String, String> colEntry : rowEntry.getValue().entrySet()) {
                String columnName = colEntry.getKey();
                String expectedValue = colEntry.getValue();

                String actualValue = getCellValue(rowName, columnName);
                if (!actualValue.equals(expectedValue)) {
                    throw new AssertionError(String.format(
                            "Data mismatch at Row '%s' Col '%s'. Expected: '%s', Actual: '%s'",
                            rowName, columnName, expectedValue, actualValue));
                }
            }
        }
    }
}
