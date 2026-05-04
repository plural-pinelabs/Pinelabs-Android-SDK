# Prompt: Create Payment API

## Goal
Implement a payment API endpoint and orchestration path aligned with lifecycle and idempotency rules.

## Inputs
- api_name
- request_schema
- response_schema
- payment_action (authorize/capture/refund/void)

## Steps
1. Validate API contract and input schema.
2. Apply `rest-payment-api` skill.
3. Apply `payment-state-machine` skill for transition checks.
4. Apply `idempotent-endpoint` skill for write safety.
5. Generate tests for success/failure and duplicate requests.

## Expected output
- API handler/service updates.
- State transition logic updates.
- Idempotency integration.
- Unit and integration tests.

## Definition of done
- Lifecycle rules pass.
- Idempotency behavior verified.
- Security and audit requirements met.
