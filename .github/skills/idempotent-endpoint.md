# Skill: Idempotent Endpoint

## Use when
Building or updating write endpoints and externally-triggered state updates.

## Inputs
- idempotency_key
- request_payload
- operation_name

## Steps
1. Read key from header or request field.
2. Build deterministic request fingerprint.
3. Query idempotency store by key.
4. Return stored response if key+fingerprint exists.
5. Reject with conflict if key exists with different fingerprint.
6. Process request and persist result atomically.

## Required checks
- Replay returns identical response.
- Conflict behavior tested.
- Concurrent duplicates do not double-process.
