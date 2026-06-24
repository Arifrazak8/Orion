package com.orion.reporting;

import com.orion.constants.FrameworkConstants;
import com.orion.utils.ConfigReader;
import com.orion.validation.ComparisonResult;
import com.orion.validation.TableComparisonResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * ExcelReportGenerator — Generates professional, styled Excel comparison
 * reports from {@link TableComparisonResult} data.
 *
 * <p>Each report includes:
 * <ul>
 *   <li><b>Summary sheet</b>: Execution info, timestamps, pass/fail statistics</li>
 *   <li><b>Per-table sheet</b>: Row-by-row comparison with Row | Column | UI Value |
 *       DB Value | Status columns, conditional formatting, auto-filters</li>
 * </ul>
 *
 * <p>Reports are timestamped and stored in the configured Excel report directory
 * (default: {@code ./reports/comparison/}).
 */
public class ExcelReportGenerator {

    private static final Logger logger = LogManager.getLogger(ExcelReportGenerator.class);

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Generates an Excel report for a single table comparison.
     *
     * @param result The comparison result.
     * @return Absolute path to the generated report file.
     */
    public String generateReport(TableComparisonResult result) {
        return generateReport(List.of(result));
    }

    /**
     * Generates an Excel report containing multiple table comparison sheets.
     *
     * @param results List of comparison results (one sheet per result).
     * @return Absolute path to the generated report file.
     */
    public String generateReport(List<TableComparisonResult> results) {
        String outputDir = ConfigReader.getProperty("report.excel.path",
                FrameworkConstants.DEFAULT_EXCEL_REPORT_PATH);
        String timestamp = new SimpleDateFormat(FrameworkConstants.TIMESTAMP_FORMAT).format(new Date());
        String fileName = "Comparison_Report_" + timestamp + ".xlsx";

        File dir = new File(outputDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File reportFile = new File(dir, fileName);

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            // Create cell styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle passStyle = createPassStyle(workbook);
            CellStyle failStyle = createFailStyle(workbook);
            CellStyle missingStyle = createMissingStyle(workbook);
            CellStyle defaultStyle = createDefaultStyle(workbook);
            CellStyle summaryLabelStyle = createSummaryLabelStyle(workbook);
            CellStyle summaryValueStyle = createSummaryValueStyle(workbook);

            // Summary sheet
            createSummarySheet(workbook, results, headerStyle, summaryLabelStyle, summaryValueStyle,
                    passStyle, failStyle);

            // Per-table comparison sheets
            for (TableComparisonResult result : results) {
                createComparisonSheet(workbook, result, headerStyle,
                        passStyle, failStyle, missingStyle, defaultStyle);
            }

            // Write to file
            try (FileOutputStream fos = new FileOutputStream(reportFile)) {
                workbook.write(fos);
            }

            String absolutePath = reportFile.getAbsolutePath();
            logger.info("Excel comparison report generated: {}", absolutePath);
            return absolutePath;

        } catch (IOException e) {
            logger.error("Failed to generate Excel report: {}", e.getMessage(), e);
            return null;
        }
    }

    // -------------------------------------------------------------------------
    // Sheet creation
    // -------------------------------------------------------------------------

    /**
     * Creates the Execution Summary sheet.
     */
    private void createSummarySheet(Workbook workbook, List<TableComparisonResult> results,
                                     CellStyle headerStyle, CellStyle labelStyle,
                                     CellStyle valueStyle, CellStyle passStyle,
                                     CellStyle failStyle) {
        Sheet sheet = workbook.createSheet("Execution Summary");

        int rowIndex = 0;

        // Title
        Row titleRow = sheet.createRow(rowIndex++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Orion CRM — Table Comparison Report");
        titleCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));
        rowIndex++; // blank row

