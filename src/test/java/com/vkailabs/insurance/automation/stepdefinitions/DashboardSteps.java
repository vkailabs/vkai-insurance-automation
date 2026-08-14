package com.vkailabs.insurance.automation.stepdefinitions;

import static org.assertj.core.api.Assertions.assertThat;

import com.vkailabs.insurance.automation.context.TestContext;
import com.vkailabs.insurance.automation.pages.DashboardPage;

import io.cucumber.java.en.Then;

/**
 * Step definitions for the dashboard policy views: the Active/Pending count boxes
 * (VKAI-005 / VJS-TC-DASH-001) and the Active vs Pending policy sections
 * (VKAI-006 / VJS-TC-DASH-003).
 *
 * <p>The count boxes are derived client-side from the loaded policies and render above the
 * "Your policies" heading. Since VKAI-006 the cards below that heading are grouped into two
 * "Your Active Policies" / "Your Pending Policies" sections (the heading itself is
 * unchanged). These steps assert the boxes are present, positioned above the heading,
 * numeric, and consistent with the rendered cards, and that the two sections render in the
 * right order with the right cards bucketed into each.
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

    // ---- Active vs Pending policy sections (VKAI-006 / VJS-TC-DASH-003) -----------

    @Then("a policy section titled {string} should be displayed")
    public void a_policy_section_titled_should_be_displayed(String title) {
        assertThat(context.dashboardPage().isPolicySectionDisplayed(title))
                .as("policy section headed '%s' should be displayed", title)
                .isTrue();
    }

    @Then("the {string} section should appear above the {string} section")
    public void the_section_should_appear_above_the_section(String firstTitle, String secondTitle) {
        assertThat(context.dashboardPage().isSectionAboveSection(firstTitle, secondTitle))
                .as("'%s' section should render above the '%s' section", firstTitle, secondTitle)
                .isTrue();
    }

    @Then("the {string} section should contain no policies with status {string}")
    public void the_section_should_contain_no_policies_with_status(String title, String status) {
        assertThat(context.dashboardPage().cardCountInSectionByStatus(title, status))
                .as("'%s' section should contain no '%s' policy cards", title, status)
                .isZero();
    }

    @Then("the {string} section should contain only policies with status {string}")
    public void the_section_should_contain_only_policies_with_status(String title, String status) {
        DashboardPage dashboard = context.dashboardPage();
        int total = dashboard.cardCountInSection(title);
        // On the live QA account this bucket is populated (Pending=54), so require >=1 and
        // that every card in the section carries the expected status. If it were empty this
        // would be a vacuous pass, which is why the empty-section case is held @Manual.
        assertThat(total)
                .as("'%s' section should render at least one policy card", title)
                .isGreaterThan(0);
        assertThat(dashboard.cardCountInSectionByStatus(title, status))
                .as("every policy card in the '%s' section should have status '%s'", title, status)
                .isEqualTo(total);
    }
}
