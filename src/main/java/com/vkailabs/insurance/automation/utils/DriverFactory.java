package com.vkailabs.insurance.automation.utils;

import java.time.Duration;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates {@link WebDriver} instances for the suite.
 *
 * <p>Driver binaries are resolved by Selenium Manager (built into Selenium 4) — no
 * WebDriverManager dependency. Browser and headless mode are config-driven
 * ({@code VKAI_AUTOMATION_BROWSER}, {@code VKAI_AUTOMATION_HEADLESS}); the defaults
 * (headless Chrome) match how CI runs. Lifecycle (create/quit) is owned by the
 * Cucumber hooks and shared across step-def classes via PicoContainer, not here.
 */
public final class DriverFactory {

    private static final Logger log = LoggerFactory.getLogger(DriverFactory.class);

    private DriverFactory() {
    }

    public static WebDriver createDriver() {
        String browser = ConfigReader.browser().toLowerCase();
        boolean headless = ConfigReader.headless();
        log.info("Creating WebDriver: browser={}, headless={}", browser, headless);

        WebDriver driver;
        switch (browser) {
            case "firefox":
                driver = createFirefox(headless);
                break;
            case "chrome":
                driver = createChrome(headless);
                break;
            default:
                log.warn("Unknown browser '{}' — falling back to chrome", browser);
                driver = createChrome(headless);
                break;
        }

        driver.manage().timeouts()
                .implicitlyWait(Duration.ofSeconds(ConfigReader.implicitWaitSeconds()));
        if (!headless) {
            driver.manage().window().maximize();
        }
        return driver;
    }

    private static WebDriver createChrome(boolean headless) {
        ChromeOptions options = new ChromeOptions();
        if (headless) {
            options.addArguments("--headless=new");
        }
        options.addArguments(
                "--window-size=1920,1080",
                "--no-sandbox",
                "--disable-dev-shm-usage",
                "--disable-gpu",
                "--remote-allow-origins=*");
        return new ChromeDriver(options);
    }

    private static WebDriver createFirefox(boolean headless) {
        FirefoxOptions options = new FirefoxOptions();
        if (headless) {
            options.addArguments("-headless");
        }
        options.addArguments("--width=1920", "--height=1080");
        return new FirefoxDriver(options);
    }
}
