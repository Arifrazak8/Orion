package com.orion.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * ValueNormalizer — Normalises UI and database values into a common
 * representation so they can be reliably compared.
 *
 * <p>Common normalisation tasks:
 * <ul>
 *   <li>Strip currency symbols ({@code $}, {@code €}, {@code £})</li>
 *   <li>Remove thousands separators (commas)</li>
 *   <li>Trim whitespace and non-breaking spaces</li>
 *   <li>Truncate / round decimals to a specified precision</li>
 *   <li>Format raw numbers into US-currency display format</li>
 *   <li>Handle null / empty values gracefully</li>
 * </ul>
 *
 * <p>All methods are stateless and thread-safe.
 */
public final class ValueNormalizer {

    private static final Logger logger = LogManager.getLogger(ValueNormalizer.class);

    private ValueNormalizer() {
        // Utility class — not instantiable
    }

    // -------------------------------------------------------------------------
    // Core normalisation
    // -------------------------------------------------------------------------

    /**
     * Strips a value down to a plain numeric string.
     *
     * <p>Removes: currency symbols ({@code $ € £ ¥}), commas, whitespace,
     * non-breaking spaces, parentheses (used for negative numbers in accounting
     * format), and leading/trailing spaces.
     *
     * <p>Examples:
     * <pre>
     *   "$74,665,217"  →  "74665217"
     *   "($1,234.56)"  →  "-1234.56"
     *   "  100  "      →  "100"
     *   null           →  ""
     * </pre>
     *
     * @param value Raw value from UI or DB.
     * @return Cleaned numeric string, or empty string if input is null/blank.
     */
    public static String stripToNumeric(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "";
        }

        String cleaned = value.trim();

        // Handle accounting-style negatives: ($1,234) → -1234
        boolean isNegative = false;
        if (cleaned.startsWith("(") && cleaned.endsWith(")")) {
            isNegative = true;
            cleaned = cleaned.substring(1, cleaned.length() - 1);
        }

        // Strip currency symbols and separators
        cleaned = cleaned.replaceAll("[\\$€£¥,\\s\\u00A0]", "");

        // Handle explicit negative sign that may have been hidden by currency symbol
        if (cleaned.startsWith("-")) {
            isNegative = true;
            cleaned = cleaned.substring(1);
        }

        if (cleaned.isEmpty()) {
            return "";
        }

