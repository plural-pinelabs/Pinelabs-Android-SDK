# Prompt: Create Webhook Handler

## Goal
Create secure webhook ingestion flow for external payment events.

## Inputs
- provider_name
- signature_scheme
- event_schema
- state_mapping_rules

## Steps
1. Apply `webhook-handler` skill.
2. Implement signature and timestamp verification.
3. Add replay protection.
4. Map external event types to internal state actions.
5. Add tests for valid, invalid, stale, and replayed events.

## Expected output
- Webhook endpoint/handler.
- Event parser and state mapping logic.
- Verification and replay-protection modules.
- Integration tests.

## Definition of done
- Invalid signatures rejected.
- Replay attacks blocked.
- State transitions conform to lifecycle rules.
