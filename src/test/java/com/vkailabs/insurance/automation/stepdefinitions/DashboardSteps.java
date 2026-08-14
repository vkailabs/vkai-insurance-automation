package com.vkailabs.insurance.automation.stepdefinitions;

import static org.assertj.core.api.Assertions.assertThat;

import com.vkailabs.insurance.automation.context.TestContext;
import com.vkailabs.insurance.automation.pages.DashboardPage;

import io.cucumber.java.en.Then;

/**
 * Step definitions for the dashboard policy-summary boxes (VKAI-005 / VJS-TC-DASH-001).
 *
 * <p>The Active/Pending count boxes are derived client-side from the loaded policies and
 * render directly above the "Your policies" heading. These steps assert the boxes are
 * present, positioned above the heading, numeric, and consistent with the rendered cards.
 * State is shared via the PicoContainer-injected {@link TestContext}.
 */
public class DashboardSteps {

    private final TestContext context;

    public DashboardSteps(TestContext context) {
        this.context = context;
    }

    @Then("a policy count summary for {string} should be displayed above the {string} heading")
    public void a_policy_count_summary_should_be_displayed_above_the_heading(String label, String heading) {
        DashboardPage dashboard = context.dashboardPage();
        assertThat(dashboard.isSummaryDisplayed(label))
                .as("'%s' policy summary box should be displayed", label)
                .isTrue();
        assertThat(dashboard.isSummaryAboveHeading(label, heading))
                .as("'%s' summary box should render above the '%s' heading", label, heading)
                .isTrue();
    }

    @Then("each policy count summary should show a numeric count")
    public void each_policy_count_summary_should_show_a_numeric_count() {
        DashboardPage dashboard = context.dashboardPage();
        // summaryCount parses the span's text as an int, so a non-numeric value would throw;
        // asserting >= 0 confirms both boxes hold a valid whole-number count.
        assertThat(dashboard.summaryCount("Active"))
                .as("Active summary count").isGreaterThanOrEqualTo(0);
        assertThat(dashboard.summaryCount("Pending"))
                .as("Pending summary count").isGreaterThanOrEqualTo(0);
    }

    @Then("the {string} summary count should equal the number of {string} policy cards")
    public void the_summary_count_should_equal_the_number_of_policy_cards(String label, String status) {
        DashboardPage dashboard = context.dashboardPage();
        assertThat(dashboard.summaryCount(label))
                .as("'%s' summary count should equal the number of rendered '%s' policy cards",
                        label, status)
                .isEqualTo(dashboard.policyCardCountByStatus(status));
    }
}
