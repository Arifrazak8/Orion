package com.orion.pages;

import com.orion.base.BasePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class MenuPage extends BasePage {

    // Locators for menu items
    @FindBy(xpath = "//SPAN[normalize-space(.)='Sales Pipeline']")
    private WebElement salesPipelineMenu;

    @FindBy(xpath = "//SPAN[normalize-space(.)='Sales Call Review']")
    private WebElement salesCallReviewMenu;

    @FindBy(xpath = "//LI[contains(@class,'has_submenu active show-submenu')]/UL/LI[1]/A/SPAN[@class='text']")
    private WebElement salesDashboardMenu;

    /**
     * Constructor for MenuPage.
     * Initializes the driver and WebElements using PageFactory via BasePage constructor.
     */
    public MenuPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Clicks on the Sales Pipeline menu item.
     * @return SalesPipelinePage
     */
    public SalesPipelinePage clickSalesPipeline() {
        click(salesPipelineMenu);
        return new SalesPipelinePage(driver);
    }

    /**
     * Clicks on the Sales Call Review menu item.
     */
    public void clickSalesCallReview() {
        click(salesCallReviewMenu);
    }

    /**
     * Clicks on the Sales Dashboard menu item.
     */
    public void clickSalesDashboard() {
        click(salesDashboardMenu);
    }
}
