@Regression @E2E
Feature: End-to-End Journeys

  # VJS-TC-E2E-001  (Jira VJS-40) — RE-SCOPED
  #
  # Original journey: enroll -> Approver activates -> pay premium -> file claim ->
  # Approver approves -> mark paid -> dashboard shows the full history.
  #
  # The provider Approver actions require an Entra ID login, which is blocked by the
  # tenant's Security Defaults / MFA (the Approver QA account has no MFA method
  # registered, and ROPC is disallowed under enforced MFA). Decision: those steps stay
  # MANUAL / exploratory (option 4) — the same posture already taken for the @Sync
  # scenarios. The journey is split below into an automatable slice and a manual leg.

  # --- Automatable portion (runs in CI) ---
  # Enroll -> Pending -> premium payment is unavailable until the policy is active.
  @Client @Automatable @VJS-40
  Scenario: Customer enrolls and cannot pay until the policy is active
    Given the customer is logged in
    When the customer enrolls in the "BHP - Basic Health Plan" plan
    Then the policy should be listed on the dashboard as "Pending"
    And no "Pay premium" control should be available for the "BHP - Basic Health Plan" policy

  # --- Manual / exploratory portion (NOT automated; needs a provider Approver) ---
  # Documented here for traceability; excluded from the run via the @Manual tag. The
  # steps below intentionally have no step definitions — they are performed by hand.
  @Manual @Provider @VJS-40
  Scenario: Approver activates, then the customer pays, claims, and is paid out
    Given a pending enrollment exists (from the automatable portion above)
    When an Approver activates the policy in the provider portal
    And the customer pays the premium on the now-active policy
    And the customer files a claim
    And an Approver approves the claim and then marks it paid
    Then the customer's dashboard shows the full history: Active policy, premium paid, claim Paid
