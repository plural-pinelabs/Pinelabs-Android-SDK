# Hook: Financial Integrity Check

## Objective
Prevent changes that can break financial correctness.

## Mandatory verifications
- Idempotency is implemented for write paths.
- Payment state machine rules are respected.
- Retry strategy is defined and bounded.
- Audit logs exist for state and external interactions.
- Webhook security checks are present when applicable.

## Evidence expected
- Unit test references.
- Integration test references.
- Risk note for edge cases.

## Result
- PASS: all mandatory checks met.
- FAIL: any missing mandatory check blocks merge.
