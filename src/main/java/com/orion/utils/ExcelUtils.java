package com.orion.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelUtils {
    private static final Logger logger = LogManager.getLogger(ExcelUtils.class);
    private static final String DEFAULT_FILE_PATH = "src/test/resources/testdata.xlsx";
    private static final String DEFAULT_SHEET_NAME = "LoginData";

    /**
     * Reads data from the Excel sheet and returns it as a 2D Object array for TestNG DataProvider.
     */
    public static Object[][] getTestData(String sheetName) {
        String filePath = DEFAULT_FILE_PATH;
        ensureExcelFileExists(filePath, sheetName);

        Object[][] data = null;
        Workbook workbook = null;
        FileInputStream fileInputStream = null;

        try {
            fileInputStream = new FileInputStream(filePath);
            workbook = WorkbookFactory.create(fileInputStream);
            Sheet sheet = workbook.getSheet(sheetName);

            if (sheet == null) {
                logger.error("Sheet '{}' not found in {}", sheetName, filePath);
                throw new RuntimeException("Sheet not found: " + sheetName);
            }

            int rowCount = sheet.getLastRowNum();
            int colCount = sheet.getRow(0).getLastCellNum();

            // Row index starts from 0. Row 0 is header, so we read from Row 1.
            data = new Object[rowCount][colCount];
            DataFormatter formatter = new DataFormatter();

            for (int i = 0; i < rowCount; i++) {
                Row row = sheet.getRow(i + 1);
                for (int j = 0; j < colCount; j++) {
                    if (row == null) {
                        data[i][j] = "";
                    } else {
                        Cell cell = row.getCell(j);
                        data[i][j] = formatter.formatCellValue(cell);
                    }
                }
            }
            logger.info("Successfully loaded {} rows of test data from sheet '{}'", rowCount, sheetName);
        } catch (Exception e) {
            logger.error("Error reading Excel data from: {}", filePath, e);
            throw new RuntimeException("Could not read test data from Excel.", e);
        } finally {
            try {
                if (workbook != null) workbook.close();
                if (fileInputStream != null) fileInputStream.close();
            } catch (IOException e) {
                logger.error("Error closing Excel resources.", e);
            }
        }
        return data;
    }

    /**
     * Helper to create a template Excel file if it doesn't exist.
     */
    public static void ensureExcelFileExists(String filePath, String sheetName) {
        File file = new File(filePath);
        if (file.exists()) {
            return;
        }

        logger.info("Excel file not found at '{}'. Creating a default template file...", filePath);
        
        // Ensure parent directories exist
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            
            Sheet sheet = workbook.createSheet(sheetName);
            
            // Header Row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Username");
            headerRow.createCell(1).setCellValue("Password");
            headerRow.createCell(2).setCellValue("ExpectedResult");

            // Data Rows (Validating against standard website: the-internet.herokuapp.com/login)
            Row row1 = sheet.createRow(1);
            row1.createCell(0).setCellValue("tomsmith");
            row1.createCell(1).setCellValue("SuperSecretPassword!");
            row1.createCell(2).setCellValue("success");

            Row row2 = sheet.createRow(2);
            row2.createCell(0).setCellValue("invalidUser");
            row2.createCell(1).setCellValue("SuperSecretPassword!");
            row2.createCell(2).setCellValue("failure");

            Row row3 = sheet.createRow(3);
            row3.createCell(0).setCellValue("tomsmith");
            row3.createCell(1).setCellValue("wrongPassword");
            row3.createCell(2).setCellValue("failure");

            // Auto-size columns
            for (int i = 0; i < 3; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(fileOutputStream);
            logger.info("Successfully created default template Excel file at: {}", filePath);
        } catch (IOException e) {
            logger.error("Failed to create default template Excel file at: {}", filePath, e);
        }
    }
}
