# Idempotency Guidelines Instruction

## Scope
All write paths must implement idempotency.

## Required behavior
1. Accept idempotency key header/field.
2. Compute deterministic request fingerprint.
3. Check prior key+fingerprint record.
4. Return stored response if record exists.
5. Persist result atomically after successful processing.

## Conflict behavior
- Same key + different fingerprint must return conflict error.
- Conflict must be logged as a warning with correlation ID.

## Storage behavior
- Set expiry/retention for idempotency records.
- Keep enough metadata for audit and debugging.

## Testing
- Replay returns same response.
- Conflict is rejected.
- Concurrent duplicate requests do not double-process.
