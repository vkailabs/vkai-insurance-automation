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

No further automation of **the deleted 30** is planned. This document records the final
triage of the original 40, not pending work — that decision stays closed.

## Extension log — genuinely-new client UI (per automation reference §9)

The scope-lock above concerns the **original 40** (10 kept, 30 deleted). It does **not**
freeze the suite against brand-new client-side UI that ships in later stories. Per §9 of
the automation reference, genuinely-new UI-only client scenarios may be added without
reopening the §8 deleted-30 decision. Additions:

- **VKAI-005 (2026-08-14):** `AUTH-006` (login heading "Client Portal", centered, "Welcome
  back" removed) and `DASH-001` (dashboard Active/Pending summary counts) — both built from
  live DOM captures and green live. `DASH-002` (zero-count boundary) authored but **held
  `@Manual`**: no reproducible zero-status state exists UI-only on the QA account (Active=5,
  Pending=54; activation is provider/Entra-side per §5, client UI can't delete policies).
  This is a data-state limitation, **not** a member of the deleted 30, and not a reopening
  of §8. Live automated count after VKAI-005: 12 scenarios / 14 execution rows.
