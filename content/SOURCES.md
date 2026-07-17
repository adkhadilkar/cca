# Content Sources — CCAR-F Knowledge Base v2

This log records every source consulted to author `packs/ccar-f/*.json`, per
the sourcing/legal guidance in `docs/DEVELOPMENT_DESIGN.md` §8. All lesson
text, questions, and glossary definitions in the pack are **originally
written**, informed by these sources — not copied from them.

## v2 update (2026-07-17): D2 verification gap closed

v1 shipped with ~27% of the exam blueprint (all of D2, plus D1 Lesson 2)
authored from trained knowledge rather than a fresh fetch, because
`code.claude.com` and `www.anthropic.com` were unreachable from the authoring
session's network policy (403 Forbidden). The user ran a crawler
(`fetch_docs.py`, stdlib-only Python) from their own machine, seeded from
`https://code.claude.com/docs/llms.txt`, which pulled **168 markdown pages**
plus **44 images** from `code.claude.com/docs/en/*`, and one fetch of the
Anthropic engineering blog post, and uploaded the result as a zip.

Findings:

- **D1 Lesson 2** (five workflow patterns) was re-verified against a fresh
  fetch of `https://www.anthropic.com/engineering/building-effective-agents`
  and found **accurate as originally authored** — no changes made.
- **D2 required substantial correction.** Specific errors found and fixed
  (see `manifest.json`'s changelog for the full list):
  - The memory hierarchy does **not** have a "most specific wins" override
    rule — CLAUDE.md files at every scope are **concatenated** into context,
    not overridden. This was a factual error in v1, not just an omission.
  - **Auto memory** (Claude's own self-written notes, distinct from
    CLAUDE.md) was missing entirely — added as a first-class D2 topic.
  - **Hook exit codes**: v1 said "any non-zero exit blocks a PreToolUse
    hook." Actual behavior: only exit code **2** blocks; exit 1 and other
    codes are non-blocking errors. Corrected in the lesson and in the
    affected practice question (`q-d2-005`).
  - **Custom slash commands have been merged into Skills** — v1 described
    them as two separate mechanisms; they're now the same system.
  - **Routines** (Anthropic-managed scheduled/event-driven automation) is a
    real, current feature that v1 didn't cover at all — added as a new
    lesson, question, and glossary term.

Every D2 doc page used is listed in the table below alongside the D1/D3/D4/D5
pages from v1.

## Official Anthropic documentation

| Domain(s) | Page | URL | Retrieved |
|---|---|---|---|
| D1 | Claude Managed Agents overview | https://platform.claude.com/docs/en/managed-agents/overview | 2026-07-16 |
| D1 | Multiagent orchestration | https://platform.claude.com/docs/en/managed-agents/multiagent-orchestration | 2026-07-16 |
| D1 | Define outcomes | https://platform.claude.com/docs/en/managed-agents/define-outcomes | 2026-07-16 |
| D1 | Building effective agents (engineering blog) | https://www.anthropic.com/engineering/building-effective-agents | 2026-07-17 (user crawl) |
| D2 | How Claude remembers your project (memory) | https://code.claude.com/docs/en/memory | 2026-07-17 (user crawl) |
| D2 | Hooks reference | https://code.claude.com/docs/en/hooks | 2026-07-17 (user crawl) |
| D2 | Create custom subagents | https://code.claude.com/docs/en/sub-agents | 2026-07-17 (user crawl) |
| D2 | Extend Claude with skills | https://code.claude.com/docs/en/skills | 2026-07-17 (user crawl) |
| D2 | CLI reference | https://code.claude.com/docs/en/cli-reference | 2026-07-17 (user crawl) |
| D2 | Headless mode | https://code.claude.com/docs/en/headless | 2026-07-17 (user crawl) |
| D2 | Settings | https://code.claude.com/docs/en/settings | 2026-07-17 (user crawl) |
| D2 | Permissions | https://code.claude.com/docs/en/permissions | 2026-07-17 (user crawl) |
| D2 | Automate work with routines | https://code.claude.com/docs/en/routines | 2026-07-17 (user crawl) |
| D2 | Overview | https://code.claude.com/docs/en/overview | 2026-07-17 (user crawl, pasted earlier in session) |
| D2 (background, not yet mined for content) | 154 further pages under code.claude.com/docs/en/ — agent-sdk/*, mcp, skills internals, plugins, sandboxing, agent-teams, agent-view, and more | see `docs/llms.txt` index | 2026-07-17 (user crawl) |
| D2 | Agent Skills overview | https://platform.claude.com/docs/en/agents-and-tools/agent-skills/overview | 2026-07-16 |
| D2 | Agent Skills best practices | https://platform.claude.com/docs/en/agents-and-tools/agent-skills/best-practices | 2026-07-16 |
| D3 | Structured outputs | https://platform.claude.com/docs/en/build-with-claude/structured-outputs | 2026-07-16 |
| D3 | Extended thinking | https://platform.claude.com/docs/en/build-with-claude/extended-thinking | 2026-07-16 |
| D4 | Tool use overview | https://platform.claude.com/docs/en/agents-and-tools/tool-use/overview | 2026-07-16 |
| D4 | How tool use works | https://platform.claude.com/docs/en/agents-and-tools/tool-use/how-tool-use-works | 2026-07-16 |
| D4 | Define tools | https://platform.claude.com/docs/en/agents-and-tools/tool-use/define-tools | 2026-07-16 |
| D4 | Remote MCP servers | https://platform.claude.com/docs/en/agents-and-tools/remote-mcp-servers | 2026-07-16 |
| D4 | MCP connector | https://platform.claude.com/docs/en/agents-and-tools/mcp-connector | 2026-07-16 |
| D5 | Context windows | https://platform.claude.com/docs/en/build-with-claude/context-windows | 2026-07-16 |
| D5 | Compaction | https://platform.claude.com/docs/en/build-with-claude/compaction | 2026-07-16 |
| D5 | Context editing | https://platform.claude.com/docs/en/build-with-claude/context-editing | 2026-07-16 |
| D5 | Prompt caching | https://platform.claude.com/docs/en/build-with-claude/prompt-caching | 2026-07-16 |
| D5 | Streaming | https://platform.claude.com/docs/en/build-with-claude/streaming | 2026-07-16 |
| D5 | Message Batches API | https://platform.claude.com/docs/en/build-with-claude/batch-processing | 2026-07-16 |
| D5 | Working with messages | https://platform.claude.com/docs/en/build-with-claude/working-with-messages | 2026-07-16 |
| D5 | Memory tool | https://platform.claude.com/docs/en/agents-and-tools/tool-use/memory-tool | 2026-07-16 |

License/terms: Anthropic developer documentation. Not redistributed verbatim;
used as reference to author original study material (see §8 of the dev design doc).

## Remaining known gap: exam format facts (third-party, unverified)

`docs/DEVELOPMENT_DESIGN.md` already flags these as needing confirmation from
Anthropic's own certification pages:

- 60 questions / 120 minutes / 720 pass score (of 1000) — Foundations exam
- 5 domains and approximate weights (D1 27%, D2 20%, D3 20%, D4 18%, D5 15%)

Source: web search of Pearson VUE's Anthropic certification page and
secondary write-ups. **Still not verified against a live fetch of
`pearsonvue.com` or an Anthropic-owned certification page** — that domain
was not part of the D2 crawl (it's a certification-logistics page, not a
Claude Code doc, so it wasn't in scope for `fetch_docs.py`). This is now the
single largest remaining unverified fact in the pack. Re-verify before the
exam guide (`guide.json`) ships as authoritative — a wrong pass score or
question count would mislead every user of the app.

## Content not yet mined from the crawl

The user's crawl retrieved 168 pages, but only ~14 were read closely enough
to correct lessons this round (memory, hooks, sub-agents, skills,
cli-reference, headless, settings, permissions, routines, overview, plus the
engineering blog post). The other ~154 pages (Agent SDK internals, plugins,
sandboxing, agent teams, background agents, MCP details, devcontainers, and
more) are saved locally by the user but not yet reviewed for additional exam
content. Treat those as a v3 opportunity, not a current gap — nothing in
today's lessons depends on them being wrong.
