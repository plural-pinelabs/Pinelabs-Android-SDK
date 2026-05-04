# System Overview

## Goal
Describe how payment-related components interact across this repository.

## Major components
- SDK orchestrator layer (`ExpressCheckoutSdk`)
- Connector adapter layer
- Webhook ingestion layer
- Retry and reconciliation support flows
- Integration app module (`app`)

## Data flow summary
1. Client initiates payment intent.
2. Orchestrator validates request and lifecycle rules.
3. Connector adapter calls external provider.
4. Response is normalized and persisted.
5. Webhooks and retries update state safely.

## Non-functional priorities
- Financial correctness
- Security and data minimization
- Observability and auditability
- Deterministic recovery behavior
