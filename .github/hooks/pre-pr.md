# Hook: Pre-PR

## Purpose
Ensure pull requests are review-ready and policy-compliant.

## Required PR sections
- Architecture impact
- Risk assessment
- Test evidence
- Rollback strategy

## Checks
1. Required instructions referenced in PR description.
2. Hook checklist satisfied.
3. Reviewer-agent findings addressed.

## Block PR when
- Governance boundaries are violated.
- Tests are missing for high-risk paths.
- Security scan or CI status is failing.
