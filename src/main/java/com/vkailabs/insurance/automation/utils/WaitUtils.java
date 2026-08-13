package com.vkailabs.insurance.automation.utils;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Thin wrapper over {@link WebDriverWait} for the explicit-wait strategy the suite
 * uses throughout (the driver is configured with no implicit wait). One instance is
 * created per Page Object, sharing that page's {@link WebDriver}.
 */
public class WaitUtils {

    private final WebDriverWait wait;

    public WaitUtils(WebDriver driver) {
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(ConfigReader.explicitWaitSeconds()));
    }

    public WebElement waitForVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public WebElement waitForClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    /** Waits until the current URL no longer contains {@code fragment} (e.g. leaving "/login"). */
    public boolean waitForUrlToNotContain(String fragment) {
        return wait.until(ExpectedConditions.not(ExpectedConditions.urlContains(fragment)));
    }

    public boolean waitForUrlToContain(String fragment) {
        return wait.until(ExpectedConditions.urlContains(fragment));
    }

    /** Waits until at least one element matching {@code locator} is present in the DOM. */
    public WebElement waitForPresence(By locator) {
        return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    /** Waits until no element matching {@code locator} is visible (e.g. the login form is gone). */
    public boolean waitForInvisible(By locator) {
        return wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }
}
