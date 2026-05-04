# Hook: Pre-Commit

## Purpose
Validate local changes before commit.

## Checks
- Build and unit test command succeeds.
- New write paths include idempotency behavior.
- New state transitions include tests and audit events.
- Security redaction checks pass for logs.

## Suggested command set
1. `./gradlew test`
2. `./gradlew :ExpressCheckoutSdk:lint`
3. Project-specific static checks

## Block commit when
- Tests fail.
- Financial integrity checklist fails.
- Restricted files changed without approval note.
