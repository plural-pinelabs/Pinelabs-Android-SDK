# Architecture Rules for Payments Engineering

## Purpose
Define machine-readable architecture rules for AI-assisted implementation in this repository.

## Domain boundaries
- Payment orchestration lives in `ExpressCheckoutSdk/`.
- App-specific UI and test harness behavior lives in `app/`.
- Connector integrations must remain behind connector interfaces.

## Core principles
1. Keep orchestration logic separate from connector transport logic.
2. Enforce payment state transitions using explicit state machine rules.
3. Apply idempotency to every write path and external callback path.
4. Treat retries as policies with bounded attempts and observability.
5. Keep reconciliation deterministic and auditable.

## Canonical payment lifecycle
- CREATED
- AUTHORIZED
- CAPTURED
- FAILED
- REFUNDED

Transition constraints:
1. AUTHORIZED requires a successful authorization response.
2. CAPTURED requires a prior AUTHORIZED payment and authorization_id.
3. REFUNDED requires a prior CAPTURED payment and capture_id.
4. FAILED payments cannot move to CAPTURED.

## Connector contract
Every connector must expose:
- authorize(request)
- capture(request)
- refund(request)
- void(request)

Connector responsibilities:
- Map domain request to connector payload.
- Call external API.
- Map connector response to domain response.

Forbidden in connectors:
- Payment business decisions.
- Retry ownership outside connector-local transport retries.
- State machine transitions.

## Reliability and safety controls
- Idempotency keys required for create/update payment endpoints.
- Retries must use retry policy templates and include dead-letter behavior when exhausted.
- Webhooks must include signature validation and replay protection.
- Audit logs required for payment state changes and connector calls.

## Data and security controls
- No sensitive PAN/CVV persistence in logs or storage.
- Tokens must be scoped by merchant_id, customer_id, and device_id when applicable.
- Secrets must be injected via environment/config providers, never hardcoded.

## Testing contract
Minimum required tests for any payment feature:
- State transition unit tests.
- Connector mapping tests.
- Idempotency tests.
- Retry policy tests.
- Webhook verification tests.
- Regression tests for failure paths.
