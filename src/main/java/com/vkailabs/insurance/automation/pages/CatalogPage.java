package com.vkailabs.insurance.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Page Object for the client policy catalog (the customer's browse-and-enroll view).
 *
 * <p>Each plan renders as a card with a "KEY - Name" heading and its own "Enroll"
 * button. Since the DOM exposes no stable card class or {@code data-testid}, the
 * Enroll button is located relative to its plan heading: the nearest ancestor of the
 * heading that also contains an "Enroll" button is the plan card, and we click that
 * card's Enroll. The plan title must be matched exactly — there are near-identical
 * plans ("BHP - Basic Health Plan" vs "Basic Health Plan - 1").
 */
public class CatalogPage extends BasePage {

    private static final Logger log = LoggerFactory.getLogger(CatalogPage.class);

    private final By catalogNav = By.xpath(
            "//a[normalize-space()='Catalog'] | //button[normalize-space()='Catalog']");
    private final By catalogTitle = By.xpath(
            "//*[self::h1 or self::h2 or self::h3][normalize-space()='Policy catalog']");

    public CatalogPage(WebDriver driver) {
        super(driver);
    }

    /** Navigates to the catalog via the nav bar and waits for it to load. */
    public CatalogPage open() {
        wait.waitForClickable(catalogNav).click();
        wait.waitForVisible(catalogTitle);
        return this;
    }

    /** Clicks the "Enroll" button on the card for the exact {@code planTitle}. */
    public void enrollInPlan(String planTitle) {
        By enrollButton = By.xpath(String.format(
                "//*[self::h1 or self::h2 or self::h3][normalize-space()='%s']"
                + "/ancestor::*[.//button[normalize-space()='Enroll']][1]"
                + "//button[normalize-space()='Enroll']",
                planTitle));
        wait.waitForClickable(enrollButton).click();
        log.info("Clicked Enroll for plan '{}'", planTitle);
    }
}
