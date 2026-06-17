package com.orion.pages;

import com.orion.base.BasePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class DashboardPage extends BasePage {

    // Locators for dashboard elements
    @FindBy(css = "h2")
    private WebElement secureAreaHeader;

    @FindBy(css = "a[href='/logout']")
    private WebElement logoutButton;

    @FindBy(id = "flash")
    private WebElement flashMessage;

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
