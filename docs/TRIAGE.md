# Regression Pack Automation — Final Scope (CLOSED)

**Status: locked — this is a closed decision, not a roadmap.** The regression pack is
permanently scoped to the **10 automated scenarios** below. The other 30 scenarios were
**deleted from Jira** — removed, not descoped or deferred. The former fixture-seam plan
and "category ② unlock" are **closed and will not be pursued.**

## The locked scope — 10 scenarios (all green, verified live)
| Jira | ID | Scenario |
|---|---|---|
| VJS-2  | AUTH-001 | New customer registers successfully |
| VJS-3  | AUTH-002 | Registration rejected — email already registered |
| VJS-4  | AUTH-003 | Registration rejects malformed email formats (client-side) |
| VJS-5  | AUTH-004 | Registered customer logs in |
| VJS-6  | AUTH-005 | Login fails with an incorrect password |
| VJS-10 | CAT-001  | Customer views the policy catalog |
| VJS-13 | ENR-001  | Customer enrolls in a plan → Pending |
| VJS-19 | PREM-002 | Cannot pay a premium on a pending policy |
| VJS-24 | CLM-003  | Cannot file a claim on a pending policy |
| VJS-40 | E2E-001  | Enroll → Pending → premium blocked (automatable slice) |

Runner filter: `@Client and not @Manual`. AUTH-003 is a Scenario Outline (3 example rows),
so `mvn test` reports **12 invocations** across these 10 scenarios.

## Accepted trade-offs (option A — no cleanup)
- Enrollment scenarios leave real **pending policies** on the live dev DB each run.
- **AUTH-001 creates a real Firebase account each run** (a fresh Gmail plus-addressed
  variant of the QA client account). No teardown.

## Why the other 30 were cut (historical context — closed)
Kept only so the decision is self-explanatory later. The removed scenarios fell into groups
not worth automating here:
- **Needed an active/expired policy** the client UI can't produce — would have required a
  provider-side test fixture. That plan
  ([provider-fixture-endpoints-spec.md](provider-fixture-endpoints-spec.md) — now obsolete)
  is **not being built.**
- **Provider Approver/Reviewer actions** — blocked by Entra ID + tenant MFA; stay manual.
- **Backend-only sync internals** — no meaningful UI surface.

No further automation is planned. This document records the final scope, not pending work.
