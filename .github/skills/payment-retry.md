# Skill: Payment Retry

## Use when
Implementing retries for transient external failures.

## Inputs
- Retryable error classes
- Max attempts
- Backoff strategy

## Steps
1. Classify errors into retryable and terminal.
2. Apply bounded retries with jitter.
3. Track attempt_count and elapsed_time.
4. Stop and mark terminal state on exhaustion.
5. Emit retry metrics and structured logs.

## Required checks
- No unbounded loops.
- Non-retryable errors fail fast.
- Exhaustion path is explicit and tested.
