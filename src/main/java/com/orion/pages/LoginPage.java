package com.orion.pages;

import com.orion.base.BasePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class LoginPage extends BasePage {

    // Locators for login page elements
    @FindBy(xpath = "//*[contains(@class,'form-control ltr_override input ext-input text-box ext-text-box')]")
    private WebElement usernameField;

    @FindBy(name = "passwd")
    private WebElement passwordField;

    @FindBy(xpath = "//INPUT[contains(@class,'form-control transparent-border')]")
    private WebElement loginButton;

    @FindBy(id = "idSIButton9")
    private WebElement nextButton;

    @FindBy(xpath = "//DIV[@data-bind=\"css: { 'inline-block': isPrimaryButtonVisible }, externalCss: { 'button-item': true }\"]/INPUT[normalize-space(@data-report-event)='Signin_Submit']")
    private WebElement submitButton;

    @FindBy(id = "idSIButton9")
    private WebElement yesButton;

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
}
