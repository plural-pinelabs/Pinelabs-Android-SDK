# Playbook: Add New UPI Bank

## Objective
Integrate a new UPI banking provider flow.

## Steps
1. Review provider onboarding documentation.
2. Implement provider connector mappings.
3. Add UPI-specific request validation and response normalization.
4. Add callback/webhook event handling.
5. Add retry handling for transient UPI API failures.
6. Add sandbox certification tests.

## Exit criteria
- UPI transaction lifecycle tests pass.
- Failure and timeout handling verified.
- Security checks for callback handling pass.
