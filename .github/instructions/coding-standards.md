# Coding Standards Instruction

## Language and style
- Prefer Kotlin-first implementation for SDK changes.
- Use explicit names; avoid ambiguous abbreviations.
- Keep functions small and side effects explicit.

## Error handling
- Do not swallow exceptions.
- Map external errors to typed internal errors.
- Preserve failure reason and traceability fields.

## Observability
- Emit structured logs with correlation identifiers.
- Never log sensitive payment data.
- Include key decision points in audit logs.

## Testing requirements
- Add tests for success, failure, and edge states.
- Add regression tests for any bug fix.
- Keep deterministic tests; avoid flaky timing dependencies.
