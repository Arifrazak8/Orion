package com.orion.pages;

import com.orion.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class DashboardPage extends BasePage {

    // Locators for dashboard elements
    private final By secureAreaHeader = By.cssSelector("h2");
    private final By logoutButton = By.cssSelector("a[href='/logout']");
    private final By flashMessage = By.id("flash");

    public DashboardPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Retrieves the main dashboard header text.
     */
    public String getHeaderText() {
        return getText(secureAreaHeader);
    }

    /**
     * Checks if the logout button is visible.
     */
    public boolean isLogoutButtonDisplayed() {
        return isDisplayed(logoutButton);
    }

    /**
     * Performs a logout and redirects back to the LoginPage.
     */
    public LoginPage clickLogout() {
        click(logoutButton);
        return new LoginPage(driver);
    }

    /**
     * Gets the text of the flash message.
     */
    public String getFlashMessageText() {
        return getText(flashMessage);
    }
}
