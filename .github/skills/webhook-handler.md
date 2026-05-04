# Skill: Webhook Handler

## Use when
Building webhook ingestion for connector/acquirer events.

## Inputs
- Signature spec
- Event schema
- Mapping to internal actions

## Steps
1. Verify signature and timestamp window.
2. Validate payload schema.
3. Check replay key/event ID for duplicates.
4. Map event to internal state update command.
5. Execute state update via orchestrator boundary.

## Required checks
- Invalid signatures rejected.
- Replay events ignored with audit log.
- Transition mapping validated by state-machine rules.
