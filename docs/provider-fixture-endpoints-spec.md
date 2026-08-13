# Provider-API Test-Fixture Endpoints — Spec

> **OBSOLETE / CLOSED (2026-08-14).** The regression pack scope was locked at the 10
> automated scenarios and the other 30 were deleted from Jira. This fixture seam is
> **not being built** — the document is kept only as a historical record of what was
> designed. See [TRIAGE.md](TRIAGE.md).

**Status:** ~~proposed~~ **not pursued** (see notice above).
**Consumer:** was the `vkai-insurance-automation` suite (the now-removed active-policy scenarios).

## Purpose & scope
Let the automation suite set up **policy preconditions** the client UI can't produce on its own
— an `active` or `expired` policy, and a specific `expiry_date` — **without** invoking the
Entra-gated Approver actions. This unlocks the 9 active/expired-policy-blocked client scenarios.

**Strictly test-data setup.** This is NOT a way to exercise the Approver business actions.
`activate` / `approve` / `reject` / `mark-paid` and all claims transitions **remain manual** and
out of scope here.

## Hard gating (both required)
1. **Env flag** — mount the fixture router only when
   `VKAI_INSURANCE_PROVIDER_API_ENABLE_TEST_FIXTURES=true`. Absent/false ⇒ routes 404 (as if not
   present). **Never set in prod** (verify prod `docker-compose.yml` / CI env never sets it).
2. **Fixture secret** — require header `X-VKAI-Test-Fixture-Key`, compared to
   `VKAI_INSURANCE_PROVIDER_API_TEST_FIXTURE_KEY`. Distinct from the sync key and from Entra.
   Missing/wrong ⇒ 401.

Per repo convention, add both new env vars to **`.env.example` AND** the `api` service's
`environment:` block in `docker-compose.yml` (but leave them unset/false in prod).

## Endpoint

### `POST /v1/test-fixtures/policies/state`
Set a mirrored policy's status and/or expiry, and **propagate to the client** so the client UI
reflects it.

Request:
```json
{
  "client_policy_id": "5ea11f4b-bc2d-4fdb-84fe-1fd6639a486d",
  "status": "active",          // one of: pending | active | expired  (optional)
  "expiry_date": "2027-08-13"  // ISO date (optional); at least one of status/expiry_date required
}
```
- The suite obtains `client_policy_id` from the client dashboard card link
  (`<a href="/policies/<client_policy_id>">`), so lookup is by `clientPolicyId` (already stored on
  the provider's mirrored policy from `POST /v1/sync/policies`).

Behavior:
1. Find the provider `policies` row by `clientPolicyId` → 404 if none.
2. Update `status` and/or `expiryDate` on that row.
3. **Propagate to the client** (this is the important part — the client UI reads the *client* DB):
   - status → reuse the existing client callback the activate flow already uses:
     `POST /v1/sync/policies/status` (via `services/clientSync.js`).
   - expiry_date → push via the existing renewal path (`POST /v1/sync/policies`,
     `event_type: policy.renewed`), which the client already handles for renewals.
4. Respond `200` with the updated provider policy (and sync outcome).

Constraints:
- Idempotent — repeating the same call is safe.
- **Policy state only.** No claim transitions, no premium/claim creation (those come through the
  normal client UI). `status` limited to `pending|active|expired`.
- No `ops_user` attribution — it is not an ops action.

## Explicitly out of scope
- Any endpoint that performs or mimics an Approver **decision** on claims
  (review/approve/reject/mark-paid). Those stay manual.
- Creating policies/premiums/claims. Enrollment/payment/claim-filing continue via the client UI.
- Prod. Ever.

## Cross-repo flags
- The client UI reads the **client** DB, so propagation (step 3) is essential. Confirm the client's
  inbound handlers persist what we push — in particular that an `expiry_date` update lands
  client-side (the `policy.renewed` path should, but verify; a small `vkai-insurance-client-api`
  check may be needed).
- Automation side (separate, in this repo, once endpoints exist): add
  `VKAI_PROVIDER_FIXTURE_BASE_URL` + `VKAI_PROVIDER_FIXTURE_KEY` to `ConfigReader`, and a small
  HTTP client to call the fixture between UI steps. No Selenium involved for the fixture call.

## Security review checklist (for the provider-api session)
- [ ] Router mounted only under the env flag; default off.
- [ ] Secret required and compared safely.
- [ ] Prod env/compose/CI never set the flag or the key.
- [ ] Fixture calls logged distinctly (they bypass the normal flow).
- [ ] No claims-transition capability crept in.
