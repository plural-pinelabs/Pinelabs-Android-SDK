---
description: "Use when adding or changing payment flows to ensure required test coverage is generated."
applyTo: "ExpressCheckoutSdk/**"
---

# Testing Guidelines Instructions

## Required test types
- State transition tests.
- Idempotency replay and conflict tests.
- Connector mapping tests.
- Retry behavior tests.
- Webhook signature and replay tests.

## Regression expectation
- Any bug fix must include a regression test.
- Cover both success and failure paths for payment actions.
- Prefer deterministic tests without timing flakiness.
