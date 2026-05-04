# Prompt: Implement Tokenization

## Goal
Implement secure tokenization flow for payment methods.

## Inputs
- token_type (merchant/network)
- create_request_schema
- lookup_request_schema
- revocation_policy

## Steps
1. Apply `issuer-tokenization` skill.
2. Enforce token scoping by merchant/customer/device.
3. Add token lifecycle and revocation handling.
4. Add security controls and logging redaction checks.
5. Generate tests for create/lookup/revoke and failure paths.

## Expected output
- Token service APIs and persistence model.
- Token validation and revocation flows.
- Security and audit test cases.

## Definition of done
- Sensitive data not logged.
- Token scope rules enforced.
- Negative-path tests pass.
