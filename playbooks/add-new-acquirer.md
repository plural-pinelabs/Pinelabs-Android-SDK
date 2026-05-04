# Playbook: Add New Acquirer

## Objective
Safely add a new card acquiring partner.

## Steps
1. Parse provider API specification and auth model.
2. Implement connector contract methods.
3. Add request and response mappings.
4. Implement webhook verification and event mapping.
5. Add retry policy for transient provider failures.
6. Add reconciliation hooks and discrepancy handling.
7. Add integration/certification tests.

## Exit criteria
- Connector passes contract and mapping tests.
- Financial integrity checks pass.
- Reviewer-agent signs off.
