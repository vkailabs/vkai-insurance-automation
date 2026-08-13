package com.vkailabs.insurance.automation.stepdefinitions;

import static org.assertj.core.api.Assertions.assertThat;

import com.vkailabs.insurance.automation.context.TestContext;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

/**
 * Step definitions for the policy catalog module. State is shared via the
 * PicoContainer-injected {@link TestContext}.
 */
public class CatalogSteps {

    private final TestContext context;

    public CatalogSteps(TestContext context) {
        this.context = context;
    }

    @When("the customer opens the policy catalog")
    public void the_customer_opens_the_policy_catalog() {
        context.catalogPage().open();
    }

    @Then("a list of plans is displayed, each showing name, premium, and coverage")
    public void a_list_of_plans_is_displayed_each_showing_name_premium_and_coverage() {
        assertThat(context.catalogPage().everyPlanShowsNamePremiumCoverage())
                .as("catalog shows at least one plan, each with name, premium, and coverage")
                .isTrue();
    }
}
