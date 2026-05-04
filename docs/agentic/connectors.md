# Connectors

## Purpose
Connectors integrate external acquirers, banks, or providers through a strict contract.

## Connector contract
- authorize()
- capture()
- refund()
- void()

## Connector responsibilities
- Request mapping
- API invocation
- Response mapping

## Connector anti-patterns
- Business logic in adapters
- Hidden retries without policy visibility
- Provider-specific schema leaked to public API contracts

## Reliability expectations
- Timeouts configured per operation.
- Correlation IDs propagated across calls.
- Deterministic error mapping.
