package com.vkailabs.insurance.automation.stepdefinitions;

import static org.assertj.core.api.Assertions.assertThat;

import com.vkailabs.insurance.automation.context.TestContext;

import io.cucumber.java.en.Then;

/**
 * Step definitions for the premium module. State is shared via the PicoContainer-injected
 * {@link TestContext}.
 */
public class PremiumSteps {

    private final TestContext context;

    public PremiumSteps(TestContext context) {
        this.context = context;
    }

    /**
     * Asserts a pending policy exposes no such control. Per the live DOM investigation,
     * a pending policy shows no "Pay premium" control at all (payment is an active-only
     * action, business-flows §2.4) — the block is the control's absence.
     */
    @Then("no {string} control should be available for the {string} policy")
    public void no_control_should_be_available_for_the_policy(String controlText, String planTitle) {
        context.dashboardPage().open();
        assertThat(context.dashboardPage().hasControlForPendingPolicy(controlText, planTitle))
                .as("'%s' control present on the pending '%s' policy", controlText, planTitle)
                .isFalse();
    }
}
