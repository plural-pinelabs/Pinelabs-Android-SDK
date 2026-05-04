# Connector Guidelines Instruction

## Mandatory interface
Every connector must implement:
- authorize(request)
- capture(request)
- refund(request)
- void(request)

## Responsibilities
- Map internal request -> connector payload.
- Execute connector API call.
- Map connector payload -> internal response.

## Forbidden behavior
- No payment state decisions.
- No orchestration branching logic.
- No direct mutation of reconciliation data.

## Reliability
- Add connector timeout config.
- Add connector-specific retry policy only for safe transport failures.
- Include connector request/response correlation identifiers.

## Test requirements
- Mapping tests for each connector operation.
- Non-200 and malformed payload tests.
- Signature verification tests for webhook callbacks.
