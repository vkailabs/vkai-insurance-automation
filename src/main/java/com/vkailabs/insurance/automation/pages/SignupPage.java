package com.vkailabs.insurance.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vkailabs.insurance.automation.utils.ConfigReader;

/**
 * Page Object for the client signup screen ("Create your account", Firebase).
 *
 * <p>Same hookless form as login (no id/name/placeholder/data-testid), so inputs are
 * keyed by type — there is exactly one text/email/password input. Reused across the
 * registration scenarios (AUTH-001/002/003).
 */
public class SignupPage extends BasePage {

    private static final Logger log = LoggerFactory.getLogger(SignupPage.class);

    private static final String SIGNUP_PATH = "/signup";
    // A valid-format password so a duplicate-email attempt is rejected on the email,
    // not pre-empted by a weak-password validation error.
    private static final String VALID_PASSWORD = "Str0ng!Pass123";

    private final By fullNameInput = By.cssSelector("input[type='text']");
    private final By emailInput = By.cssSelector("input[type='email']");
    private final By passwordInput = By.cssSelector("input[type='password']");
    private final By signUpButton = By.cssSelector("button[type='submit']");
    // Firebase error rendered inline on a failed signup (same .error-message as login).
    private final By errorMessage = By.cssSelector(".error-message");

    public SignupPage(WebDriver driver) {
        super(driver);
    }

    /** Opens the client signup page directly (SPA deep-link). */
    public SignupPage open() {
        String url = ConfigReader.clientBaseUrl().replaceAll("/+$", "") + SIGNUP_PATH;
        log.info("Opening signup at {}", url);
        driver.get(url);
        wait.waitForVisible(fullNameInput);
        return this;
    }

    /** Fills the form and submits. Does not assert the outcome. */
    public void register(String fullName, String email, String password) {
        assertResolvesToQaAccount(email);
        wait.waitForVisible(fullNameInput).clear();
        driver.findElement(fullNameInput).sendKeys(fullName);
        driver.findElement(emailInput).clear();
        driver.findElement(emailInput).sendKeys(email);
        driver.findElement(passwordInput).clear();
        driver.findElement(passwordInput).sendKeys(password);
        log.info("Submitting signup for {}", email);
        wait.waitForClickable(signUpButton).click();
    }

    /**
     * Attempts to register using the already-registered QA client email (from config),
     * expecting rejection. Idempotent — no account is created on a duplicate email.
     */
    public void registerWithExistingClientEmail() {
        register("QA Duplicate Check", ConfigReader.clientEmail(), VALID_PASSWORD);
    }

    /** True if the browser is still on the signup route (i.e. registration did not proceed). */
    public boolean isOnSignupPage() {
        return driver.getCurrentUrl().contains(SIGNUP_PATH);
    }

    /** Waits for the inline signup error to appear and returns its (trimmed) text. */
    public String waitForErrorMessage() {
        return wait.waitForVisible(errorMessage).getText().trim();
    }

    /**
     * Safety guard against silently creating a real account with a wrong email: every
     * submitted signup email must resolve (Gmail plus/dot normalization) to the configured
     * QA account. This allows the QA email itself AND its plus-addressed variants — the
     * sanctioned dynamic-email space for AUTH-001/003 — while rejecting any unrelated
     * address, which would create a real account.
     *
     * <p>Assumes dynamic registration emails are plus-variants of the QA Gmail account; if
     * that base ever differs, revisit this check.
     */
    private void assertResolvesToQaAccount(String email) {
        String qa = ConfigReader.clientEmail();
        if (!normalizeGmail(email).equals(normalizeGmail(qa))) {
            throw new IllegalStateException(
                    "Refusing to submit signup with '" + email + "': it does not resolve to the "
                    + "configured QA account (VKAI_CLIENT_EMAIL). Only the QA email or a Gmail "
                    + "plus-addressed variant of it is allowed — any other address would create "
                    + "a REAL account.");
        }
    }

    private static String normalizeGmail(String email) {
        String e = email.trim().toLowerCase();
        int at = e.indexOf('@');
        if (at < 0) {
            return e;
        }
        String local = e.substring(0, at);
        String domain = e.substring(at + 1);
        int plus = local.indexOf('+');
        if (plus >= 0) {
            local = local.substring(0, plus);            // drop the +tag
        }
        if (domain.equals("gmail.com") || domain.equals("googlemail.com")) {
            local = local.replace(".", "");              // Gmail ignores dots
        }
        return local + "@" + domain;
    }
}
