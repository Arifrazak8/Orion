package com.orion.pages;

import com.orion.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class LoginPage extends BasePage {

    // Locators for login page elements
    private final By usernameField = By.xpath("//input[@type='submit']");
    private final By passwordField = By.id("password");
    private final By loginButton = By.cssSelector("button[type='submit']");
    private final By flashMessage = By.id("flash");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Enters the username.
     */
    public void enterUsername(String username) {
        sendKeys(usernameField, username);
    }

    /**
     * Enters the password.
     */
    public void enterPassword(String password) {
        sendKeys(passwordField, password);
    }

    /**
     * Clicks the login submit button.
     */
    public void clickLoginButton() {
        click(loginButton);
    }

    /**
     * Performs a complete login action and returns a new DashboardPage instance.
     * POM Design Principle: Action methods return the instance of the resulting Page.
     */
    public DashboardPage login(String username, String password) {
        enterUsername(username);
        enterPassword(password);
        clickLoginButton();
        return new DashboardPage(driver);
    }

    /**
     * Gets the text of the flash error/success message alert.
     */
    public String getFlashMessageText() {
        return getText(flashMessage);
    }

    /**
     * Checks if the flash message alert is displayed.
     */
    public boolean isFlashMessageDisplayed() {
        return isDisplayed(flashMessage);
    }
}
