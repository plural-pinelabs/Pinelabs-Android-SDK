# Governance and AI Safety Boundaries

## Objective
Prevent unsafe, non-compliant, or architecture-breaking AI-generated changes.

## Hard boundaries (must not modify without explicit approval)
- Ledger posting and settlement algorithms.
- Reconciliation matching algorithms.
- Production financial integrity checks.
- Cryptographic primitives and key management behavior.

## Required controls for payment changes
1. Idempotency coverage for write paths.
2. Payment state machine compliance.
3. Retry policy defined and bounded.
4. Audit logging present for state changes and external events.
5. Security checks for PII/PCI redaction in logs.

## Pull request requirements
- Architecture alignment statement.
- Risk assessment section.
- Test evidence for success and failure paths.
- Rollback/mitigation note for high-impact changes.

## AI agent operating policy
- Prefer minimal, isolated changes.
- Reuse existing templates, prompts, and skills.
- Reject changes that bypass mandated hooks.
- Escalate when uncertain about financial correctness.

## Approval model
- Connector changes: Connector owner + reviewer-agent checks.
- Payment state changes: Payment owner + reviewer-agent checks.
- Security-sensitive changes: Security owner approval.
- Reconciliation-related changes: Reconciliation owner approval.

## Incident posture
If a generated change risks financial correctness:
1. Stop merge.
2. Open incident note with impacted flows.
3. Add regression tests before reopening PR.
