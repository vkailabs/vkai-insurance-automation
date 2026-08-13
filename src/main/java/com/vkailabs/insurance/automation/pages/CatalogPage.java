package com.vkailabs.insurance.automation.pages;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
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
    private final By planCard = By.cssSelector("article.policy-card");

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

    /** Number of plan cards currently rendered on the catalog. */
    public int planCount() {
        return driver.findElements(planCard).size();
    }

    /**
     * True if the catalog shows at least one plan and every plan card exposes a non-empty
     * name, Premium figure, and Coverage figure. Card DOM:
     * {@code <article class="policy-card"><h3 class="policy-card-title">…</h3>
     * <dl class="policy-card-figures"><div><dt>Premium</dt><dd>$…</dd></div>
     * <div><dt>Coverage</dt><dd>$…</dd></div></dl>…}
     */
    public boolean everyPlanShowsNamePremiumCoverage() {
        try {
            wait.waitForVisible(planCard);
        } catch (TimeoutException e) {
            log.warn("No plan cards appeared on the catalog");
            return false;
        }
        List<WebElement> cards = driver.findElements(planCard);
        for (WebElement card : cards) {
            String name = textOrEmpty(card, By.cssSelector(".policy-card-title"));
            String premium = figureValue(card, "Premium");
            String coverage = figureValue(card, "Coverage");
            if (name.isBlank() || premium.isBlank() || coverage.isBlank()) {
                log.warn("Plan card incomplete — name='{}', premium='{}', coverage='{}'",
                        name, premium, coverage);
                return false;
            }
        }
        return true;
    }

    private String textOrEmpty(WebElement scope, By by) {
        List<WebElement> els = scope.findElements(by);
        return els.isEmpty() ? "" : els.get(0).getText().trim();
    }

    /** The {@code <dd>} value for a figure whose {@code <dt>} label matches (e.g. "Premium"). */
    private String figureValue(WebElement card, String label) {
        List<WebElement> dds = card.findElements(By.xpath(
                ".//div[dt[normalize-space()='" + label + "']]/dd"));
        return dds.isEmpty() ? "" : dds.get(0).getText().trim();
    }
}
