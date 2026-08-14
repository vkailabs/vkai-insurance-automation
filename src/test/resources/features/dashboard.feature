@Regression @Client
Feature: Dashboard Policy Summary
  As a logged-in customer
  I want to see how many of my policies are Active vs Pending at a glance
  So that I can understand my coverage without scanning the full list

  # VJS-TC-DASH-001  (VKAI-005)
  # Dashboard summary block, derived client-side from the loaded policies, renders directly
  # above the policy list. Live DOM: <div class="policy-summary"> with one
  # <div class="summary-box"> per status, each holding <span class="summary-count"> and
  # <span class="summary-label"> ("Active" / "Pending"). This scenario verifies both boxes
  # render, sit above the list, are numeric, and match the rendered card counts.
  # VKAI-006 note: the old single "Your policies" heading was removed when the list was
  # split into two sections, so the positional assertion now references the first section
  # heading, "Your Active Policies" (the topmost policy-list heading).
  @Positive @VJS-TC-DASH-001
  Scenario: Dashboard shows Active and Pending policy count summaries above the policy list
    Given the customer is logged in
    Then a policy count summary for "Active" should be displayed above the "Your Active Policies" heading
    And a policy count summary for "Pending" should be displayed above the "Your Active Policies" heading
    And each policy count summary should show a numeric count
    And the "Active" summary count should equal the number of "Active" policy cards
    And the "Pending" summary count should equal the number of "Pending" policy cards

  # VJS-TC-DASH-003  (VKAI-006 - new)
  # The dashboard now splits enrolled policies into two sections instead of one list:
  # "Your Active Policies" (all non-pending) first, then "Your Pending Policies" (pending).
  # Live DOM: each section is a <section class="policy-section"> headed by an
  # <h2 class="section-title">; a populated section holds a <div class="card-grid"> of
  # <article class="policy-card"> cards (status via the status-<x> pill class). This
  # scenario verifies both section headings render in Active-then-Pending order and that
  # cards are bucketed correctly (no pending card under Active; only pending cards under
  # Pending). On the live QA account both sections are populated (Active=5, Pending=54).
  @Positive @VJS-TC-DASH-003
  Scenario: Dashboard splits policies into Active and Pending sections
    Given the customer is logged in
    Then a policy section titled "Your Active Policies" should be displayed
    And a policy section titled "Your Pending Policies" should be displayed
    And the "Your Active Policies" section should appear above the "Your Pending Policies" section
    And the "Your Active Policies" section should contain no policies with status "Pending"
    And the "Your Pending Policies" section should contain only policies with status "Pending"

  # VJS-TC-DASH-004  (VKAI-006 - authored, HELD / not currently live-verifiable)
  # Boundary: when a bucket has no policies its section still renders, showing a
  # <p class="policy-section-empty"> with exact text "No active policies." / "No pending
  # policies." (trailing period) instead of a card grid.
  #
  # NOT AUTOMATED-GREEN RIGHT NOW — tagged @Manual so it is excluded from the
  # "@Client and not @Manual" run. The live QA account currently has Active=5 and
  # Pending=54, so NEITHER bucket is empty, and an empty-bucket state is not reproducible
  # UI-only on this account (activation is provider/Entra-side = out of scope, and the
  # client UI cannot delete policies). Automating this honestly would require a reproducible
  # empty-bucket state AND a fresh live DOM capture of the empty-section paragraph before
  # writing locators. Do not fake an empty state. Same disposition as DASH-002 — a
  # data-state limitation, not the §5 Entra/architecture blocker; deliberately NO Jira issue.
  @Boundary @Manual @VJS-TC-DASH-004
  Scenario: An empty policy bucket still renders its section with an empty-state message
    Given a client account with no policies in one bucket (Active or Pending)
    When the customer views the dashboard
    Then that bucket's section should still render
    And it should show the empty-state message "No active policies." or "No pending policies."

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
