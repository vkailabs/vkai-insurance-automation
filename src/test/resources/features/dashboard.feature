@Regression @Client
Feature: Dashboard Policy Summary
  As a logged-in customer
  I want to see how many of my policies are Active vs Pending at a glance
  So that I can understand my coverage without scanning the full list

  # VJS-TC-DASH-001  (VKAI-005 — new)
  # New dashboard summary block, derived client-side from the loaded policies, renders
  # directly above the "Your policies" heading. Live DOM: <div class="policy-summary">
  # with one <div class="summary-box"> per status, each holding <span class="summary-count">
  # and <span class="summary-label"> ("Active" / "Pending"). This scenario verifies both
  # boxes render, sit above the heading, are numeric, and match the rendered card counts.
  @Positive @VJS-TC-DASH-001
  Scenario: Dashboard shows Active and Pending policy count summaries above the policy list
    Given the customer is logged in
    Then a policy count summary for "Active" should be displayed above the "Your policies" heading
    And a policy count summary for "Pending" should be displayed above the "Your policies" heading
    And each policy count summary should show a numeric count
    And the "Active" summary count should equal the number of "Active" policy cards
    And the "Pending" summary count should equal the number of "Pending" policy cards

  # VJS-TC-DASH-002  (VKAI-005 — authored, HELD / not currently live-verifiable)
  # Boundary: a status with zero policies should still render its box showing "0" (both
  # boxes always render once data is loaded), rather than being hidden.
  #
  # NOT AUTOMATED-GREEN RIGHT NOW — tagged @Manual so it is excluded from the
  # "@Client and not @Manual" run. The live QA account currently has Active=5 and
  # Pending=54, so NEITHER status is zero, and a genuine zero-count state is not
  # reproducible UI-only on this account (activation is provider/Entra-side = out of
  # scope, and the client UI cannot delete policies). Automating this honestly would
  # require a reproducible zero-status state (e.g. a freshly registered account with an
  # empty policy list) AND a fresh live DOM capture of that empty-dashboard state before
  # writing locators — neither exists yet. Do not fake a zero state. See automation
  # reference §7 for disposition.
  @Boundary @Manual @VJS-TC-DASH-002
  Scenario: Dashboard summary shows "0" for a status with no policies
    Given a client account with no policies of a given status
    When the customer views the dashboard
    Then that status's policy count summary should still render and display "0"
