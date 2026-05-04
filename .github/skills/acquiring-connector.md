# Skill: Acquiring Connector

## Use when
Adding a new acquirer connector.

## Inputs
- Connector API specification
- Authentication method
- Endpoint mappings

## Steps
1. Define connector adapter implementing required methods.
2. Build request mappers per action.
3. Build response mappers per action.
4. Implement connector client with timeout and correlation IDs.
5. Integrate connector into orchestrator routing.

## Required checks
- No business logic in adapter.
- Mapping tests for all actions.
- Connector error mapping is deterministic.
