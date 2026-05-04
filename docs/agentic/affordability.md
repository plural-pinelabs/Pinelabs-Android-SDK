# Affordability

## Scope
Guidance for affordability checks and financing eligibility within checkout/payment flows.

## Typical checks
- Eligibility by merchant and product category.
- Amount threshold and tenure constraints.
- Risk and policy flags from partner systems.

## Integration rules
- Keep affordability decision logic explicit and testable.
- Separate partner response mapping from business decisioning.
- Store affordability decision trace for auditability.

## Testing
- Positive and negative eligibility scenarios.
- Partner timeout/failure fallback behavior.
- Consistent user-facing response for denied options.
