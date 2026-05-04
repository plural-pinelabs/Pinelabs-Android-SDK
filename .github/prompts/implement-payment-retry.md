# Prompt: Implement Payment Retry

## Goal
Add robust retry behavior for transient payment/connector failures.

## Inputs
- operation_name
- retryable_error_set
- max_attempts
- backoff_strategy

## Steps
1. Classify retryable vs non-retryable failures.
2. Apply `payment-retry` skill.
3. Add bounded backoff strategy and jitter.
4. Add terminal failure and escalation behavior.
5. Generate tests for retry and exhaustion paths.

## Expected output
- Retry policy module.
- Observability fields for attempts and outcomes.
- Regression tests for retry behavior.

## Definition of done
- No unbounded retries.
- Idempotency-safe retries only.
- Exhaustion path produces clear terminal state.