        // Execution metadata
        rowIndex = addSummaryRow(sheet, rowIndex, labelStyle, valueStyle,
                "Report Generated", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        rowIndex = addSummaryRow(sheet, rowIndex, labelStyle, valueStyle,
                "Environment", ConfigReader.getProperty("url", "N/A"));
        rowIndex = addSummaryRow(sheet, rowIndex, labelStyle, valueStyle,
                "Executed By", System.getProperty("user.name"));
        rowIndex = addSummaryRow(sheet, rowIndex, labelStyle, valueStyle,
                "Total Tables Compared", String.valueOf(results.size()));
        rowIndex++; // blank row

        // Per-table summary table header
        Row tableHeaderRow = sheet.createRow(rowIndex++);
        String[] headers = {"Table Name", "UI Rows", "DB Rows", "Total Checks",
                "Passed", "Failed", "Missing", "Pass Rate"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = tableHeaderRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Per-table summary rows
        for (TableComparisonResult result : results) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(result.getTableName());
            row.createCell(1).setCellValue(result.getUiRowCount());
            row.createCell(2).setCellValue(result.getDbRowCount());
            row.createCell(3).setCellValue(result.getTotalComparisons());

            Cell passCell = row.createCell(4);
            passCell.setCellValue(result.getPassCount());
            passCell.setCellStyle(passStyle);

            Cell failCell = row.createCell(5);
            failCell.setCellValue(result.getFailCount());
            if (result.getFailCount() > 0) {
                failCell.setCellStyle(failStyle);
            }

            row.createCell(6).setCellValue(result.getMissingCount());
            row.createCell(7).setCellValue(String.format("%.1f%%", result.getPassPercentage()));
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * Creates a comparison sheet for a single table.
     */
    private void createComparisonSheet(Workbook workbook, TableComparisonResult result,
                                        CellStyle headerStyle, CellStyle passStyle,
                                        CellStyle failStyle, CellStyle missingStyle,
                                        CellStyle defaultStyle) {
        // Sheet name — truncate to 31 chars (Excel limit)
        String sheetName = result.getTableName();
        if (sheetName.length() > 31) {
            sheetName = sheetName.substring(0, 31);
        }

        Sheet sheet = workbook.createSheet(sheetName);

        // Header row
        Row headerRow = sheet.createRow(0);
        String[] columns = {"#", "Row", "Column", "UI Value", "DB Value", "Status", "Message"};
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }

        // Freeze the header row
        sheet.createFreezePane(0, 1);

        // Data rows
        List<ComparisonResult> comparisons = result.getResults();
        for (int i = 0; i < comparisons.size(); i++) {
            ComparisonResult cr = comparisons.get(i);
            Row row = sheet.createRow(i + 1);

            row.createCell(0).setCellValue(i + 1);
            row.createCell(1).setCellValue(cr.getRowIdentifier() != null ? cr.getRowIdentifier() : "");
            row.createCell(2).setCellValue(cr.getColumnName() != null ? cr.getColumnName() : "");
            row.createCell(3).setCellValue(cr.getUiValue() != null ? cr.getUiValue() : "N/A");
            row.createCell(4).setCellValue(cr.getDbValue() != null ? cr.getDbValue() : "N/A");

            Cell statusCell = row.createCell(5);
            statusCell.setCellValue(cr.getStatus());

            Cell messageCell = row.createCell(6);
            messageCell.setCellValue(cr.getMessage() != null ? cr.getMessage() : "");

            // Apply conditional styling
            CellStyle rowStyle = switch (cr.getStatus()) {
                case "PASS" -> passStyle;
                case "FAIL" -> failStyle;
                case "MISSING" -> missingStyle;
                default -> defaultStyle;
            };

            statusCell.setCellStyle(rowStyle);
        }

        // Enable auto-filter on the header row
        if (!comparisons.isEmpty()) {
            sheet.setAutoFilter(new CellRangeAddress(0, comparisons.size(), 0, columns.length - 1));
        }

        // Auto-size columns
        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    // -------------------------------------------------------------------------
    // Helper — summary row
    // -------------------------------------------------------------------------

    private int addSummaryRow(Sheet sheet, int rowIndex, CellStyle labelStyle,
                               CellStyle valueStyle, String label, String value) {
        Row row = sheet.createRow(rowIndex);
        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(labelStyle);

        Cell valueCell = row.createCell(1);
        valueCell.setCellValue(value);
        valueCell.setCellStyle(valueStyle);
        return rowIndex + 1;
    }

    // -------------------------------------------------------------------------
    // Cell styles
    // -------------------------------------------------------------------------

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createPassStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.DARK_GREEN.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createFailStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.RED.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createMissingStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.DARK_YELLOW.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createDefaultStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createSummaryLabelStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        return style;
    }

    private CellStyle createSummaryValueStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        return style;
    }
}
