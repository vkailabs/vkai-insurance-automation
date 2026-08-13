package com.vkailabs.insurance.automation.hooks;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vkailabs.insurance.automation.context.TestContext;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;

/**
 * Driver lifecycle for every scenario. PicoContainer injects the same {@link TestContext}
 * here and into the step classes, so teardown quits the one shared driver.
 *
 * <p>On failure, a screenshot is attached to the Cucumber scenario; the ExtentReports
 * adapter embeds scenario attachments in the report automatically.
 */
public class Hooks {

    private static final Logger log = LoggerFactory.getLogger(Hooks.class);

    private final TestContext context;

    public Hooks(TestContext context) {
        this.context = context;
    }

    @Before
    public void beforeScenario(Scenario scenario) {
        log.info("=== START: {} ===", scenario.getName());
    }

    @After
    public void afterScenario(Scenario scenario) {
        try {
            if (scenario.isFailed() && context.hasDriver()) {
                byte[] png = ((TakesScreenshot) context.getDriver()).getScreenshotAs(OutputType.BYTES);
                scenario.attach(png, "image/png", scenario.getName());
                log.info("Attached failure screenshot for '{}'", scenario.getName());
            }
        } catch (Exception e) {
            log.warn("Could not capture failure screenshot: {}", e.getMessage());
        } finally {
            context.quit();
            log.info("=== END: {} [{}] ===", scenario.getName(), scenario.getStatus());
        }
    }
}
