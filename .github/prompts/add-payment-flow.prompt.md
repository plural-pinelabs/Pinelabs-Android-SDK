---
mode: "agent"
description: "Generate a new payment flow with lifecycle-safe orchestration, idempotency, and tests."
---

# Add Payment Flow

## Inputs
- flow_name
- action_type
- connector_name

## Steps
1. Define API and domain models.
2. Enforce lifecycle transition checks.
3. Add idempotency behavior for write operations.
4. Add connector mapping integration.
5. Add tests for success, failure, and duplicates.

## Output
- Updated flow implementation.
- Test coverage for core and edge paths.
