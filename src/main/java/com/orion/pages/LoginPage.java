package com.orion.pages;

import com.orion.base.BasePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class LoginPage extends BasePage {

    // Locators for login page elements
    private final By usernameField = By.id("username");
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
     * Clicks the next button.
     */
    public void clickNextButton() {
        click(nextButton);
    }

    /**
     * Clicks the submit button.
     */
    public void clickSubmitButton() {
        click(submitButton);
    }

    public void clickYesButton() {
        click(yesButton);
    }

    /**
     * Performs a complete login action and returns a new DashboardPage instance.
     * POM Design Principle: Action methods return the instance of the resulting
     * Page.
     */
    public DashboardPage login(String username, String password) {
        clickLoginButton();
        enterUsername(username);
        clickNextButton();
        enterPassword(password);
        clickSubmitButton();
        clickYesButton();
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
