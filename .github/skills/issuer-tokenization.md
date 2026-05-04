# Skill: Issuer Tokenization

## Use when
Creating issuer/network tokenization flows.

## Inputs
- Token type and scope
- Enrollment/verification requirements
- Storage and expiry policy

## Steps
1. Validate eligibility and request payload.
2. Create token through issuer/network integration path.
3. Persist token metadata with scope keys.
4. Provide token lookup and revocation endpoints.
5. Enforce masking in logs and responses.

## Required checks
- Token mapped to merchant/customer/device context.
- No sensitive fields leak to logs.
- Revocation and expiry behavior is tested.
