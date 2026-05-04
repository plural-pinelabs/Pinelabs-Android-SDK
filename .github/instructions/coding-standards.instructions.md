---
description: "Use when generating Kotlin code, tests, and payment SDK changes with consistent style and safety checks."
applyTo: "{ExpressCheckoutSdk/**,app/**}"
---

# Coding Standards Instructions

## Kotlin style
- Prefer explicit names and small functions.
- Keep null-handling clear and deterministic.
- Use typed models instead of map-like dynamic payloads.

## Error handling
- Avoid swallowed exceptions.
- Map external failures to stable internal error types.
- Preserve correlation identifiers for diagnostics.

## Observability
- Use structured logging.
- Never log sensitive payment data.
- Add audit entries for financial state changes.
