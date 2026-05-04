# Payment Architecture

## Layering
- API/entry layer: validates and normalizes requests.
- Orchestration layer: owns lifecycle and business decisions.
- Connector layer: external API transport and mapping only.
- Persistence layer: payment state, idempotency, audit records.

## Core entities
- Payment
- Authorization
- Capture
- Refund
- Retry Attempt
- Audit Event

## Architectural guarantees
- One source of truth for payment state transitions.
- Connector abstraction hides provider-specific differences.
- Write operations are idempotent.
- State change events are auditable.
