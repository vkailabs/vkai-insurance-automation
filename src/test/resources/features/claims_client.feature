@Regression @Client
Feature: Claims Filing & Tracking
  As a customer
  I want claim filing gated to active policies
  So that I cannot file a claim on a policy that is not active

  # VJS-TC-CLM-003  (Jira VJS-24)
  # Re-framed like PREM-002: a pending policy exposes NO "File a claim" control at all
  # (the pending policy-card DOM has actions = "View details" only, and its Claims
  # section reads "No claims filed."). Claims are an active-policy action
  # (business-flows §2.5). Reuses the enrollment step and the generic control-absence
  # assertion — no new page-object or step code.
  @Negative @VJS-24
  Scenario: Customer cannot file a claim on a pending policy
    Given the customer is logged in
    And the customer enrolls in the "BHP - Basic Health Plan" plan
    Then no "File a claim" control should be available for the "BHP - Basic Health Plan" policy
