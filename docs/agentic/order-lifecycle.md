# Order Lifecycle

## Order states (example)
- ORDER_CREATED
- PAYMENT_PENDING
- PAYMENT_COMPLETED
- PAYMENT_FAILED
- ORDER_CANCELLED
- ORDER_REFUNDED

## Rules
1. Payment authorization must precede capture for deferred capture flows.
2. Refund is allowed only for captured payments.
3. Failed payment should not move order to PAYMENT_COMPLETED.

## Integration notes
- Keep order and payment state transitions synchronized.
- Emit audit trail for order state changes triggered by payment events.
