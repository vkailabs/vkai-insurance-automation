package com.vkailabs.insurance.automation.pages;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Page Object for the client dashboard (the post-login landing + "Your policies").
 *
 * <p>The client portal is a React SPA with no {@code data-testid} hooks, so locators
 * key off durable visible text. The "Your policies" heading is the dashboard-specific
 * marker; "Logout" confirms an authenticated session.
 *
 * <p>A policy renders as {@code <article class="policy-card">} with an
 * {@code <h3 class="policy-card-title">} and a {@code <span class="status-pill status-*">}
 * status pill. We locate a card by its exact title plus the status <em>class</em>
 * ({@code status-pending}, …) rather than the pill's (lowercase) text, so the match is
 * immune to display-copy casing.
 */
public class DashboardPage extends BasePage {

    private static final Logger log = LoggerFactory.getLogger(DashboardPage.class);

    private static final String LOGIN_PATH = "/login";

    private final By policiesHeading = By.xpath(
            "//*[self::h1 or self::h2 or self::h3][normalize-space()='Your policies']");
    // Policy summary block (VKAI-005): <div class="policy-summary"> holding one
    // <div class="summary-box"> per status, each with a <span class="summary-count">
    // and a <span class="summary-label"> ("Active" / "Pending"). Sits directly above
    // the "Your policies" header.
    private final By policySummary = By.cssSelector("div.policy-summary");
    private final By logoutControl = By.xpath(
            "//*[self::button or self::a][normalize-space()='Logout']");
    private final By dashboardNav = By.xpath(
            "//a[normalize-space()='Dashboard'] | //button[normalize-space()='Dashboard']");

    public DashboardPage(WebDriver driver) {
        super(driver);
    }

    /** Navigates to the dashboard via the nav bar and waits for it to load. */
    public DashboardPage open() {
        wait.waitForClickable(dashboardNav).click();
        wait.waitForVisible(policiesHeading);
        return this;
    }

    /**
     * Returns true once the authenticated dashboard is confirmed loaded, false if the
     * expected elements do not appear within the explicit-wait window.
     */
    public boolean isLoaded() {
        try {
            wait.waitForUrlToNotContain(LOGIN_PATH);   // navigated away from login
            wait.waitForVisible(logoutControl);        // authenticated session
            wait.waitForVisible(policiesHeading);      // dashboard-specific content
            log.info("Dashboard confirmed loaded at {}", driver.getCurrentUrl());
            return true;
        } catch (TimeoutException e) {
            log.warn("Dashboard did not load within the wait window (url={}): {}",
                    driver.getCurrentUrl(), e.getMessage());
            return false;
        }
    }

    /** True if a policy for {@code planTitle} is shown with the given {@code status}. */
    public boolean isPolicyListed(String planTitle, String status) {
        try {
            wait.waitForVisible(policyCard(planTitle, status));
            return true;
        } catch (TimeoutException e) {
            log.warn("No '{}' policy card with status '{}' found: {}",
                    planTitle, status, e.getMessage());
            return false;
        }
    }

    /**
     * True if the pending {@code planTitle} policy card exposes a clickable control whose
     * text contains {@code controlText} (case-insensitive). Used to assert the negative
     * case for a pending policy, where the control is expected to be absent entirely.
     */
    public boolean hasControlForPendingPolicy(String controlText, String planTitle) {
        WebElement card = wait.waitForVisible(policyCard(planTitle, "Pending"));
        String needle = controlText.toLowerCase();
        List<WebElement> controls = card.findElements(By.xpath(
                ".//*[self::button or self::a]"
                + "[contains(translate(normalize-space(.),"
                + " 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '"
                + needle + "')]"));
        return !controls.isEmpty();
    }

    /** True if the policy-summary block is present on the dashboard. */
    public boolean isPolicySummaryDisplayed() {
        try {
            wait.waitForVisible(policySummary);
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    /** True if a summary box for the given status label (e.g. "Active") is displayed. */
    public boolean isSummaryDisplayed(String label) {
        try {
            wait.waitForVisible(summaryBox(label));
            return true;
        } catch (TimeoutException e) {
            log.warn("No summary box found for label '{}'", label);
            return false;
        }
    }

    /**
     * True if the summary box for {@code label} precedes the given heading in document
     * order (i.e. renders above it). Uses the XPath {@code following::} axis so the check
     * is about real DOM position, not pixel geometry.
     */
    public boolean isSummaryAboveHeading(String label, String heading) {
        By locator = By.xpath(String.format(
                "//div[contains(concat(' ', normalize-space(@class), ' '), ' summary-box ')]"
                + "[.//span[contains(concat(' ', normalize-space(@class), ' '), ' summary-label ')]"
                + "[normalize-space()='%s']]"
                + "/following::*[self::h1 or self::h2 or self::h3][normalize-space()='%s']",
                label, heading));
        return !driver.findElements(locator).isEmpty();
    }

    /** The numeric count shown in the summary box for {@code label}. */
    public int summaryCount(String label) {
        WebElement box = wait.waitForVisible(summaryBox(label));
        String text = box.findElement(By.xpath(
                ".//span[contains(concat(' ', normalize-space(@class), ' '), ' summary-count ')]"))
                .getText().trim();
        return Integer.parseInt(text);
    }

    /** The number of policy cards currently rendered with the given status. */
    public int policyCardCountByStatus(String status) {
        String statusClass = "status-" + status.toLowerCase().replace(" ", "-");
        By cards = By.xpath(String.format(
                "//article[contains(concat(' ', normalize-space(@class), ' '), ' policy-card ')]"
                + "[.//span[contains(concat(' ', normalize-space(@class), ' '), ' %s ')]]",
                statusClass));
        return driver.findElements(cards).size();
    }

    /**
     * A summary box ({@code <div class="summary-box">}) whose label span text matches
     * {@code label} exactly (e.g. "Active", "Pending").
     */
    private By summaryBox(String label) {
        return By.xpath(String.format(
                "//div[contains(concat(' ', normalize-space(@class), ' '), ' policy-summary ')]"
                + "//div[contains(concat(' ', normalize-space(@class), ' '), ' summary-box ')]"
                + "[.//span[contains(concat(' ', normalize-space(@class), ' '), ' summary-label ')]"
                + "[normalize-space()='%s']]",
                label));
    }

    /**
     * A policy card ({@code <article class="policy-card">}) matching the exact plan title
     * and the status pill's {@code status-<status>} class (e.g. "Pending" → the
     * {@code status-pending} pill). Matching the class, not the pill text, avoids the
     * lowercase display-copy ("pending") vs. capitalized-status mismatch.
     */
    private By policyCard(String planTitle, String status) {
        String statusClass = "status-" + status.toLowerCase().replace(" ", "-");
        return By.xpath(String.format(
                "//article[contains(concat(' ', normalize-space(@class), ' '), ' policy-card ')]"
                + "[.//h3[contains(@class, 'policy-card-title')][normalize-space()='%s']]"
                + "[.//span[contains(concat(' ', normalize-space(@class), ' '), ' %s ')]]",
                planTitle, statusClass));
    }
}
