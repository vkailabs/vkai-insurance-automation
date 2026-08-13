@Regression @Client
Feature: Policy Catalog Browsing
  As a customer
  I want to browse the policy catalog
  So that I can choose a plan to enroll in

  # VJS-TC-CAT-001  (Jira VJS-10)
  @Positive @VJS-10
  Scenario: Customer views the policy catalog
    Given the customer is logged in
    When the customer opens the policy catalog
    Then a list of plans is displayed, each showing name, premium, and coverage
