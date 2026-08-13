# Regression Pack Automation — Triage

40 scenarios total (Jira `VJS-2`…`VJS-41`). This maps each against the known constraints
so batches can be planned. Keep in sync as scenarios go green or constraints change.

## Constraints in play
- **Active/expired-policy precondition** — the client UI only produces a *pending* policy;
  *active* and *expired* states need a test fixture (see
  [provider-fixture-endpoints-spec.md](provider-fixture-endpoints-spec.md)).
- **Dynamic-email** — registration needs a unique email per run → **Gmail plus-addressing**
  (`vkailabs+<unique>@gmail.com`).
- **Entra/MFA** — provider Approver/Reviewer actions stay **manual** (decided; tenant Security
  Defaults + no MFA method on the QA account block automation).
- **Backend-only** — sync-internals scenarios have no meaningful UI surface → manual.

## Green (5)
`VJS-5` AUTH-004 · `VJS-6` AUTH-005 · `VJS-13` ENR-001 · `VJS-19` PREM-002 ·
`VJS-40` E2E-001 (automatable slice).

## ① Automatable now — no new blockers (3)
| Jira | ID | Scenario |
|---|---|---|
| VJS-10 | CAT-001 | Customer views catalog (name/premium/coverage per plan) |
| VJS-24 | CLM-003 | Cannot file a claim on a **pending** policy (mirrors PREM-002) |
| VJS-3 | AUTH-002 | Register with an already-registered email → rejected (idempotent; reuses QA email) |

## ② Blocked — active/expired-policy fixture (9)
| Jira | ID | Needs |
|---|---|---|
| VJS-18 | PREM-001 | active |
| VJS-20 | PREM-003 | active (premium boundary) |
| VJS-22 | CLM-001 | active |
| VJS-25 | CLM-004 | active (claim-vs-coverage boundary) |
| VJS-26 | CLM-005 | active (reach claim form) |
| VJS-34 | REN-001 | active + near expiry |
| VJS-36 | DASH-001 | active + a premium + a claim |
| VJS-23 | CLM-002 | **expired** (date control) |
| VJS-35 | REN-002 | active at **specific expiry offsets** (date control) |

## ③ Blocked — dynamic-email (plus-addressing) (2)
| Jira | ID | Note |
|---|---|---|
| VJS-2 | AUTH-001 | Register success (creates an account) |
| VJS-4 | AUTH-003 | Email-format outline — only the `accepted` row creates an account; `rejected` rows could split into ① |

## ④ Permanently manual — as decided (21)
**Provider Entra actions (15):** `VJS-7/8/9` AUTH-006/007/008 · `VJS-12` CAT-003 ·
`VJS-14` ENR-002 · `VJS-17` ENR-005* · `VJS-21` PREM-004 · `VJS-27` CLM-006 ·
`VJS-28…33` CLM-007…012 · `VJS-41` E2E-002.
**Backend-only (6):** `VJS-11` CAT-002 · `VJS-15` ENR-003 · `VJS-16` ENR-004 ·
`VJS-37/38/39` SYNC-001/002/003.

## Flags
- Backend-only is really **5** sync scenarios (ENR-003/004 + SYNC-001/002/003) plus CAT-002.
- **ENR-005\*** is ambiguous: an *inactive* plan ("Basic Health Plan - 1") still showed on the
  client catalog with an Enroll button in a capture — either the client doesn't hide
  deactivated plans, or it's a stale-cache artifact (CAT-002 territory). Needs investigation
  before it's classifiable; parked in ④.
- The fixture unlocks **all of ②** (data-setup only — it does **not** let tests invoke Approver
  actions, which stay in ④).

## Build cadence
2–3 scenarios per batch, DOM-verified from live captures, stop for `mvn test` before continuing.

**Batch log**
- Batch 1: CAT-001, CLM-003 — done, merged to main.
- Batch 2: AUTH-002 — done (on dev).

Category ① is now fully automated (CAT-001, CLM-003, AUTH-002). Remaining work needs a
decision to land first: ② the fixture seam (9 scenarios), or ③ plus-addressing for
registration success/format (AUTH-001/003).
