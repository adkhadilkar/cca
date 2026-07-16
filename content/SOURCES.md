# Content Sources — CCAR-F Knowledge Base v1

This log records every source consulted to author `packs/ccar-f/*.json`, per
the sourcing/legal guidance in `docs/DEVELOPMENT_DESIGN.md` §8. All lesson
text, questions, and glossary definitions in the pack are **originally
written**, informed by these sources — not copied from them.

## Official Anthropic documentation (platform.claude.com)

Fetched as Markdown via each page's official `.md` endpoint on 2026-07-16.

| Domain(s) | Page | URL |
|---|---|---|
| D1 | Claude Managed Agents overview | https://platform.claude.com/docs/en/managed-agents/overview |
| D1 | Multiagent orchestration | https://platform.claude.com/docs/en/managed-agents/multiagent-orchestration |
| D1 | Define outcomes | https://platform.claude.com/docs/en/managed-agents/define-outcomes |
| D2 | Agent Skills overview | https://platform.claude.com/docs/en/agents-and-tools/agent-skills/overview |
| D2 | Agent Skills best practices | https://platform.claude.com/docs/en/agents-and-tools/agent-skills/best-practices |
| D3 | Structured outputs | https://platform.claude.com/docs/en/build-with-claude/structured-outputs |
| D3 | Extended thinking | https://platform.claude.com/docs/en/build-with-claude/extended-thinking |
| D4 | Tool use overview | https://platform.claude.com/docs/en/agents-and-tools/tool-use/overview |
| D4 | How tool use works | https://platform.claude.com/docs/en/agents-and-tools/tool-use/how-tool-use-works |
| D4 | Define tools | https://platform.claude.com/docs/en/agents-and-tools/tool-use/define-tools |
| D4 | Remote MCP servers | https://platform.claude.com/docs/en/agents-and-tools/remote-mcp-servers |
| D4 | MCP connector | https://platform.claude.com/docs/en/agents-and-tools/mcp-connector |
| D5 | Context windows | https://platform.claude.com/docs/en/build-with-claude/context-windows |
| D5 | Compaction | https://platform.claude.com/docs/en/build-with-claude/compaction |
| D5 | Context editing | https://platform.claude.com/docs/en/build-with-claude/context-editing |
| D5 | Prompt caching | https://platform.claude.com/docs/en/build-with-claude/prompt-caching |
| D5 | Streaming | https://platform.claude.com/docs/en/build-with-claude/streaming |
| D5 | Message Batches API | https://platform.claude.com/docs/en/build-with-claude/batch-processing |
| D5 | Working with messages | https://platform.claude.com/docs/en/build-with-claude/working-with-messages |
| D5 | Memory tool | https://platform.claude.com/docs/en/agents-and-tools/tool-use/memory-tool |

License/terms: Anthropic developer documentation. Not redistributed verbatim;
used as reference to author original study material (see §8 of the dev design doc).

## Anthropic Engineering blog

| Domain | Page | URL | Note |
|---|---|---|---|
| D1 | Building effective agents | https://www.anthropic.com/engineering/building-effective-agents | **Fetch blocked (HTTP 403)** by this session's network egress policy. Content used here (workflows vs. agents; the five patterns: prompt chaining, routing, parallelization, orchestrator-workers, evaluator-optimizer) reflects the assistant's trained knowledge of this well-known public post, not a fresh fetch. **Action item: re-verify D1 Lesson 2 word-for-word against the live post before shipping**, since it could not be directly re-fetched this session. |

## Exam format facts (third-party, corroborating)

`docs/DEVELOPMENT_DESIGN.md` already flags these as needing confirmation from
Anthropic's own certification pages before authoring is finalized:

- 60 questions / 120 minutes / 720 pass score (of 1000) — Foundations exam
- 5 domains and approximate weights (D1 27%, D2 20%, D3 20%, D4 18%, D5 15%)

Source: web search of Pearson VUE's Anthropic certification page and
secondary write-ups (see prior session's exam research). **Not yet verified
against a live fetch of https://www.pearsonvue.com/us/en/anthropic.html or an
Anthropic-owned certification page** — `code.claude.com`, `anthropic.com`,
and `pearsonvue.com` were all unreachable (403/blocked) from this session's
network policy. Re-verify before the exam guide ships as authoritative.

## Known gaps / blocked sources (follow-up required)

The following official sources are directly relevant to the exam domains but
were **not reachable** in this session due to network egress policy
(403 Forbidden / connection blocked), not because they don't exist:

- `code.claude.com` — Claude Code's own documentation (CLAUDE.md, hooks,
  slash commands, headless mode, permissions). D2 lessons in this pack are
  therefore written from trained knowledge of Claude Code, **not a fresh
  official-doc fetch** — the highest-priority re-verification item.
- `www.anthropic.com` — Engineering blog, Anthropic Academy course pages.
- `modelcontextprotocol.io` — the MCP specification site itself (D4 lessons
  rely on Anthropic's own MCP connector/remote-MCP docs instead, which were
  reachable).
- Anthropic Academy / Skilljar course video content — not fetchable at all
  (gated, and video isn't text); would need official transcripts per the
  content pipeline's §7.3 process.

**Recommendation:** before this pack is treated as exam-ready, re-run the
fetch pipeline from an environment with access to `code.claude.com` and
`anthropic.com`, diff against what's authored here, and correct any drift —
especially D1's workflow-pattern definitions and all of D2.
