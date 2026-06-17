package com.orion.tests;

import com.orion.pages.DashboardPage;
import com.orion.pages.LoginPage;
import com.orion.utils.ExcelUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class LoginTest extends BaseTest {
    private static final Logger logger = LogManager.getLogger(LoginTest.class);

    @Test(description = "Verify successful login with valid credentials")
    public void testSuccessfulLogin() {
        logger.info("Executing testSuccessfulLogin...");
        LoginPage loginPage = new LoginPage(driver);
        
        // Use standard valid credentials for the-internet demo website
        DashboardPage dashboardPage = loginPage.login("weavers_test@norleegroup.com", "weavers_test@norleegroup.com");
        
        logger.info("Verifying login results...");
        Assert.assertTrue(dashboardPage.isLogoutButtonDisplayed(), "Dashboard logout button is not displayed");
        Assert.assertTrue(dashboardPage.getFlashMessageText().contains("You logged into"), 
                "Success flash message did not match expected pattern");
        
        // Cleanup: logout
        dashboardPage.clickLogout();
    }

    @Test(description = "Verify error message is displayed when logging in with invalid credentials")
    public void testInvalidLogin() {
        logger.info("Executing testInvalidLogin...");
        LoginPage loginPage = new LoginPage(driver);
        
        loginPage.login("invalidUser", "wrongPassword");
        
        logger.info("Verifying failure results...");
        Assert.assertTrue(loginPage.isFlashMessageDisplayed(), "Flash message warning is not displayed");
        Assert.assertTrue(loginPage.getFlashMessageText().contains("invalid"), 
                "Error flash message did not contain expected text");
    }

    @DataProvider(name = "loginExcelData")
    public Object[][] getLoginDataFromExcel() {
        logger.info("Retrieving login test data from Excel...");
        return ExcelUtils.getTestData("LoginData");
    }

    @Test(dataProvider = "loginExcelData", description = "Data-driven login validations using Excel sheet")
    public void testLoginDataDriven(String username, String password, String expectedResult) {
        logger.info("Executing Data-Driven Test. User: {}, Expected: {}", username, expectedResult);
        LoginPage loginPage = new LoginPage(driver);
        
        if (expectedResult.equalsIgnoreCase("success")) {
            DashboardPage dashboardPage = loginPage.login(username, password);
            Assert.assertTrue(dashboardPage.isLogoutButtonDisplayed(), "Dashboard logout button is not displayed");
            Assert.assertTrue(dashboardPage.getFlashMessageText().contains("You logged into"), 
                    "Success flash message did not match expected pattern");
            dashboardPage.clickLogout();
        } else {
            loginPage.login(username, password);
            Assert.assertTrue(loginPage.isFlashMessageDisplayed(), "Flash message warning is not displayed");
            Assert.assertTrue(loginPage.getFlashMessageText().contains("invalid"), 
                    "Error flash message did not contain expected text");
        }
    }
}
