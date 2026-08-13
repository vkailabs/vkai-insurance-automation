@Regression
Feature: Premium Payment
  As a customer
  I want premium payment gated to active policies
  So that I cannot pay on a policy that is not active yet

  # VJS-TC-PREM-002  (Jira VJS-19)
  # Re-framed after live DOM investigation: a pending policy exposes NO "Pay premium"
  # control at all (confirmed absent even under "View details") — the block is the
  # absence of the control, not a disabled button. Matches business-flows §2.4.
  # Enrolls first (reusing the enrollment step) to create the pending policy under test.
  @Client @Negative @VJS-19
  Scenario: Customer cannot pay a premium on a pending policy
    Given the customer is logged in
    And the customer enrolls in the "BHP - Basic Health Plan" plan
    Then no "Pay premium" control should be available for the "BHP - Basic Health Plan" policy
