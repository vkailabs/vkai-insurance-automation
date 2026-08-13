package com.vkailabs.insurance.automation.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Central configuration lookup for the automation suite.
 *
 * <p>Resolution order for every key:
 * <ol>
 *   <li>Environment variable (via {@link System#getenv}) — preferred, and what CI uses
 *       (values injected as GitHub Actions secrets).</li>
 *   <li>A gitignored {@code config/config-local.properties} on the test classpath —
 *       convenient for local runs. Copy {@code config-local.properties.example} to
 *       {@code config-local.properties} and fill in real QA values.</li>
 * </ol>
 *
 * <p>Keys are the same {@code UPPER_SNAKE_CASE} names in both places (per the
 * {@code VKAI_<COMPONENT>_<SETTING>} naming convention), so nothing needs mapping.
 * Real credentials live only in the environment or the gitignored file — never in git.
 */
public final class ConfigReader {

    private static final Logger log = LoggerFactory.getLogger(ConfigReader.class);

    private static final String LOCAL_CONFIG_RESOURCE = "/config/config-local.properties";

    // ---- Required keys (the six the pilot needs) ----------------------------
    public static final String CLIENT_BASE_URL   = "VKAI_CLIENT_BASE_URL";
    public static final String CLIENT_EMAIL      = "VKAI_CLIENT_EMAIL";
    public static final String CLIENT_PASSWORD   = "VKAI_CLIENT_PASSWORD";
    public static final String PROVIDER_BASE_URL = "VKAI_PROVIDER_BASE_URL";
    public static final String PROVIDER_EMAIL    = "VKAI_PROVIDER_EMAIL";
    public static final String PROVIDER_PASSWORD = "VKAI_PROVIDER_PASSWORD";

    // ---- Optional driver-tuning keys (sensible defaults if unset) -----------
    public static final String BROWSER            = "VKAI_AUTOMATION_BROWSER";
    public static final String HEADLESS           = "VKAI_AUTOMATION_HEADLESS";
    public static final String IMPLICIT_WAIT_SECS = "VKAI_AUTOMATION_IMPLICIT_WAIT_SECS";
    public static final String EXPLICIT_WAIT_SECS = "VKAI_AUTOMATION_EXPLICIT_WAIT_SECS";

    private static final Properties FILE_PROPS = new Properties();

    static {
        load();
    }

    private ConfigReader() {
    }

    private static void load() {
        try (InputStream in = ConfigReader.class.getResourceAsStream(LOCAL_CONFIG_RESOURCE)) {
            if (in != null) {
                FILE_PROPS.load(in);
                log.info("Loaded local config from classpath resource {}", LOCAL_CONFIG_RESOURCE);
            } else {
                log.info("No {} on classpath; resolving all config from environment variables only",
                        LOCAL_CONFIG_RESOURCE);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read " + LOCAL_CONFIG_RESOURCE, e);
        }
    }

    /** Returns the value for {@code key}, or {@code null} if unset in both env and file. */
    public static String get(String key) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            value = FILE_PROPS.getProperty(key);
        }
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    /** Returns the value for {@code key}, or {@code defaultValue} if unset. */
    public static String get(String key, String defaultValue) {
        String value = get(key);
        return value == null ? defaultValue : value;
    }

    /** Returns the value for {@code key}, or throws if it is unset in both env and file. */
    public static String getRequired(String key) {
        String value = get(key);
        if (value == null) {
            throw new IllegalStateException(
                    "Missing required config '" + key + "'. Set it as an environment variable, "
                    + "or add it to src/test/resources/config/config-local.properties "
                    + "(copy config-local.properties.example and fill in real QA values).");
        }
        return value;
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = get(key);
        return value == null ? defaultValue : Boolean.parseBoolean(value);
    }

    public static int getInt(String key, int defaultValue) {
        String value = get(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.warn("Config '{}' = '{}' is not an integer; using default {}", key, value, defaultValue);
            return defaultValue;
        }
    }

    // ---- Typed accessors ----------------------------------------------------

    public static String clientBaseUrl()   { return getRequired(CLIENT_BASE_URL); }
    public static String clientEmail()      { return getRequired(CLIENT_EMAIL); }
    public static String clientPassword()   { return getRequired(CLIENT_PASSWORD); }
    public static String providerBaseUrl()  { return getRequired(PROVIDER_BASE_URL); }
    public static String providerEmail()    { return getRequired(PROVIDER_EMAIL); }
    public static String providerPassword() { return getRequired(PROVIDER_PASSWORD); }

    public static String browser()          { return get(BROWSER, "chrome"); }
    public static boolean headless()        { return getBoolean(HEADLESS, true); }
    // Default 0: the suite uses explicit waits (WaitUtils); mixing an implicit wait
    // with explicit waits causes unpredictable timeouts, so implicit is off by default.
    public static int implicitWaitSeconds() { return getInt(IMPLICIT_WAIT_SECS, 0); }
    public static int explicitWaitSeconds() { return getInt(EXPLICIT_WAIT_SECS, 15); }
}
