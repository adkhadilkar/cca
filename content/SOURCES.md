# Content Sources — CCAR-F Knowledge Base v3

This log records every source consulted to author `packs/ccar-f/*.json`, per
the sourcing/legal guidance in `docs/DEVELOPMENT_DESIGN.md` §8. All lesson
text, questions, and glossary definitions are **originally written**,
informed by these sources — with one explicit exception noted below (the 12
official sample questions, which are reproduced with attribution because
Anthropic published them for exactly this purpose).

## v3 (2026-07-17): rebuilt against the official Exam Guide

The user supplied three official PDFs, published by Anthropic:

1. **Claude Certified Architect – Foundations: Exam Guide** (Version 0.2,
   Last Updated June 30 2026) — the authoritative source for domain names,
   order, and weights; the full task-statement blueprint (29 statements
   across 5 domains); exam mechanics (60 questions, 120 min, single-answer
   4-choice format, 4-of-6 scenario bank, $125 fee, 100–1,000 scaled score,
   720 pass, 12-month validity); 12 official sample questions with
   explanations; in-scope and out-of-scope topic lists; and preparation
   exercises.
2. **Anthropic Certification Exam Policy** (updated June 25, 2026) —
   confidentiality of Exam Content, prohibited misconduct (explicitly
   including "using AI products or services to assist you during the
   Exam"), ID verification, and a 14-calendar-day appeal window.
3. **Certification Terms and Conditions** — governs Program participation,
   12-month certification term/renewal, and IP ownership of Exam content.

This supersedes the exam-format facts in v1/v2, which were sourced from web
search summaries of Pearson VUE's page and were **wrong in several
particulars**: the domain order/names were different (the official D2 is
"Tool Design & MCP Integration," not "Claude Code"; official D3 is "Claude
Code Configuration & Workflows," not "Prompt Engineering"), and the response
format is **single-answer only** — v1/v2 included multi-select questions
that don't reflect the real exam format at all.

### What changed, concretely

- **Domains reordered and renamed** to match the official outline exactly
  (see `domains.json`). D2/D3/D4's content in prior versions is now spread
  across different domain IDs — this was a full content remap, not a patch.
- **All 30 lessons rewritten**, one per official task statement, using the
  Knowledge/Skills bullets and the reasoning patterns visible in the 12
  official sample-question explanations (e.g., "programmatic enforcement
  over prompt-based guidance whenever compliance must be deterministic" is
  the single most repeated correct-answer pattern in the official samples).
- **Response format corrected**: every question is now single-answer,
  4-choice, matching the real exam exactly. The `type: "multi"` questions
  from v1/v2 are gone.
- **12 official sample questions added**, each tagged `"official": true`
  and cited as `sourceRef: "Official Exam Guide — Sample Question N"` —
  kept visibly distinct from the 22 originally-authored questions that fill
  out the remaining task statements.
- **Out-of-scope topics retired from the lesson set**: prompt-caching
  implementation details, streaming API mechanics, and token-counting
  algorithms are explicitly listed as NOT tested in the official guide, so
  the deep-dive lessons v1/v2 had on these were dropped rather than kept as
  dead weight.
- **Agent SDK specifics added** that generic Claude Code/Managed Agents docs
  didn't cover: `stop_reason` handling, the `Task` tool and `allowedTools`,
  `AgentDefinition`, `fork_session`, MCP's `isError` flag and error
  categories, `.mcp.json` vs. `~/.claude.json` scoping.

## On reproducing the 12 official sample questions

The Certification Exam Policy treats "Exam Content" (tasks, questions,
answers) as Anthropic's confidential information and prohibits distributing
or publishing it. The Exam Guide is a different thing: Anthropic's own
public-facing preparation document, which states its own purpose in its
first paragraph — "This guide describes the exam content, lists the domains
and task statements tested, **provides sample questions**, and recommends
preparation strategies." Reproducing those 12 questions in a personal
study app, clearly attributed and separated from original content, matches
exactly how the Guide says it should be used. This is not the live,
confidential exam question bank — nobody involved in this project has ever
had access to that, and the 22 originally-authored questions are exactly
that: original, modeled on the published task statements the way any
independent study guide would be.

## Background sources (v1/v2, partially superseded)

These were used for v1/v2 and remain useful background where they overlap
with official task statements (mainly D3's CLAUDE.md/skills/plan-mode
content), but are no longer the primary source for exam scope or weighting:

| Source | URL | Status |
|---|---|---|
| Claude Managed Agents overview | https://platform.claude.com/docs/en/managed-agents/overview | Background only |
| Multiagent orchestration | https://platform.claude.com/docs/en/managed-agents/multiagent-orchestration | Background only |
| Agent Skills overview / best practices | https://platform.claude.com/docs/en/agents-and-tools/agent-skills/overview | Background only |
| code.claude.com/docs crawl (168 pages, user-run) | https://code.claude.com/docs/llms.txt | Background; informed D3's CLAUDE.md/hooks/skills lessons where they matched official task statements |
| Building effective agents (Anthropic Engineering) | https://www.anthropic.com/engineering/building-effective-agents | Background only — the official guide doesn't test the generic five-pattern framing as such |

License/terms: Anthropic developer documentation and the official Exam
Guide are used as reference to author original study material; not
redistributed verbatim except the 12 attributed sample questions above.

## Remaining known gaps

- The **154 code.claude.com pages not yet mined** from the v2 crawl (Agent
  SDK internals beyond what the Exam Guide covers, plugins, sandboxing,
  agent teams, background agents) — optional future depth, not a current
  gap, since the official Exam Guide is now the authoritative scope
  definition and none of today's lessons depend on those pages.
- The Exam Guide is versioned ("Version 0.2") and dated; Anthropic may
  revise it. Re-check for a newer version before treating this pack as
  permanently current.
