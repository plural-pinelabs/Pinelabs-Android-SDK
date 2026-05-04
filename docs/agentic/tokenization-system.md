# Tokenization System

## Token types
- Merchant Token: stored card/payment reference for merchant context.
- Network Token: network-issued tokenized payment credential.

## Required token mappings
- customer_id
- device_id
- merchant_id

## Security rules
- Do not expose raw instrument data in logs.
- Enforce scoped retrieval by actor and merchant context.
- Track token lifecycle: created, active, suspended, revoked.

## Operational checks
- Token creation must be auditable.
- Revocation must invalidate downstream usage immediately.
