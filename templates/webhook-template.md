# Webhook Template

## Endpoint metadata
- Provider:
- Path:
- Event types:

## Verification
- Signature algorithm:
- Timestamp tolerance:
- Replay key:

## Handler flow
1. Validate signature and timestamp.
2. Parse and validate payload.
3. Reject replayed event IDs.
4. Map event to internal action.
5. Apply state update through orchestrator boundary.

## Observability
- Correlation IDs:
- Audit fields:

## Tests
- valid event:
- invalid signature:
- stale timestamp:
- replay event:
