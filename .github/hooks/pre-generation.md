# Hook: Pre-Generation

## Purpose
Run before generating implementation changes.

## Checks
1. Confirm target prompt and skill files are identified.
2. Confirm architecture and lifecycle instructions are loaded.
3. Confirm hard boundaries from `Governance.md` are acknowledged.

## Block generation when
- Payment lifecycle rules are missing for payment changes.
- Idempotency behavior is undefined for write paths.
- Requested changes touch restricted financial subsystems without approval.
