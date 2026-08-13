package com.vkailabs.insurance.automation.stepdefinitions;

import static org.assertj.core.api.Assertions.assertThat;

import com.vkailabs.insurance.automation.context.TestContext;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

/**
 * Step definitions for authentication scenarios. Reusable across modules: any scenario
 * that needs a logged-in client reuses "the customer logs in with valid client credentials".
 *
 * <p>State is shared via the PicoContainer-injected {@link TestContext}, not statics.
 */
public class AuthSteps {

    private final TestContext context;

    public AuthSteps(TestContext context) {
        this.context = context;
    }

    @Given("the client login page is open")
    public void the_client_login_page_is_open() {
        context.clientLoginPage().open();
    }

    /** Reusable precondition for any scenario that needs an authenticated customer. */
    @Given("the customer is logged in")
    public void the_customer_is_logged_in() {
        context.clientLoginPage().open();
        context.clientLoginPage().loginWithConfiguredCredentials();
        assertThat(context.dashboardPage().isLoaded())
                .as("expected the client dashboard to load after logging in")
                .isTrue();
    }

    @When("the customer logs in with valid client credentials")
    public void the_customer_logs_in_with_valid_client_credentials() {
        context.clientLoginPage().loginWithConfiguredCredentials();
    }

    @Then("the customer should land on the client dashboard")
    public void the_customer_should_land_on_the_client_dashboard() {
        assertThat(context.dashboardPage().isLoaded())
                .as("expected the authenticated client dashboard to load after login")
                .isTrue();
    }

    @When("the customer logs in with an incorrect password")
    public void the_customer_logs_in_with_an_incorrect_password() {
        context.clientLoginPage().loginWithInvalidPassword();
    }

    @Then("an authentication error {string} should be displayed")
    public void an_authentication_error_should_be_displayed(String expectedMessage) {
        assertThat(context.clientLoginPage().waitForErrorMessage())
                .as("inline login error text")
                .isEqualTo(expectedMessage);
    }

    @Then("the customer should remain on the login page")
    public void the_customer_should_remain_on_the_login_page() {
        assertThat(context.clientLoginPage().isOnLoginPage())
                .as("expected to stay on /login after a failed login")
                .isTrue();
    }
}
