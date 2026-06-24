package com.orion.reporting;

import com.orion.utils.ExtentReportManager;
import com.orion.validation.ComparisonResult;
import com.orion.validation.TableComparisonResult;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * ReportingService — Unified facade that triggers both HTML (Extent) and
 * Excel report generation from a single {@code reportComparison()} call.
 *
 * <p>Accumulates results across multiple tables so a final multi-table
 * Excel report can be generated at the end of the test suite.
 */
public class ReportingService {

    private static final Logger logger = LogManager.getLogger(ReportingService.class);

    private final ExcelReportGenerator excelGenerator;
    private final List<TableComparisonResult> accumulatedResults;

    public ReportingService() {
        this.excelGenerator = new ExcelReportGenerator();
        this.accumulatedResults = new ArrayList<>();
    }

    public ReportingService(ExcelReportGenerator excelGenerator) {
        this.excelGenerator = excelGenerator;
        this.accumulatedResults = new ArrayList<>();
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Reports a single table comparison result.
     *
     * <ol>
     *   <li>Embeds an HTML comparison table into the active Extent report test node</li>
     *   <li>Accumulates the result for later Excel generation</li>
     * </ol>
     *
     * @param result The table comparison result to report.
     */
    public void reportComparison(TableComparisonResult result) {
        // 1. Embed in Extent HTML report
        try {
            embedInExtentReport(result);
        } catch (Exception e) {
            logger.warn("Could not embed comparison in Extent report: {}", e.getMessage());
        }

        // 2. Accumulate for Excel
        accumulatedResults.add(result);
        logger.info("Comparison result accumulated for: '{}'", result.getTableName());
    }

    /**
     * Generates the consolidated Excel report for all accumulated results.
     * Typically called in {@code @AfterSuite} or at the end of the test run.
     *
     * @return Path to the generated Excel file, or null if no results accumulated.
     */
    public String generateConsolidatedExcelReport() {
        if (accumulatedResults.isEmpty()) {
            logger.info("No comparison results to report.");
            return null;
        }
        logger.info("Generating consolidated Excel report for {} table(s)...",
                accumulatedResults.size());
        return excelGenerator.generateReport(accumulatedResults);
    }

    /**
     * Generates an immediate Excel report for a single result
     * (useful when you want per-test Excel output).
     *
     * @param result The comparison result.
     * @return Path to the generated Excel file.
     */
    public String generateImmediateExcelReport(TableComparisonResult result) {
        return excelGenerator.generateReport(result);
    }

    /**
     * Returns all accumulated results.
     */
    public List<TableComparisonResult> getAccumulatedResults() {
        return accumulatedResults;
    }

    /**
     * Clears accumulated results (e.g. between test suites).
     */
    public void clearAccumulatedResults() {
        accumulatedResults.clear();
    }

    // -------------------------------------------------------------------------
    // Extent Report HTML embedding
    // -------------------------------------------------------------------------

    /**
     * Embeds a colour-coded HTML comparison table into the current Extent
     * test node.
     */
    private void embedInExtentReport(TableComparisonResult result) {
        var test = ExtentReportManager.getTest();
        if (test == null) {
            logger.debug("No active Extent test — skipping HTML embedding.");
            return;
        }

        StringBuilder html = new StringBuilder();
        html.append("<h5>Table Comparison: ").append(result.getTableName()).append("</h5>");

        // Summary line
        html.append("<p><b>UI Rows:</b> ").append(result.getUiRowCount())
                .append(" | <b>DB Rows:</b> ").append(result.getDbRowCount())
                .append(" | <b>Pass:</b> ").append(result.getPassCount())
                .append(" | <b>Fail:</b> ").append(result.getFailCount())
                .append(" | <b>Missing:</b> ").append(result.getMissingCount())
                .append(" | <b>Pass Rate:</b> ")
                .append(String.format("%.1f%%", result.getPassPercentage()))
                .append("</p>");

        // Comparison table (show failures first, then passes)
        html.append("<table style='border-collapse:collapse; width:100%; font-size:12px;'>");
        html.append("<thead><tr style='background:#2c3e50; color:white;'>");
        html.append("<th style='padding:6px; border:1px solid #ddd;'>#</th>");
        html.append("<th style='padding:6px; border:1px solid #ddd;'>Row</th>");
        html.append("<th style='padding:6px; border:1px solid #ddd;'>Column</th>");
        html.append("<th style='padding:6px; border:1px solid #ddd;'>UI Value</th>");
        html.append("<th style='padding:6px; border:1px solid #ddd;'>DB Value</th>");
        html.append("<th style='padding:6px; border:1px solid #ddd;'>Status</th>");
        html.append("</tr></thead><tbody>");

        List<ComparisonResult> allResults = result.getResults();
        int counter = 0;
        for (ComparisonResult cr : allResults) {
            counter++;
            String bgColor = switch (cr.getStatus()) {
                case "PASS" -> "#d4edda";
                case "FAIL" -> "#f8d7da";
                case "MISSING" -> "#fff3cd";
                default -> "#ffffff";
            };

            html.append("<tr style='background:").append(bgColor).append(";'>");
            html.append("<td style='padding:4px; border:1px solid #ddd;'>").append(counter).append("</td>");
            html.append("<td style='padding:4px; border:1px solid #ddd;'>")
                    .append(safe(cr.getRowIdentifier())).append("</td>");
            html.append("<td style='padding:4px; border:1px solid #ddd;'>")
                    .append(safe(cr.getColumnName())).append("</td>");
            html.append("<td style='padding:4px; border:1px solid #ddd;'>")
                    .append(safe(cr.getUiValue())).append("</td>");
            html.append("<td style='padding:4px; border:1px solid #ddd;'>")
                    .append(safe(cr.getDbValue())).append("</td>");
            html.append("<td style='padding:4px; border:1px solid #ddd; font-weight:bold;'>")
                    .append(cr.getStatus()).append("</td>");
            html.append("</tr>");

            // Limit HTML rows to prevent massive reports
            if (counter >= 500) {
                html.append("<tr><td colspan='6' style='padding:8px; text-align:center;'><i>")
                        .append("... and ").append(allResults.size() - 500).append(" more results")
                        .append("</i></td></tr>");
                break;
            }
        }

        html.append("</tbody></table>");
        test.info(html.toString());
        logger.debug("Embedded comparison HTML ({} rows) in Extent report.", counter);
    }

    /**
     * Escapes null values for safe HTML rendering.
     */
    private String safe(String value) {
        return value != null ? value.replace("<", "&lt;").replace(">", "&gt;") : "N/A";
    }
}
