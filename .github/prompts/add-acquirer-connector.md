# Prompt: Add Acquirer Connector

## Goal
Add a new card acquiring connector with mapping, webhook handling, retry policy, and tests.

## Inputs
- connector_name
- api_spec
- authentication_method
- webhook_spec

## Steps
1. Parse and normalize acquirer API specification.
2. Apply `acquiring-connector` skill.
3. Apply `webhook-handler` skill.
4. Apply `payment-retry` skill for connector failure handling.
5. Generate integration and certification-oriented tests.

## Expected output
- Connector implementation with authorize/capture/refund/void.
- Request/response mappers.
- Webhook verifier and event handler.
- Retry policy artifact and test suite.

## Definition of done
- Connector follows interface contract.
- No business logic in connector adapter.
- Financial integrity hook checklist passes.
