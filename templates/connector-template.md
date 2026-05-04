# Connector Template

## Connector metadata
- Connector name:
- Provider:
- Authentication type:

## Required interface methods
- authorize(request)
- capture(request)
- refund(request)
- void(request)

## Mapping layer
- mapAuthorizeRequest()
- mapAuthorizeResponse()
- mapCaptureRequest()
- mapCaptureResponse()
- mapRefundRequest()
- mapRefundResponse()

## Webhook handler
- verifySignature(raw, headers)
- parseEvent(raw)
- mapEventToStateAction(event)

## Reliability
- timeout config:
- retry policy reference:

## Tests
- mapping tests:
- error normalization tests:
- webhook verification tests:
