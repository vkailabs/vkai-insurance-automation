package com.vkailabs.insurance.automation.stepdefinitions;

import static org.assertj.core.api.Assertions.assertThat;

import com.vkailabs.insurance.automation.context.TestContext;
import com.vkailabs.insurance.automation.pages.DashboardPage;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

/**
 * Step definitions for pending-policy cancellation (VKAI-010 / VJS-49).
 *
 * <p>Covers the client-observable, non-destructive parts of the feature: the Cancel button is
 * present on Pending cards (CANCEL-001) and absent on Active cards (CANCEL-002), and the Cancel
 * action is gated by a native confirmation dialog whose dismiss path is a safe no-op
 * (CANCEL-003). The destructive accept path (CANCEL-004) is held {@code @Manual} in the feature
 * file and has no step definitions here — accepting the confirm performs a terminal cancel
 * against the live QA account, and the resulting "Cancelled" status is hidden from the client
 * dashboard anyway (surfaced only on the provider portal, out of scope).
 *
 * <p>State is shared via the PicoContainer-injected {@link TestContext}.
 */
public class CancellationSteps {

    private static final String PENDING_COUNT_BEFORE = "cancel.pendingCountBefore";
    private static final String CONFIRM_TEXT_SEEN = "cancel.confirmTextSeen";

    private final TestContext context;

    public CancellationSteps(TestContext context) {
        this.context = context;
    }

    @Then("the {string} section should contain at least one policy card")
    public void the_section_should_contain_at_least_one_policy_card(String title) {
        assertThat(context.dashboardPage().cardCountInSection(title))
                .as("'%s' section should render at least one policy card", title)
                .isGreaterThan(0);
    }

    @Then("every policy card in the {string} section should show a Cancel button")
    public void every_policy_card_in_the_section_should_show_a_cancel_button(String title) {
        DashboardPage dashboard = context.dashboardPage();
        int cards = dashboard.cardCountInSection(title);
        assertThat(cards)
                .as("'%s' section should render at least one policy card to assert against", title)
                .isGreaterThan(0);
        assertThat(dashboard.cancelButtonCountInSection(title))
                .as("every one of the %d card(s) in the '%s' section should expose a Cancel button",
                        cards, title)
                .isEqualTo(cards);
    }

    @Then("no policy card in the {string} section should show a Cancel button")
    public void no_policy_card_in_the_section_should_show_a_cancel_button(String title) {
        assertThat(context.dashboardPage().cancelButtonCountInSection(title))
                .as("no policy card in the '%s' section should expose a Cancel button", title)
                .isZero();
    }

    @When("the customer clicks Cancel on a pending policy and dismisses the confirmation dialog")
    public void the_customer_clicks_cancel_on_a_pending_policy_and_dismisses_the_confirmation_dialog() {
        DashboardPage dashboard = context.dashboardPage();
        // Record the Pending count first so we can prove the dismiss path mutated nothing.
        context.put(PENDING_COUNT_BEFORE, dashboard.summaryCount("Pending"));
        String confirmText = dashboard.clickFirstPendingCancelAndReadConfirm();
        context.put(CONFIRM_TEXT_SEEN, confirmText);
        // Dismiss = "no" to window.confirm: no cancel API call, no mutation.
        dashboard.dismissOpenConfirm();
    }

    @Then("a cancellation confirmation dialog reading {string} should have been shown")
    public void a_cancellation_confirmation_dialog_reading_should_have_been_shown(String expected) {
        assertThat((String) context.get(CONFIRM_TEXT_SEEN))
                .as("native confirmation dialog copy for cancelling a pending policy")
                .isEqualTo(expected);
    }

    @Then("the {string} policy count should be unchanged")
    public void the_policy_count_should_be_unchanged(String label) {
        int before = (int) context.get(PENDING_COUNT_BEFORE);
        assertThat(context.dashboardPage().summaryCount(label))
                .as("'%s' count should be unchanged after dismissing the cancel confirmation", label)
                .isEqualTo(before);
    }
}
