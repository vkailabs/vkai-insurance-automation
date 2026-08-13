package com.vkailabs.insurance.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vkailabs.insurance.automation.utils.ConfigReader;

/**
 * Page Object for the client portal login screen (Firebase email/password).
 *
 * <p>Locators are grounded in the live DOM as of the pilot build: the form exposes
 * no id/name/placeholder/data-testid, so we key off input type and the submit button.
 * If the app later adds test hooks (data-testid), tighten these selectors.
 */
public class ClientLoginPage extends BasePage {

    private static final Logger log = LoggerFactory.getLogger(ClientLoginPage.class);

    private static final String LOGIN_PATH = "/login";

    /** A deliberately invalid password for negative login tests (clearly not a real secret). */
    private static final String INVALID_PASSWORD = "vkai-automation-invalid-pw-000";

    private final By emailInput = By.cssSelector("input[type='email']");
    private final By passwordInput = By.cssSelector("input[type='password']");
    private final By signInButton = By.cssSelector("button[type='submit']");
    // Firebase auth error, rendered inline on failed login (role="alert").
    private final By errorMessage = By.cssSelector(".error-message");

    public ClientLoginPage(WebDriver driver) {
        super(driver);
    }

    /** Opens the client portal; the app redirects the root to {@code /login}. */
    public ClientLoginPage open() {
        String baseUrl = ConfigReader.clientBaseUrl();
        log.info("Opening client portal at {}", baseUrl);
        driver.get(baseUrl);
        wait.waitForVisible(emailInput);
        return this;
    }

    /** Enters credentials and submits. Does not assert the outcome. */
    public void loginAs(String email, String password) {
        wait.waitForVisible(emailInput).clear();
        driver.findElement(emailInput).sendKeys(email);
        driver.findElement(passwordInput).clear();
        driver.findElement(passwordInput).sendKeys(password);
        log.info("Submitting client login for {}", email);
        wait.waitForClickable(signInButton).click();
    }

    /** Convenience: log in with the configured QA client credentials. */
    public void loginWithConfiguredCredentials() {
        loginAs(ConfigReader.clientEmail(), ConfigReader.clientPassword());
    }

    /** Attempts login with the configured QA email but a deliberately wrong password. */
    public void loginWithInvalidPassword() {
        loginAs(ConfigReader.clientEmail(), INVALID_PASSWORD);
    }

    /**
     * Waits for authentication to complete: success is leaving the {@code /login} route
     * (Firebase validates asynchronously, then the SPA routes to the dashboard).
     * Returns true if we navigated away from the login page within the wait window.
     */
    public boolean waitUntilLoggedIn() {
        boolean left = wait.waitForUrlToNotContain(LOGIN_PATH);
        log.info("Post-login URL: {}", driver.getCurrentUrl());
        return left;
    }

    /** Waits for the inline auth error to appear and returns its (trimmed) text. */
    public String waitForErrorMessage() {
        return wait.waitForVisible(errorMessage).getText().trim();
    }

    /** True if the inline auth error is visible within the wait window, false otherwise. */
    public boolean isErrorDisplayed() {
        try {
            wait.waitForVisible(errorMessage);
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    /** True if the browser is still on the login route (i.e. login did not proceed). */
    public boolean isOnLoginPage() {
        return driver.getCurrentUrl().contains(LOGIN_PATH);
    }
}
