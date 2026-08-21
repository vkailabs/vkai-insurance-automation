@Regression @Client
Feature: Pending Policy Cancellation
  As a customer with a policy still awaiting approval
  I want to cancel that Pending policy myself
  So that I can withdraw an enrolment before it becomes active

  # VKAI-010 — the client portal now lets a customer cancel a Pending policy before approval.
  # Live DOM (client commit 7f4b49d, per the client subagent's stable-DOM report — the same
  # grounding basis used for the VKAI-006 section split, to be re-verified on the live run):
  #   - A Pending policy card carries, inside its <div class="policy-card-actions">, a
  #     <button class="btn btn-danger btn-small policy-cancel-btn">Cancel</button>
  #     (label flips to "Cancelling…", disabled, while the request is in flight).
  #   - The Cancel button renders ONLY on pending cards — i.e. only inside the
  #     "Your Pending Policies" <section class="policy-section"> — never on Active/other cards.
  #   - Clicking it opens a native window.confirm:
  #     "Cancel this pending policy? This cannot be undone."
  #   - On accept, the cancel API is called and the policy is then HIDDEN from the client
  #     dashboard entirely (excluded from both sections and both counts — a deliberate client
  #     decision). The new "Cancelled" status pill is therefore never visible on the client;
  #     the authoritative "Cancelled" display lives on the provider portal (out of scope, §5).

  # VJS-TC-CANCEL-001  (VKAI-010 - new)
  # Scenario 1 of VJS-49 — non-mutating DOM assertion. Every Pending policy card exposes a
  # Cancel button. On the live QA account the Pending bucket is populated (Pending=54).
  @Positive @VJS-TC-CANCEL-001
  Scenario: A Pending policy card shows a Cancel button
    Given the customer is logged in
    Then the "Your Pending Policies" section should contain at least one policy card
    And every policy card in the "Your Pending Policies" section should show a Cancel button

  # VJS-TC-CANCEL-002  (VKAI-010 - new)
  # Scenario 3 of VJS-49 — non-mutating DOM assertion. Active (non-pending) policy cards must
  # NOT expose a Cancel button. On the live QA account the Active bucket is populated (Active=5).
  @Negative @VJS-TC-CANCEL-002
  Scenario: An Active policy card shows no Cancel button
    Given the customer is logged in
    Then the "Your Active Policies" section should contain at least one policy card
    And no policy card in the "Your Active Policies" section should show a Cancel button

  # VJS-TC-CANCEL-003  (VKAI-010 - new)
  # Scenario 2 of VJS-49, SAFE (non-destructive) half only. Clicking Cancel opens a native
  # confirmation gate; DISMISSING it performs no cancellation. This verifies the confirm gate,
  # its exact copy, and that the dismiss path is a safe no-op — without mutating any live
  # policy. The destructive accept path is held @Manual as VJS-TC-CANCEL-004 below.
  @Positive @VJS-TC-CANCEL-003
  Scenario: Cancelling a Pending policy is gated by a confirmation that can be safely dismissed
    Given the customer is logged in
    When the customer clicks Cancel on a pending policy and dismisses the confirmation dialog
    Then a cancellation confirmation dialog reading "Cancel this pending policy? This cannot be undone." should have been shown
    And the "Pending" policy count should be unchanged

  # VJS-TC-CANCEL-004  (VKAI-010 - authored, HELD / @Manual — destructive, not client-observable)
  # Scenario 2 of VJS-49, DESTRUCTIVE half. Accepting the confirmation actually cancels the
  # policy. Held @Manual (excluded from the "@Client and not @Manual" run), deliberately NO Jira
  # issue, for two independent reasons:
  #   (1) SAFETY: accepting the confirm fires a terminal, irreversible cancel against the live
  #       production QA account — unsafe to run repeatedly/unattended in CI.
  #   (2) NOT CLIENT-OBSERVABLE: cancelled policies are HIDDEN from the client dashboard by
  #       design, so a client-UI-only test cannot assert "status changes to Cancelled" — only
  #       disappearance/count-drop is observable, and the authoritative "Cancelled" display is
  #       on the provider portal (VJS-49 Scenario 4, Entra-gated, out of scope per §5).
  # This is a safety + observability limitation on genuinely-new client UI, NOT a member of the
  # permanently-deleted 30 (§8). If ever automated, do it against a freshly self-enrolled,
  # disposable policy — never a pre-existing one — and it still could only assert the
  # disappearance/count-drop half.
  @Boundary @Manual @VJS-TC-CANCEL-004
  Scenario: Confirming Cancel removes the policy from the dashboard and the Pending count
    Given the customer has just enrolled in a plan, creating a disposable Pending policy
    When the customer clicks Cancel on that policy and confirms the dialog
    Then that policy should no longer appear anywhere on the client dashboard
    And the "Pending" policy count should drop by one
    # And on the provider portal the policy shows "Cancelled" and can no longer be approved
    # (VJS-49 Scenario 4 — provider-side + cross-cloud sync, out of scope for this suite).
