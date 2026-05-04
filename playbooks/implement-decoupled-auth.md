# Playbook: Implement Decoupled Auth

## Objective
Add decoupled authentication flows while preserving payment lifecycle safety.

## Steps
1. Define decoupled auth state extension and rules.
2. Add initiation and completion APIs.
3. Add callback/webhook handling for auth outcome.
4. Ensure payment transition gating by auth result.
5. Add timeout and retry handling for pending auth states.
6. Add integration tests for success, timeout, rejection.

## Exit criteria
- No capture without successful auth completion.
- Pending and expired auth paths are deterministic.
- Audit events cover all auth transitions.
