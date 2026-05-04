# Payment Lifecycle Instruction

## Canonical states
- CREATED
- AUTHORIZED
- CAPTURED
- FAILED
- REFUNDED

## Allowed transitions
- CREATED -> AUTHORIZED
- AUTHORIZED -> CAPTURED
- AUTHORIZED -> FAILED
- CAPTURED -> REFUNDED
- CREATED -> FAILED

## Forbidden transitions
- FAILED -> CAPTURED
- REFUNDED -> CAPTURED
- CREATED -> REFUNDED

## Rules
1. Capture requires existing `authorization_id`.
2. Refund requires existing `capture_id`.
3. Duplicate state transitions must be idempotent.
4. Every transition must emit an audit event.
