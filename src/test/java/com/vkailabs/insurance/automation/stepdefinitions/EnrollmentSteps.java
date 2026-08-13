package com.vkailabs.insurance.automation.stepdefinitions;

import static org.assertj.core.api.Assertions.assertThat;

import com.vkailabs.insurance.automation.context.TestContext;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

/**
 * Step definitions for the enrollment module. Reused across enrollment, premium, and
 * E2E scenarios (any flow that needs a customer to enroll in a plan). State is shared
 * via the PicoContainer-injected {@link TestContext}.
 */
public class EnrollmentSteps {

    private static final String ENROLLED_PLAN = "enrolledPlan";

    private final TestContext context;

    public EnrollmentSteps(TestContext context) {
        this.context = context;
    }

    @When("the customer enrolls in the {string} plan")
    public void the_customer_enrolls_in_the_plan(String planTitle) {
        context.catalogPage().open();
        context.catalogPage().enrollInPlan(planTitle);
        context.put(ENROLLED_PLAN, planTitle);
    }

    @Then("the policy should be listed on the dashboard as {string}")
    public void the_policy_should_be_listed_on_the_dashboard_as(String status) {
        String planTitle = (String) context.get(ENROLLED_PLAN);
        context.dashboardPage().open();
        assertThat(context.dashboardPage().isPolicyListed(planTitle, status))
                .as("policy '%s' listed on the dashboard with status '%s'", planTitle, status)
                .isTrue();
    }
}
