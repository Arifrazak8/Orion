package com.orion.utils;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Generic utility for interacting with hidden native select elements wrapped by Select2.
 */
public class Select2Utils {

    private WebDriver driver;

    public Select2Utils(WebDriver driver) {
        this.driver = driver;
    }

    /**
     * Unhides the native select element, selects an option, and triggers a change event.
     */
    public void selectOption(WebElement hiddenSelect, String optionText) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].style.display='block'; arguments[0].style.visibility='visible';", hiddenSelect);
        
        Select select = new Select(hiddenSelect);
        select.selectByVisibleText(optionText);
        
        js.executeScript("arguments[0].dispatchEvent(new Event('change'));", hiddenSelect);
    }

    /**
     * Gets all available options in the Select2 dropdown.
     */
    public List<String> getAvailableOptions(WebElement hiddenSelect) {
        Select select = new Select(hiddenSelect);
        return select.getOptions().stream()
                .map(WebElement::getText)
                .map(String::trim)
                .filter(text -> !text.isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * Gets currently selected option(s). Returns a list to support multi-select.
     */
    public List<String> getSelectedOptions(WebElement hiddenSelect) {
        Select select = new Select(hiddenSelect);
        return select.getAllSelectedOptions().stream()
                .map(WebElement::getText)
                .map(String::trim)
                .collect(Collectors.toList());
    }
}
