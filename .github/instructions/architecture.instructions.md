---
description: "Use when implementing or reviewing payments architecture rules, boundaries, and flow ownership."
applyTo: "ExpressCheckoutSdk/**"
---

# Architecture Instructions

## Core boundaries
- Keep payment orchestration logic in SDK domain services.
- Keep connector adapters focused on request/response mapping and transport.
- Do not place business decision logic inside connector implementations.

## Lifecycle controls
- Enforce explicit payment state transitions.
- Reject invalid transitions.
- Emit audit records for every state mutation.

## Reliability controls
- Write paths must support idempotency.
- Retries must be bounded and observable.
- Webhooks must pass signature and replay checks before processing.
