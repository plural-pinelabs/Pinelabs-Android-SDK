# Security Guidelines Instruction

## Data protection
- Never log PAN, CVV, full token payloads, or secrets.
- Mask sensitive identifiers in logs and errors.
- Encrypt sensitive at-rest values if persisted.

## Authentication and integrity
- Validate webhook signatures and timestamp windows.
- Enforce replay protection for callbacks.
- Rotate credentials and keys through secure config management.

## Access and secrets
- No hardcoded secrets in code or docs.
- Use environment-driven or secret-store injection.
- Scope access by least privilege.

## Compliance-oriented checks
- Maintain audit trail for payment actions and state changes.
- Record actor, timestamp, correlation IDs, and outcome.
- Include negative-path tests for auth and signature failures.
