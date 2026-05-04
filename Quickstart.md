# Quickstart: Agentic Coding for Express

## Prerequisites
- Repository cloned and buildable with Gradle.
- AI coding assistant enabled in editor/CI.
- Team agreement on governance in `Governance.md`.

## First-time setup
1. Read `Architecture.md`.
2. Read key instructions in `.github/instructions`.
3. Pick a prompt in `.github/prompts`.
4. Use associated skills in `.github/skills`.
5. Follow matching playbook in `playbooks/`.

## Recommended first workflow
- Prompt: `.github/prompts/add-acquirer-connector.md`
- Skills:
  - `.github/skills/acquiring-connector.md`
  - `.github/skills/webhook-handler.md`
  - `.github/skills/payment-retry.md`
- Hook gate: `.github/hooks/financial-integrity-check.md`

## CI workflow expectation
On PR creation:
1. `ci.yml` runs tests and linting.
2. `ai-code-review.yml` checks architecture and governance rules.
3. `security-scan.yml` checks dependencies and risky patterns.
4. `ai-test-generation.yml` suggests missing test cases.

## Authoring guidance
- Keep each file focused on one concept.
- Prefer explicit rules/checklists over long prose.
- Update prompts and skills when architecture changes.

## Definition of done for AI-generated work
- All required hooks pass.
- All required tests pass.
- Governance boundaries respected.
- Reviewer-agent findings resolved.
