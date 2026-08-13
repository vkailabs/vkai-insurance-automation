package com.vkailabs.insurance.automation.pages;

import org.openqa.selenium.WebDriver;

import com.vkailabs.insurance.automation.utils.WaitUtils;

/**
 * Base for all Page Objects: holds the shared {@link WebDriver} and a {@link WaitUtils}
 * bound to it. Page Objects extend this and expose intent-level methods.
 */
public abstract class BasePage {

    protected final WebDriver driver;
    protected final WaitUtils wait;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WaitUtils(driver);
    }
}
