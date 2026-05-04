# Plan: Implement Decoupled Auth

## Phase 1
1. Define decoupled auth state model and transitions.
2. Add initiation and completion API contracts.

## Phase 2
1. Implement auth callback/webhook mapping.
2. Enforce state transition guards.

## Phase 3
1. Add timeout/retry strategy for pending auth.
2. Add observability and audit events.

## Phase 4
1. Add test matrix for approve/reject/timeout/replay.
2. Run governance and security checks.
