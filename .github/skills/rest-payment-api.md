# Skill: REST Payment API

## Use when
Implementing a payment API endpoint for authorize/capture/refund/void actions.

## Inputs
- Endpoint contract
- Request/response schema
- Action type

## Steps
1. Define endpoint and validation rules.
2. Map request to orchestrator command model.
3. Execute orchestrator action.
4. Map domain response to API response.
5. Return typed errors with stable codes.

## Required checks
- Request validation coverage.
- Audit event for write action.
- Idempotency integration for write operations.
