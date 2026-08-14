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

    @Given("the client signup page is open")
    public void the_client_signup_page_is_open() {
        context.signupPage().open();
    }

    @When("the customer registers with the already-registered client email")
    public void the_customer_registers_with_the_already_registered_client_email() {
        context.signupPage().registerWithExistingClientEmail();
    }

    @When("the customer registers as a new user")
    public void the_customer_registers_as_a_new_user() {
        context.signupPage().registerAsNewUser();
    }

    @When("the customer enters the email {string}")
    public void the_customer_enters_the_email(String email) {
        context.signupPage().enterEmail(email);
    }

    @Then("the email should be rejected as invalid by the form")
    public void the_email_should_be_rejected_as_invalid_by_the_form() {
        assertThat(context.signupPage().isEmailFormatValid())
                .as("email should fail the form's native format validation")
                .isFalse();
    }

    @Then("registration should be rejected with the error {string}")
    public void registration_should_be_rejected_with_the_error(String expectedMessage) {
        assertThat(context.signupPage().waitForErrorMessage())
                .as("inline signup error text")
                .isEqualTo(expectedMessage);
    }

    @Then("the customer should remain on the signup page")
    public void the_customer_should_remain_on_the_signup_page() {
        assertThat(context.signupPage().isOnSignupPage())
                .as("expected to stay on /signup after a rejected registration")
                .isTrue();
    }

    // --- Login page presentation (VKAI-005 / VJS-TC-AUTH-006) ---

    @Then("the login heading should read {string}")
    public void the_login_heading_should_read(String expected) {
        assertThat(context.clientLoginPage().loginHeadingText())
                .as("login card heading text")
                .isEqualTo(expected);
    }

    @Then("the login heading should be horizontally centered")
    public void the_login_heading_should_be_horizontally_centered() {
        assertThat(context.clientLoginPage().isLoginHeadingCentered())
                .as("login heading should render horizontally centered (computed text-align)")
                .isTrue();
    }

    @Then("the text {string} should not be present on the login page")
    public void the_text_should_not_be_present_on_the_login_page(String text) {
        assertThat(context.clientLoginPage().isTextPresent(text))
                .as("text '%s' should be absent from the login page", text)
                .isFalse();
    }
}
