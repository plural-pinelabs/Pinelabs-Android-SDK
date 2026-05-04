# Architecture Instruction

## Intent
This file defines mandatory architecture behavior for generated changes.

## Repository layout rules
- Domain logic belongs in SDK module (`ExpressCheckoutSdk`).
- Integration/demo UI belongs in `app` module.
- External provider code must be isolated in connector layers.

## Design rules
1. Keep orchestration separate from transport.
2. Keep business rules out of connector adapters.
3. Enforce explicit state transitions through one state manager.
4. Keep tokenization and security controls centralized.
5. Emit structured logs and audit events for critical transitions.

## Required for all feature PRs
- State machine impact analysis.
- Idempotency behavior.
- Retry strategy.
- Failure handling and rollback notes.
