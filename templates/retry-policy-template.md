# Retry Policy Template

## Policy metadata
- Operation name:
- Owner:
- Last reviewed:

## Error classification
- Retryable errors:
- Non-retryable errors:

## Retry strategy
- Max attempts:
- Backoff type:
- Jitter:
- Timeout per attempt:

## Exhaustion behavior
- Terminal status:
- Alert/escalation target:
- Dead-letter behavior:

## Observability
- attempt_count metric:
- latency and outcome metrics:
- structured log fields:

## Tests
- retries until success:
- retries exhausted:
- non-retryable fails fast:
