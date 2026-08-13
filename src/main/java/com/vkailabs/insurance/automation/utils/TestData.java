package com.vkailabs.insurance.automation.utils;

import java.time.Instant;
import java.util.UUID;

/**
 * Test-data helpers.
 */
public final class TestData {

    private TestData() {
    }

    /**
     * A unique, valid email that resolves to the configured QA client account via Gmail
     * plus-addressing — e.g. {@code vkailabs+auto-1723...-a1b2@gmail.com}. Fresh on every
     * call, so registration treats it as a brand-new account, while all such mail still
     * lands in the QA inbox. It also passes {@code SignupPage}'s safety guard (a plus-tag
     * normalizes back to the QA account).
     *
     * <p>NOTE: using this for a successful signup creates a real Firebase account each run
     * (accepted trade-off — see {@code docs/TRIAGE.md}).
     */
    public static String uniqueClientEmail() {
        return plusTag(ConfigReader.clientEmail(), uniqueTag());
    }

    /** Inserts (or replaces) a Gmail plus-tag on {@code baseEmail}'s local part. */
    static String plusTag(String baseEmail, String tag) {
        int at = baseEmail.indexOf('@');
        if (at < 0) {
            return baseEmail;
        }
        String local = baseEmail.substring(0, at);
        String domain = baseEmail.substring(at + 1);
        int plus = local.indexOf('+');
        if (plus >= 0) {
            local = local.substring(0, plus);   // drop any existing +tag before adding ours
        }
        return local + "+" + tag + "@" + domain;
    }

    private static String uniqueTag() {
        return "auto-" + Instant.now().toEpochMilli() + "-"
                + UUID.randomUUID().toString().substring(0, 4);
    }
}
