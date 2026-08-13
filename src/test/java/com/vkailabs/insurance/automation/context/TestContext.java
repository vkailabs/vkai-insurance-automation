package com.vkailabs.insurance.automation.context;

import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.WebDriver;

import com.vkailabs.insurance.automation.pages.CatalogPage;
import com.vkailabs.insurance.automation.pages.ClientLoginPage;
import com.vkailabs.insurance.automation.pages.DashboardPage;
import com.vkailabs.insurance.automation.pages.SignupPage;
import com.vkailabs.insurance.automation.utils.DriverFactory;

/**
 * Per-scenario shared state, managed by PicoContainer.
 *
 * <p>Cucumber-PicoContainer creates exactly one instance per scenario and injects it
 * into {@code Hooks} and every step-definition class via their constructors. That is
 * how step classes stay small and reusable (one per module) yet share a single
 * {@link WebDriver} and scenario scratch data — no statics.
 *
 * <p>The driver is created lazily on first use and cached Page Objects hang off it,
 * so the same instances are reused within a scenario across step classes.
 */
public class TestContext {

    private WebDriver driver;
    private ClientLoginPage clientLoginPage;
    private DashboardPage dashboardPage;
    private CatalogPage catalogPage;
    private SignupPage signupPage;

    /** Free-form scratch data shared between steps within a scenario. */
    private final Map<String, Object> scenarioData = new HashMap<>();

    public WebDriver getDriver() {
        if (driver == null) {
            driver = DriverFactory.createDriver();
        }
        return driver;
    }

    /** True if a driver was actually created this scenario (used by hooks for teardown/screenshots). */
    public boolean hasDriver() {
        return driver != null;
    }

    public ClientLoginPage clientLoginPage() {
        if (clientLoginPage == null) {
            clientLoginPage = new ClientLoginPage(getDriver());
        }
        return clientLoginPage;
    }

    public DashboardPage dashboardPage() {
        if (dashboardPage == null) {
            dashboardPage = new DashboardPage(getDriver());
        }
        return dashboardPage;
    }

    public CatalogPage catalogPage() {
        if (catalogPage == null) {
            catalogPage = new CatalogPage(getDriver());
        }
        return catalogPage;
    }

    public SignupPage signupPage() {
        if (signupPage == null) {
            signupPage = new SignupPage(getDriver());
        }
        return signupPage;
    }

    public void put(String key, Object value) {
        scenarioData.put(key, value);
    }

    public Object get(String key) {
        return scenarioData.get(key);
    }

    /** Quits the driver (if any) and clears cached page/state. Called from @After. */
    public void quit() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
        clientLoginPage = null;
        dashboardPage = null;
        catalogPage = null;
        signupPage = null;
        scenarioData.clear();
    }
}
