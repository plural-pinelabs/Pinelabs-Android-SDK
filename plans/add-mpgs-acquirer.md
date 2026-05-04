# Plan: Add MPGS Acquirer

## Phase 1: Connector foundation
1. Create MPGS connector adapter and configuration.
2. Implement authorize/capture/refund/void mappings.

## Phase 2: Integration flow
1. Wire connector routing in orchestrator.
2. Add webhook event verification and parsing.

## Phase 3: Reliability and controls
1. Add retry policy for transient failures.
2. Add audit logging and observability fields.

## Phase 4: Validation
1. Add integration tests and certification fixtures.
2. Run financial integrity checks and review gates.
