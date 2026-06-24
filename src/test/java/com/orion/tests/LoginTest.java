package com.orion.tests;

import com.orion.pages.DashboardPage;
import com.orion.pages.LoginPage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

public class LoginTest extends BaseTest {
    private static final Logger logger = LogManager.getLogger(LoginTest.class);

    @Test(description = "Verify successful login with valid credentials")
    public void testSuccessfulLogin() {
        logger.info("Executing testSuccessfulLogin...");
        LoginPage loginPage = new LoginPage(driver);

        // Use standard valid credentials for the-internet demo website
<<<<<<< HEAD
        DashboardPage dashboardPage = loginPage.login("weavers_test@norleegroup.com", "weavers_test@norleegroup.com");
        
=======
        DashboardPage dashboardPage = loginPage.login("weavers_test@norleegroup.com", "4bj\"u>T=h4AZ");

>>>>>>> cd019c7c23ad5b9ef953db70fc0748f983327d9e
        logger.info("Verifying login results...");
        Assert.assertTrue(dashboardPage.isLogoutButtonDisplayed(), "Dashboard logout button is not displayed");
        Assert.assertTrue(dashboardPage.getFlashMessageText().contains("You logged into"),
                "Success flash message did not match expected pattern");

        // Cleanup: logout
        dashboardPage.clickLogout();
    }

}
