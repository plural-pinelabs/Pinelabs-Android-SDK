# Express Agentic Engineering Layer

This repository now includes an AI guidance layer for safe, consistent payments engineering.

## Why this exists
- Encode architecture and rules once for AI agents and engineers.
- Reduce prompt variance for payments features.
- Add governance controls before code lands.

## Project context
- `ExpressCheckoutSdk/`: payment orchestration SDK module.
- `app/`: integration/sample Android app.

## Agentic structure
- `.github/agents`: specialized AI role definitions.
- `.github/instructions`: always-on engineering rules.
- `.github/prompts`: task templates with inputs and outputs.
- `.github/skills`: reusable implementation capabilities.
- `.github/hooks`: safety and governance checks.
- `.github/workflows`: CI automation for tests, review, and scans.
- `docs/agentic`: conceptual knowledge for payments domains.
- `playbooks`: repeatable multi-step delivery workflows.
- `plans`: execution plans produced by planner agents.
- `templates`: reusable scaffolds for APIs, connectors, webhooks, retry.

## Start here
1. Read `Quickstart.md`.
2. Read `Architecture.md` and `Governance.md`.
3. Use one prompt from `.github/prompts`.
4. Execute the associated playbook.
5. Ensure hooks and workflows pass before merge.

## Scope and intent
This layer is documentation and workflow guidance. It does not replace code ownership, design reviews, or production controls.
