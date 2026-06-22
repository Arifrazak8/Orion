package com.orion.constants;

import com.orion.utils.ConfigReader;

public class TableConstants {

    // Divisions
    public static final String DIVISION_MULTI_FAMILY = ConfigReader.getProperty("division.multi_family",
            "02 - Multi Family");
    public static final String DIVISION_COMMERCIAL = ConfigReader.getProperty("division.commercial", "03 - Commercial");
    public static final String DIVISION_UNDERGROUND = ConfigReader.getProperty("division.underground",
            "04 - Underground");
    public static final String DIVISION_HOSPITALITY = ConfigReader.getProperty("division.hospitality",
            "05 - Hospitality");
    public static final String DIVISION_TECHNOLOGY = ConfigReader.getProperty("division.technology", "07 - Technology");
    public static final String DIVISION_HEALTHCARE = ConfigReader.getProperty("division.healthcare", "08 - Healthcare");
    public static final String DIVISION_RESIDENTIAL = ConfigReader.getProperty("division.residential",
            "09 - Residential");
    public static final String DIVISION_SERVICE = ConfigReader.getProperty("division.service", "10 - Service");
    public static final String DIVISION_SPECIALTY_PROJECTS = ConfigReader.getProperty("division.specialty_projects",
            "11 - Specialty Projects");
    public static final String DIVISION_MECHANICAL = ConfigReader.getProperty("division.mechanical", "20 - Mechanical");
    public static final String DIVISION_ENGINEERING = ConfigReader.getProperty("division.engineering",
            "30 - Engineering");

    // Columns
    public static final String COLUMN_LOI = ConfigReader.getProperty("column.loi", "LOI");
    public static final String COLUMN_CONFIRM_90 = ConfigReader.getProperty("column.confirm_90", "90% Confirm");
    public static final String COLUMN_CONFIRM_50 = ConfigReader.getProperty("column.confirm_50", "50% Confirm");
    public static final String COLUMN_BBS = ConfigReader.getProperty("column.bbs", "B/Bs");
    public static final String COLUMN_TOTAL = ConfigReader.getProperty("column.total", "Total");
    public static final String COLUMN_TOTAL_EXCL_2BS = ConfigReader.getProperty("column.total_excl_2bs",
            "Total Excl. 2Bs");
}
