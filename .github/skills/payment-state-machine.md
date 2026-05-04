# Skill: Payment State Machine

## Use when
Adding or changing payment state behavior.

## Inputs
- Current payment state
- Requested action
- Available references (authorization_id/capture_id)

## Steps
1. Validate transition is allowed.
2. Validate required references for target state.
3. Apply transition atomically.
4. Emit audit event for transition.
5. Persist resulting state and metadata.

## Required checks
- Forbidden transitions rejected.
- Duplicate transitions are idempotent.
- All transitions are test-covered.
