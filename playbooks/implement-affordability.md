# Playbook: Implement Affordability

## Objective
Add affordability decision flows for checkout options.

## Steps
1. Define affordability inputs and decision outputs.
2. Implement partner integration adapter.
3. Add decision service with explicit rule checks.
4. Add fallback behavior for partner failures.
5. Add reporting and audit for affordability outcomes.
6. Add tests for eligible/ineligible/error scenarios.

## Exit criteria
- Decision path is deterministic and testable.
- Partner errors do not break checkout safety.
- Audit trace is complete for decisions.