        return isNegative ? "-" + cleaned : cleaned;
    }

    /**
     * Strips a value to a whole-number string (integer part only).
     *
     * <p>Examples:
     * <pre>
     *   "$74,665,217.45" → "74665217"
     *   "100.99"         → "100"
     *   "123"            → "123"
     * </pre>
     *
     * @param value Raw value.
     * @return Integer portion as a string, or empty string if null/blank.
     */
    public static String stripToWholeNumber(String value) {
        String numeric = stripToNumeric(value);
        if (numeric.isEmpty()) {
            return "";
        }
        // Truncate at decimal point
        int dotIndex = numeric.indexOf('.');
        if (dotIndex >= 0) {
            return numeric.substring(0, dotIndex);
        }
        return numeric;
    }

    /**
     * Rounds a numeric string to the specified decimal places.
     *
     * @param value  Raw numeric or currency string.
     * @param scale  Number of decimal places.
     * @return Rounded numeric string, or empty string if not parseable.
     */
    public static String roundToScale(String value, int scale) {
        String numeric = stripToNumeric(value);
        if (numeric.isEmpty()) {
            return "";
        }
        try {
            BigDecimal bd = new BigDecimal(numeric).setScale(scale, RoundingMode.HALF_UP);
            return bd.toPlainString();
        } catch (NumberFormatException e) {
            logger.warn("Cannot round non-numeric value: '{}'", value);
            return numeric;
        }
    }

    // -------------------------------------------------------------------------
    // Formatting (for display / report generation)
    // -------------------------------------------------------------------------

    /**
     * Formats a raw numeric value into US-style display format with commas.
     *
     * <p>Example: {@code "74665217"} → {@code "74,665,217"}
     *
     * @param value Raw numeric string or BigDecimal string.
     * @return Comma-formatted string, or the original value if not parseable.
     */
    public static String formatWithCommas(String value) {
        String numeric = stripToNumeric(value);
        if (numeric.isEmpty()) {
            return value != null ? value : "";
        }
        try {
            double d = Double.parseDouble(numeric);
            DecimalFormat df = new DecimalFormat("#,###");
            return df.format(d);
        } catch (NumberFormatException e) {
            logger.warn("Cannot format non-numeric value with commas: '{}'", value);
            return value;
        }
    }

    /**
     * Formats a raw numeric value as US currency (e.g. {@code "$74,665,217"}).
     *
     * @param value Raw numeric string.
     * @return Currency-formatted string.
     */
    public static String formatAsCurrency(String value) {
        String formatted = formatWithCommas(value);
        if (formatted.isEmpty()) {
            return "$0";
        }
        if (formatted.startsWith("-")) {
            return "-$" + formatted.substring(1);
        }
        return "$" + formatted;
    }

    // -------------------------------------------------------------------------
    // Comparison helpers
    // -------------------------------------------------------------------------

    /**
     * Compares two values after normalising both to plain numeric strings.
     *
     * <p>This is the recommended comparison method for currency / formatted
     * number fields where the UI shows {@code "$74,665,217"} and the DB
     * returns {@code "74665217.00"}.
     *
     * @param uiValue Value captured from the UI.
     * @param dbValue Value fetched from the database.
     * @return {@code true} if the normalised values are equal.
     */
    public static boolean numericEquals(String uiValue, String dbValue) {
        String normalizedUi = stripToWholeNumber(uiValue);
        String normalizedDb = stripToWholeNumber(dbValue);
        boolean match = normalizedUi.equals(normalizedDb);
        if (!match) {
            logger.debug("numericEquals FAIL — UI: '{}' (→'{}'), DB: '{}' (→'{}')",
                    uiValue, normalizedUi, dbValue, normalizedDb);
        }
        return match;
    }

    /**
     * Compares two values after normalising both with full decimal precision.
     *
     * @param uiValue Value from UI.
     * @param dbValue Value from DB.
     * @param scale   Decimal places to compare.
     * @return {@code true} if the normalised+rounded values are equal.
     */
    public static boolean numericEquals(String uiValue, String dbValue, int scale) {
        String normalizedUi = roundToScale(uiValue, scale);
        String normalizedDb = roundToScale(dbValue, scale);
        boolean match = normalizedUi.equals(normalizedDb);
        if (!match) {
            logger.debug("numericEquals({}) FAIL — UI: '{}' (→'{}'), DB: '{}' (→'{}')",
                    scale, uiValue, normalizedUi, dbValue, normalizedDb);
        }
        return match;
    }

    /**
     * Compares two string values after trimming whitespace.
     * Case-sensitive by default.
     *
     * @param uiValue Value from UI.
     * @param dbValue Value from DB.
     * @return {@code true} if trimmed values are equal.
     */
    public static boolean textEquals(String uiValue, String dbValue) {
        String ui = (uiValue != null) ? uiValue.trim() : "";
        String db = (dbValue != null) ? dbValue.trim() : "";
        return ui.equals(db);
    }

    /**
     * Case-insensitive text comparison after trimming.
     */
    public static boolean textEqualsIgnoreCase(String uiValue, String dbValue) {
        String ui = (uiValue != null) ? uiValue.trim() : "";
        String db = (dbValue != null) ? dbValue.trim() : "";
        return ui.equalsIgnoreCase(db);
    }

    /**
     * Normalises a value for generic comparison.
     * Strips whitespace, non-breaking spaces, and converts to lowercase.
     *
     * @param value Raw value.
     * @return Normalised string suitable for equality checks.
     */
    public static String normalizeForComparison(String value) {
        if (value == null) return "";
        return value.trim()
                .replaceAll("\\u00A0", " ")   // non-breaking space → regular space
                .replaceAll("\\s+", " ")       // collapse multiple spaces
                .trim();
    }
}
