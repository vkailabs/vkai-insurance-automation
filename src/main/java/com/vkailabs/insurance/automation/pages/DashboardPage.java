package com.vkailabs.insurance.automation.pages;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Page Object for the client dashboard (the post-login landing + policy sections).
 *
 * <p>The client portal is a React SPA with no {@code data-testid} hooks, so locators
 * key off durable visible text / class structure. The {@code div.policy-summary} block is
 * the dashboard-specific content marker; "Logout" confirms an authenticated session.
 *
 * <p>Since VKAI-006 the policy list is grouped into two
 * {@code <section class="policy-section">} blocks, each headed by an
 * {@code <h2 class="section-title">} ("Your Active Policies" then "Your Pending Policies"
 * in DOM order), rendered <em>below</em> the still-present {@code <h1 class="page-title">Your
 * policies</h1>}. A populated section holds a {@code <div class="card-grid">} of policy
 * cards; an empty one shows a {@code <p class="policy-section-empty">}. The dashboard-loaded
 * marker keys off the always-present, unambiguous policy-summary block rather than any
 * heading.
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

    // Policy summary block (VKAI-005): <div class="policy-summary"> holding one
    // <div class="summary-box"> per status, each with a <span class="summary-count">
    // and a <span class="summary-label"> ("Active" / "Pending"). Always present once the
    // dashboard data has loaded, so it doubles as the dashboard-loaded content marker.
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
        wait.waitForVisible(policySummary);
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
            wait.waitForVisible(policySummary);        // dashboard-specific content
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

    // ---- Policy sections (VKAI-006): Active vs Pending split ----------------------

    /** True if a policy section headed by the exact {@code title} is displayed. */
    public boolean isPolicySectionDisplayed(String title) {
        try {
            wait.waitForVisible(sectionTitleHeading(title));
            return true;
        } catch (TimeoutException e) {
            log.warn("No policy section headed '{}' found: {}", title, e.getMessage());
            return false;
        }
    }

    /**
     * True if the section headed {@code firstTitle} precedes the one headed
     * {@code secondTitle} in document order. Uses the XPath {@code following::} axis so the
     * check is about real DOM position, not pixel geometry.
     */
    public boolean isSectionAboveSection(String firstTitle, String secondTitle) {
        By locator = By.xpath(String.format(
                "//h2[contains(concat(' ', normalize-space(@class), ' '), ' section-title ')]"
                + "[normalize-space()='%s']"
                + "/following::h2[contains(concat(' ', normalize-space(@class), ' '), ' section-title ')]"
                + "[normalize-space()='%s']",
                firstTitle, secondTitle));
        return !driver.findElements(locator).isEmpty();
    }

    /** Total number of policy cards rendered inside the section headed {@code title}. */
    public int cardCountInSection(String title) {
        By cards = By.xpath(sectionXpath(title)
                + "//article[contains(concat(' ', normalize-space(@class), ' '), ' policy-card ')]");
        return driver.findElements(cards).size();
    }

    /**
     * Number of policy cards with the given {@code status} pill inside the section headed
     * {@code title}. Matches on the {@code status-<status>} class, not the pill's (lowercase)
     * text, consistent with the rest of this page object.
     */
    public int cardCountInSectionByStatus(String title, String status) {
        String statusClass = "status-" + status.toLowerCase().replace(" ", "-");
        By cards = By.xpath(sectionXpath(title)
                + "//article[contains(concat(' ', normalize-space(@class), ' '), ' policy-card ')]"
                + "[.//span[contains(concat(' ', normalize-space(@class), ' '), ' " + statusClass + " ')]]");
        return driver.findElements(cards).size();
    }

    // ---- Pending-policy cancellation (VKAI-010) -----------------------------------
    //
    // Since VKAI-010, a Pending policy card carries a Cancel control inside its
    // <div class="policy-card-actions">: <button class="btn btn-danger btn-small
    // policy-cancel-btn">Cancel</button> (label flips to "Cancelling…", disabled, while the
    // request is in flight). The control renders ONLY on pending cards, i.e. only inside the
    // "Your Pending Policies" section — Active/other cards expose no Cancel button. Clicking it
    // opens a native window.confirm ("Cancel this pending policy? This cannot be undone.");
    // accepting calls the cancel API and the policy is then HIDDEN from the dashboard entirely
    // (excluded from both sections and both counts — a deliberate client-side decision), so the
    // "Cancelled" status pill is never shown here (it surfaces on the provider portal, out of
    // scope). We locate the button by its stable policy-cancel-btn class, scoped to a section.
    //
    // These locators are grounded on the client subagent's reported stable DOM for commit
    // 7f4b49d (same grounding basis as the VKAI-006 section split), not an independent live
    // capture — the live run pass re-verifies/adjusts them.

    private static final String PENDING_SECTION_TITLE = "Your Pending Policies";

    /** Number of {@code .policy-cancel-btn} controls rendered inside the section headed {@code title}. */
    public int cancelButtonCountInSection(String title) {
        By buttons = By.xpath(sectionXpath(title)
                + "//button[contains(concat(' ', normalize-space(@class), ' '), ' policy-cancel-btn ')]");
        return driver.findElements(buttons).size();
    }

    /**
     * Clicks the first {@code .policy-cancel-btn} in the "Your Pending Policies" section and
     * returns the text of the native confirmation dialog it opens, without acting on it — the
     * caller decides whether to {@link #dismissOpenConfirm()} (safe no-op) or accept. Waiting
     * for the dialog also proves the button is wired to a confirm gate.
     */
    public String clickFirstPendingCancelAndReadConfirm() {
        By firstCancel = By.xpath(sectionXpath(PENDING_SECTION_TITLE)
                + "//button[contains(concat(' ', normalize-space(@class), ' '), ' policy-cancel-btn ')]");
        wait.waitForClickable(firstCancel).click();
        String text = wait.waitForAlert().getText().trim();
        log.info("Pending-policy cancel confirmation dialog text: '{}'", text);
        return text;
    }

    /** Dismisses the currently-open confirmation dialog — the safe no-op path (no cancellation). */
    public void dismissOpenConfirm() {
        driver.switchTo().alert().dismiss();
    }

    /** The {@code <h2 class="section-title">} heading whose text matches {@code title} exactly. */
    private By sectionTitleHeading(String title) {
        return By.xpath(String.format(
                "//h2[contains(concat(' ', normalize-space(@class), ' '), ' section-title ')]"
                + "[normalize-space()='%s']",
                title));
    }

    /**
     * The {@code <section class="policy-section">} that contains the {@code section-title}
     * heading with the exact {@code title} text.
     */
    private String sectionXpath(String title) {
        return String.format(
                "//section[contains(concat(' ', normalize-space(@class), ' '), ' policy-section ')]"
                + "[.//h2[contains(concat(' ', normalize-space(@class), ' '), ' section-title ')]"
                + "[normalize-space()='%s']]",
                title);
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
