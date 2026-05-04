# Skill: Reconciliation Job

## Use when
Implementing reconciliation and discrepancy detection tasks.

## Inputs
- Internal transaction dataset
- External settlement/partner report
- Matching keys and tolerances

## Steps
1. Normalize data from both sides.
2. Match records using deterministic keys.
3. Classify matched, missing, and mismatched groups.
4. Persist discrepancy records with reason codes.
5. Generate report and retry unresolved fetches if needed.

## Required checks
- Matching rules are deterministic.
- Discrepancies are auditable.
- Reconciliation test fixtures include mismatch scenarios.
