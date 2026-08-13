@Regression
Feature: Policy Enrollment & Activation
  As a customer
  I want to enroll in a policy plan
  So that I can be covered

  # VJS-TC-ENR-001  (Jira VJS-13)
  # The pack enrolls in "Basic Health Plan". The client UI prefixes the catalog key
  # (VKAI-002), so the on-screen title is "BHP - Basic Health Plan" — matched exactly
  # to disambiguate from the near-identical "Basic Health Plan - 1" plan.
  @Client @Positive @VJS-13
  Scenario: Customer enrolls in a policy plan
    Given the customer is logged in
    When the customer enrolls in the "BHP - Basic Health Plan" plan
    Then the policy should be listed on the dashboard as "Pending"
