package com.orion.utils;

import org.openqa.selenium.By;
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
        // 1. Find Select2 container
        WebElement select2Container = hiddenSelect.findElement(By.xpath("./following-sibling::span[contains(@class, 'select2-container')]"));
        
        // 2. Click the selection area to open dropdown
        WebElement selection = select2Container.findElement(By.cssSelector(".select2-selection"));
        selection.click();
        
        // Small pause for dropdown to open/render
        try { Thread.sleep(500); } catch (InterruptedException e) {}
        
        // 3. Try to find the search input field
        WebElement searchInput = null;
        try {
            // First try within the container (multiple select inline search)
            searchInput = select2Container.findElement(By.cssSelector("input.select2-search__field, textarea.select2-search__field"));
        } catch (org.openqa.selenium.NoSuchElementException e) {
            try {
                // Then try globally (single select dropdown appended to body)
                searchInput = driver.findElement(By.cssSelector("span.select2-container--open input.select2-search__field, span.select2-container--open textarea.select2-search__field"));
            } catch (org.openqa.selenium.NoSuchElementException ex) {
                // Search field might not exist if disabled
            }
        }
        
        if (searchInput != null && searchInput.isDisplayed()) {
            searchInput.clear();
            searchInput.sendKeys(optionText);
            try { Thread.sleep(500); } catch (InterruptedException e) {}
        }
        
        // 4. Find the matching option in the dropdown results and click it
        List<WebElement> results = driver.findElements(By.cssSelector("li.select2-results__option"));
        boolean clicked = false;
        for (WebElement res : results) {
            String text = res.getText().trim();
            if (text.equalsIgnoreCase(optionText.trim()) || text.contains(optionText.trim())) {
                res.click();
                clicked = true;
                break;
            }
        }
        
        if (clicked) {
            // Wait for the dropdown to close/events to propagate
            try { Thread.sleep(500); } catch (InterruptedException e) {}
            return;
        }
        
        throw new RuntimeException("Could not click option '" + optionText + "' in Select2 dropdown");
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
