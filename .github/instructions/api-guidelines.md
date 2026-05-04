# API Guidelines Instruction

## API behavior
- Keep endpoint names explicit by payment action.
- Validate request payloads before invoking orchestration.
- Return deterministic response schema and typed errors.

## Write path requirements
- Accept idempotency key for create/update operations.
- Use request fingerprinting for duplicate detection.
- Return prior result for replayed idempotent request.

## Response rules
- Include stable identifiers (`payment_id`, `order_id`, `attempt_id` when applicable).
- Include status and next allowed actions.
- Avoid leaking connector-specific fields in public contracts.

## Versioning
- Preserve backward compatibility for non-breaking changes.
- Introduce versioned contracts for breaking changes.
