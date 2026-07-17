# Knowledge Base — Claude Certified Architect (Foundations)

Generated from `content/packs/ccar-f/*.json` — the single source of truth. Regenerate this file after editing the JSON rather than hand-editing it directly.

**v3: rebuilt against the official Exam Guide, Certification Exam Policy, and Certification Terms and Conditions** (Anthropic PDFs, user-supplied). See `content/SOURCES.md` for full provenance, including how the 12 official sample questions are attributed separately from originally-authored ones.

## Exam overview

- **CCAR-F** — 60 questions, 120 min, pass score 720 (scale: 100–1,000 (scaled score; equates difficulty across exam forms)), fee $125 USD

- Format: Multiple choice — exactly one correct answer and three incorrect (plausible) options per question

- Structure: 4 scenarios are presented, drawn at random from a bank of 6 possible scenarios. Each scenario frames a cluster of questions in a realistic production context.

- Delivery: Online proctored, or at a test center · Validity: 12 months from the date the credential is awarded; must be renewed before expiration or the certification must be re-earned from scratch

| Domain | Title | Weight |
|---|---|---|
| D1 | Agentic Architecture & Orchestration | 27% |
| D2 | Tool Design & MCP Integration | 18% |
| D3 | Claude Code Configuration & Workflows | 20% |
| D4 | Prompt Engineering & Structured Output | 20% |
| D5 | Context Management & Reliability | 15% |

### Scenario bank (4 of these 6 appear on any given exam)

- **Customer Support Resolution Agent** (D1, D2, D5) — An agent (Claude Agent SDK) handles high-ambiguity requests — returns, billing disputes, account issues — via custom MCP tools (get_customer, lookup_order, process_refund, escalate_to_human). Target: 80%+ first-contact resolution while knowing when to escalate.
- **Code Generation with Claude Code** (D3, D5) — A team uses Claude Code for generation, refactoring, debugging, and docs, integrated into their workflow with custom slash commands, CLAUDE.md, and a working sense of plan mode vs. direct execution.
- **Multi-Agent Research System** (D1, D2, D5) — A coordinator delegates to specialized subagents — web search, document analysis, synthesis, report generation — to research topics and produce comprehensive, cited reports.
- **Developer Productivity with Claude** (D2, D3, D1) — An Agent SDK agent helps engineers explore unfamiliar codebases and legacy systems, generate boilerplate, and automate repetitive tasks, using built-in tools (Read, Write, Bash, Grep, Glob) plus MCP servers.
- **Claude Code for Continuous Integration** (D3, D4) — Claude Code runs automated code review, test generation, and PR feedback inside a CI/CD pipeline; prompts must give actionable feedback while minimizing false positives.
- **Structured Data Extraction** (D4, D5) — A system extracts information from unstructured documents, validates output against JSON schemas, handles edge cases gracefully, and integrates with downstream systems at high accuracy.

### What to expect
- This exam tests judgment about production tradeoffs, not trivia — every official sample question rewards the response that fixes the actual root cause with proportionate effort, not the most sophisticated-sounding option.
- A recurring pattern across official sample answers: when compliance must be guaranteed (identity verification before a refund, a required tool call order), the correct answer is programmatic enforcement (hooks, prerequisite gates) — never a prompt instruction alone, however detailed.
- Another recurring pattern: fix the cheapest, most targeted thing first. Vague tool descriptions get fixed by writing better descriptions, not by adding a routing layer or consolidating tools. Narrow task decomposition gets fixed by widening the decomposition, not by patching downstream agents.
- Complete the four hands-on exercises in the official Exam Guide (multi-tool agent with escalation logic; Claude Code team workflow configuration; structured extraction pipeline; multi-agent research pipeline with error propagation) — they map directly onto the five domains.

### Out of scope (explicitly NOT tested)
- Fine-tuning Claude models or training custom models
- Claude API authentication, billing, or account management
- Deep implementation details of specific programming languages/frameworks beyond tool/schema config
- Deploying or hosting MCP servers (infra, networking, container orchestration)
- Claude's internal architecture, training process, or model weights
- Constitutional AI, RLHF, or safety training methodologies
- Embedding models or vector database implementation details
- Computer use (browser/desktop automation) and vision/image analysis capabilities
- Streaming API implementation or server-sent events
- Rate limiting, quotas, or API pricing calculations
- OAuth, API key rotation, or authentication protocol details
- Specific cloud provider configurations (AWS, GCP, Azure)
- Performance benchmarking or model comparison metrics
- Prompt caching implementation details (beyond knowing it exists)
- Token counting algorithms or tokenization specifics

> Unofficial preparation material. Not affiliated with, authored by, or endorsed by Anthropic. This app is built from Anthropic's own publicly released Exam Guide, Certification Terms and Conditions, and Certification Exam Policy (each user-supplied and dated June/June 30 2026) — but Anthropic may update these documents, and this app may lag. Always confirm current exam format, domains, and policies on Anthropic's official certification pages before your exam, and never use this app or any AI assistance during the actual proctored exam.

## Lessons

### D1 — Agentic Architecture & Orchestration (27%)

_Agentic loop lifecycle (stop_reason, tool results in context); coordinator-subagent orchestration via the Task tool; context passing and spawning; enforcement vs. prompt-based guidance for multi-step workflows; Agent SDK hooks for tool interception; task decomposition strategies; session state, resumption, and forking._

#### Task 1.1 — Agentic loops for autonomous task execution (8 min)

An agentic loop is: send a request to Claude, inspect `stop_reason`, act accordingly, repeat. `stop_reason: "tool_use"` means Claude wants to call a tool — execute it and return the result for the next iteration. `stop_reason: "end_turn"` means Claude is done — present the response and stop the loop.

Tool results get appended to conversation history so the model can reason about what to do next with full context of what happened. This is what makes the loop 'agentic': Claude decides which tool to call next based on accumulated context, not a hardcoded sequence.

```text
loop:
  response = send(messages)
  if response.stop_reason == "tool_use":
      result = execute(response.tool_use)
      messages.append(tool_result: result)
      continue
  elif response.stop_reason == "end_turn":
      return response  # done
```

> **Exam key point:** Known anti-patterns to recognize and avoid: parsing the assistant's natural-language TEXT to decide whether to keep looping (fragile — use stop_reason instead); using an arbitrary iteration cap as the PRIMARY stopping mechanism (a safety backstop is fine, but it isn't how the loop is supposed to terminate); and checking for the presence of assistant text as a 'done' signal (Claude can produce text alongside a tool_use block).

#### Task 1.2 — Coordinator-subagent orchestration patterns (8 min)

The tested pattern is hub-and-spoke: a coordinator agent manages ALL inter-subagent communication, error handling, and information routing. Subagents don't talk to each other directly — everything flows through the coordinator, which gives you observability, consistent error handling, and controlled information flow.

Subagents run with ISOLATED context — they do not automatically inherit the coordinator's conversation history. The coordinator decides which subagents to invoke based on query complexity, decomposes the task, delegates, and aggregates results.

A named failure mode: overly NARROW task decomposition by the coordinator, leading to incomplete coverage of a broad topic (e.g., decomposing 'creative industries' into only visual-arts subtasks and silently missing music, writing, and film — the subagents did their assigned jobs correctly; the problem was what they were assigned).

> **Exam key point:** Good coordinator design: dynamically select which subagents to invoke rather than always routing through the full pipeline; partition scope across subagents to minimize duplication; run iterative refinement loops where the coordinator evaluates synthesis output for gaps and re-delegates with targeted follow-up queries until coverage is sufficient.

#### Task 1.3 — Subagent invocation, context passing, and spawning (8 min)

In the Claude Agent SDK, the **Task tool** is the mechanism for spawning subagents. The coordinator's `allowedTools` must explicitly include `"Task"` or it cannot invoke subagents at all.

Subagent context must be EXPLICITLY provided in the prompt — subagents do not automatically inherit parent context, and they don't share memory between separate invocations. When passing findings from one agent to another (e.g., web search results into a synthesis subagent), include the complete findings directly in the subagent's prompt, and use structured formats that separate content from metadata (source URLs, document names, page numbers) to preserve attribution.

`AgentDefinition` configuration covers each subagent type's description, system prompt, and tool restrictions. **Fork-based session management** lets you explore divergent approaches from a shared analysis baseline — start from one point, branch into multiple parallel explorations.

> **Exam key point:** Spawn PARALLEL subagents by emitting multiple Task tool calls in a SINGLE coordinator response, not across separate turns — that's what actually gets them running concurrently. Also: design coordinator prompts around research goals and quality criteria rather than step-by-step procedural instructions, so subagents can adapt.

#### Task 1.4 — Enforcement and handoff patterns in multi-step workflows (8 min)

There are two ways to enforce a required order of operations: programmatic enforcement (hooks, prerequisite gates) and prompt-based guidance (asking nicely in the system prompt). Prompt instructions alone have a NON-ZERO failure rate — the model will occasionally skip a step even when told not to.

When deterministic compliance is required — identity verification before a financial operation is the canonical example — you need a programmatic prerequisite that blocks the downstream tool call until the prerequisite has actually completed (e.g., block `process_refund` until `get_customer` has returned a verified customer ID).

For mid-process human escalation, use a structured handoff protocol: customer details, root-cause analysis, and recommended action — enough for a human agent with no access to the conversation transcript to pick up immediately.

> **Exam key point:** This is the single most repeated pattern in official sample answers: whenever a scenario describes a reliability problem caused by the model occasionally skipping a required step, the fix is a PROGRAMMATIC prerequisite gate — not a better-worded prompt, not few-shot examples showing the correct order. Enhancing the prompt is a plausible-sounding WRONG answer whenever the stakes are financial or safety-critical.

#### Task 1.5 — Agent SDK hooks for interception and normalization (7 min)

Two applied hook patterns are tested. **PostToolUse** hooks intercept a tool's RESULT and transform it before the model processes it — e.g., normalizing heterogeneous data formats (Unix timestamps, ISO 8601, numeric status codes) coming back from different MCP tools into one consistent shape.

Tool call interception hooks intercept an OUTGOING tool call before it executes, to enforce a compliance rule — e.g., blocking a refund tool call above a $500 threshold and redirecting to human escalation instead of letting the model decide case by case.

> **Exam key point:** Same principle as Task 1.4: choose hooks over prompt-based enforcement whenever a business rule needs a GUARANTEE, not a probabilistic best effort. A hook that blocks a call is deterministic; a system-prompt instruction saying 'never approve refunds over $500' is not.

#### Task 1.6 — Task decomposition strategies (7 min)

Choose between two decomposition strategies based on predictability. **Prompt chaining** — a fixed sequential pipeline — fits predictable, multi-aspect work: analyze each file individually, then run a separate cross-file integration pass. **Dynamic adaptive decomposition** fits open-ended investigation where subtasks are generated based on what's discovered at each step.

For large code reviews, split into per-file local analysis passes plus a separate cross-file integration pass — a single pass over many files at once causes attention dilution (inconsistent depth, missed bugs, contradictory findings across files that should be treated identically).

For open-ended tasks (e.g., 'add comprehensive tests to a legacy codebase'), first map structure and identify high-impact areas, then build a prioritized plan that adapts as dependencies are discovered — don't try to fully specify the plan up front.

> **Exam key point:** Prompt chaining = fixed, known steps in advance. Dynamic decomposition = steps discovered as you go. If a scenario says the number of relevant files/subtasks 'depends on what's found,' that's dynamic decomposition, not prompt chaining — a subtly different distinction from D1's coordinator-vs-parallelization framing but the same underlying idea.

#### Task 1.7 — Session state, resumption, and forking (7 min)

`--resume <session-name>` continues a specific named prior conversation. `fork_session` creates an INDEPENDENT branch from a shared analysis baseline so you can explore divergent approaches (e.g., comparing two refactoring strategies from the same starting codebase analysis) without the branches interfering with each other.

When resuming a session after code has changed since the last run, explicitly tell the agent what files changed — don't assume it will re-discover this, and don't require full re-exploration when a targeted note would do.

> **Exam key point:** Starting a NEW session with a structured summary is often more reliable than resuming a stale one — if prior tool results are stale (files have since changed), a fresh session with an injected summary avoids the model reasoning from outdated information. Resume when prior context is still mostly valid; start fresh with a summary when it isn't.


### D2 — Tool Design & MCP Integration (18%)

_Writing effective tool interfaces and descriptions; structured MCP error responses (isError, retryability); distributing tools across agents and tool_choice configuration; integrating MCP servers into Claude Code and agent workflows; selecting built-in tools (Read, Write, Edit, Bash, Grep, Glob) effectively._

#### Task 2.1 — Effective tool interfaces with clear descriptions (8 min)

Tool descriptions are the PRIMARY mechanism an LLM uses to select which tool to call. Minimal descriptions ('Retrieves customer information' / 'Retrieves order details') lead to unreliable selection between similar tools — the model genuinely lacks the information to differentiate them.

A good description includes input formats, example queries, edge cases, and explicit boundaries explaining when to use this tool versus a similar alternative. Ambiguous or overlapping descriptions (e.g., `analyze_content` vs `analyze_document`, near-identical wording) cause misrouting even when the tools do genuinely different things.

System prompt wording can also bias tool selection — keyword-sensitive instructions can create unintended associations that override a well-written tool description, so review the system prompt too, not just the tool schema, when diagnosing a selection problem.

> **Exam key point:** When production logs show poor tool selection with minimal tool descriptions, the correct FIRST step is almost always expanding the descriptions — a low-effort, high-leverage fix. Building a routing layer, adding many few-shot examples, or consolidating tools are all valid ideas in principle but are over-engineered as a FIRST response when the root cause is simply inadequate descriptions.

#### Task 2.2 — Structured error responses for MCP tools (8 min)

MCP tools communicate failure back to the agent via the `isError` flag. But a uniform response like 'Operation failed' prevents the agent from making an appropriate recovery decision — it can't tell a timeout from a validation error from a policy violation.

Distinguish four error categories: **transient** (timeout, service unavailable — worth retrying), **validation** (invalid input — retry only helps if the input itself gets fixed), **business** (a policy violation — not retryable, needs a different action entirely), and **permission** errors. Return structured metadata: `errorCategory`, an `isRetryable` boolean, and a human-readable description, plus a `retriable: false` flag with a customer-friendly explanation for business-rule violations.

Also distinguish ACCESS FAILURES (a real error, needing a retry decision) from VALID EMPTY RESULTS (a successful query that legitimately found nothing) — conflating the two either wastes retries on a query that will never return anything, or silently treats a real failure as 'no results.'

> **Exam key point:** In a subagent, implement LOCAL error recovery for transient failures first; only propagate to the coordinator errors the subagent genuinely can't resolve on its own — and when you do propagate, include what was attempted and any partial results, not just a bare failure signal.

#### Task 2.3 — Distributing tools across agents and tool_choice (8 min)

Giving one agent too many tools (18 instead of 4–5) degrades tool-selection reliability by increasing decision complexity. Agents with tools OUTSIDE their specialization tend to misuse them — a synthesis agent that also has web-search access will sometimes search instead of synthesizing.

Scope tool access per role: give each agent only what its role needs, with a small number of deliberately-scoped cross-role tools for high-frequency needs (e.g., a narrow `verify_fact` tool for a synthesis agent, while complex verification still routes through the coordinator to the full research agent).

`tool_choice` has three modes: `"auto"` (model decides whether/which tool to call), `"any"` (must call SOME tool, model picks which — guarantees a tool call instead of conversational text), and forced selection `{"type": "tool", "name": "..."}` (must call this specific tool).

> **Exam key point:** Use forced tool_choice to guarantee a specific tool runs FIRST (e.g., `extract_metadata` before enrichment steps), then let subsequent steps happen in follow-up turns. Use `"any"` when several extraction schemas exist and you don't know the document type in advance but need SOME structured tool call, not free text.

#### Task 2.4 — Integrating MCP servers into Claude Code and agent workflows (8 min)

MCP servers are scoped at two levels: **project-level** (`.mcp.json`) for shared team tooling, checked into version control, versus **user-level** (`~/.claude.json`) for personal or experimental servers. `.mcp.json` supports environment-variable expansion (e.g., `${GITHUB_TOKEN}`) so credentials never get committed to the repo.

Tools from ALL configured MCP servers are discovered at connection time and available simultaneously — there's no per-turn filtering by default, which is exactly why Task 2.3's scoped-access discipline matters.

**MCP resources** expose content catalogs (issue summaries, documentation hierarchies, database schemas) so the agent has visibility into what data exists WITHOUT needing exploratory tool calls to find out.

> **Exam key point:** Two practical judgment calls that come up: (1) enhance an MCP tool's description in detail so the agent doesn't fall back to a weaker built-in tool (like Grep) when the MCP tool is actually more capable; (2) for standard integrations (e.g., Jira), prefer an existing community MCP server over building a custom one — reserve custom servers for genuinely team-specific workflows.

#### Task 2.5 — Selecting built-in tools effectively (6 min)

**Grep** searches file CONTENTS for patterns — function names, error messages, import statements. **Glob** matches file PATHS by name/extension pattern (e.g., `**/*.test.tsx`). Confusing the two is a common exam distractor: 'find all files containing X' is Grep; 'find all files named like X' is Glob.

**Read**/**Write** operate on whole files. **Edit** makes targeted modifications by matching unique anchor text — when Edit fails because the anchor text isn't unique in the file, fall back to Read + Write for a reliable full-file replacement instead of fighting with Edit.

> **Exam key point:** Build codebase understanding INCREMENTALLY: start with Grep to find entry points, then Read to follow imports and trace flow — rather than reading every file upfront. To trace a function's usage across wrapper modules, first identify all its exported names, then Grep for each name across the codebase.


### D3 — Claude Code Configuration & Workflows (20%)

_CLAUDE.md hierarchy, scoping, and @import; custom slash commands and skills (context: fork, allowed-tools, argument-hint); path-specific rules via .claude/rules/; plan mode vs. direct execution; iterative refinement techniques; integrating Claude Code into CI/CD pipelines._

#### Task 3.1 — CLAUDE.md hierarchy, scoping, and modular organization (8 min)

Three scope levels: **user-level** (`~/.claude/CLAUDE.md`) — applies only to that one user and is NOT shared with teammates via version control; **project-level** (`.claude/CLAUDE.md` or root `CLAUDE.md`) — shared with the team; and **directory-level** (a CLAUDE.md in a subdirectory) for scoped context.

The `@import` syntax references external files to keep CLAUDE.md modular — e.g., a monorepo can import only the standards files relevant to each package rather than one giant file everyone loads in full. `.claude/rules/` is the alternative to a monolithic CLAUDE.md: split instructions into topic-specific files (`testing.md`, `api-conventions.md`, `deployment.md`).

Run `/memory` to verify which memory files actually loaded into a session — this is the standard diagnostic when instructions aren't being followed and you suspect a scoping problem.

> **Exam key point:** Classic diagnostic scenario: a new team member isn't receiving instructions everyone else has. Root cause is almost always that the instructions live in USER-level configuration (personal, not version-controlled) rather than PROJECT-level — the fix is moving them to project scope, not re-explaining the instructions to the new hire.

#### Task 3.2 — Custom slash commands and skills (9 min)

Project-scoped commands live in `.claude/commands/` (shared via version control — everyone who clones/pulls the repo gets them automatically). User-scoped commands live in `~/.claude/commands/` (personal, not shared).

Skills live in `.claude/skills/` with a `SKILL.md` file, and support frontmatter options: `context: fork` runs the skill in an ISOLATED sub-agent context so its (often verbose) output doesn't pollute the main conversation; `allowed-tools` restricts which tools the skill may use during execution (e.g., limit to file-write operations to prevent destructive actions); `argument-hint` prompts the developer for required parameters when they invoke the skill without arguments.

Personal skill customization: create a variant in `~/.claude/skills/` under a different name so your personal tweak doesn't affect teammates using the shared project version.

> **Exam key point:** Choose between skills and CLAUDE.md by loading pattern: Skills are ON-DEMAND (invoked when needed, task-specific workflows); CLAUDE.md is ALWAYS-LOADED (universal standards every session needs). A configuration-file mechanism like '.claude/config.json with a commands array' does not exist — a distractor to watch for.

#### Task 3.3 — Path-specific rules for conditional convention loading (6 min)

`.claude/rules/` files can carry YAML frontmatter with a `paths` field of glob patterns — the rule then loads into context ONLY when Claude is working with a matching file, instead of being loaded into every session unconditionally. This directly reduces irrelevant context and token usage.

Glob-pattern path rules beat directory-level CLAUDE.md files whenever a convention applies to files SCATTERED across many directories rather than confined to one directory tree — the canonical example is test files (`Button.test.tsx` sitting right next to `Button.tsx`) that need the same conventions no matter where they live.

> **Exam key point:** If a scenario describes conventions that must apply 'regardless of location' or 'wherever these files happen to live,' the answer is a `.claude/rules/` file with a `paths` glob (e.g., `paths: ["**/*.test.tsx"]`) — not per-directory CLAUDE.md files (directory-bound, doesn't generalize) and not relying on Claude to infer which section of one big CLAUDE.md applies (unreliable inference vs. explicit matching).

#### Task 3.4 — Plan mode vs. direct execution (7 min)

**Plan mode** fits complex tasks: large-scale changes, multiple genuinely valid approaches, architectural decisions, multi-file modifications. It enables safe codebase exploration and design BEFORE committing to changes, preventing costly rework when dependencies surface late.

**Direct execution** fits simple, well-scoped changes with a clear path — a single-file bug fix with a clear stack trace, adding one validation check to one function.

The **Explore subagent** isolates verbose discovery output (searching, reading many files) and returns a summary, preserving the main conversation's context during multi-phase investigation.

> **Exam key point:** 'Start in direct execution and switch to plan mode only if complexity emerges' is a wrong answer whenever the scenario ALREADY states the task is architecturally complex (e.g., a monolith-to-microservices restructuring) — the complexity is known up front, not something that might emerge later, so plan mode is correct from the start. It's also fine to combine both: plan mode for investigation, then direct execution for the implementation it produced.

#### Task 3.5 — Iterative refinement for progressive improvement (7 min)

When prose descriptions get interpreted inconsistently, 2–3 concrete input/output examples communicate the expected transformation more reliably than more prose. Test-driven iteration — write the test suite first, then iterate by sharing test FAILURES — gives Claude a concrete target rather than a vague notion of 'correct.'

The **interview pattern**: have Claude ask clarifying questions before implementing, to surface considerations you might not have anticipated (cache invalidation strategy, failure modes) — useful in unfamiliar domains where you don't yet know what you don't know.

> **Exam key point:** When multiple issues interact (fixing one changes how another should be fixed), address them together in a single detailed message. When issues are independent, fix them sequentially instead — bundling independent issues just adds noise, and splitting interacting ones causes the model to fix each in isolation without seeing how they affect each other.

#### Task 3.6 — Integrating Claude Code into CI/CD pipelines (8 min)

The `-p` (or `--print`) flag runs Claude Code non-interactively — required in automated pipelines, since without it a script hangs waiting for interactive input that will never come. `--output-format json` plus `--json-schema` enforces machine-parseable structured output for CI (e.g., findings you can post as inline PR comments programmatically).

CLAUDE.md is how you give CI-invoked Claude Code project context: testing standards, fixture conventions, review criteria — the same file mechanism as interactive use, just consumed headlessly.

**Session context isolation** matters for code review specifically: the SAME session that generated the code retains reasoning context from that generation, making it less likely to question its own decisions. An INDEPENDENT review instance — without that prior reasoning context — catches more subtle issues.

> **Exam key point:** A pipeline hanging with 'waiting for interactive input' in the logs is always solved by adding `-p`/`--print` — not by redirecting stdin, not by inventing a nonexistent `CLAUDE_HEADLESS` env var or `--batch` flag. Also: when re-running reviews after new commits, include prior review findings in context and instruct Claude to report only NEW or still-unaddressed issues, to avoid duplicate comments.


### D4 — Prompt Engineering & Structured Output (20%)

_Explicit criteria to reduce false positives; few-shot prompting for consistency; enforcing structured output with tool_use and JSON schemas; validation/retry/feedback loops for extraction quality; efficient batch processing strategy; multi-instance and multi-pass review architectures._

#### Task 4.1 — Explicit criteria to reduce false positives (7 min)

Explicit, checkable criteria beat vague instructions: 'flag comments only when claimed behavior contradicts actual code behavior' works; 'check that comments are accurate' doesn't, because it leaves the judgment call underspecified. Vague qualifiers like 'be conservative' or 'only report high-confidence findings' don't actually improve precision — they just shift where the ambiguity lives.

False positive rate matters beyond the immediate noise: a high false-positive rate in ONE category undermines developer trust in ALL categories, including the accurate ones.

> **Exam key point:** Write specific criteria that define which issues to REPORT (bugs, security) versus SKIP (minor style, local patterns already used elsewhere in the codebase) — rather than trying to filter by the model's self-reported confidence, which (per Domain 1/5 patterns) is generally unreliable. Defining explicit severity criteria with concrete code examples for each level is the reliable path to consistent classification.

#### Task 4.2 — Few-shot prompting for output consistency (7 min)

Few-shot examples are the most effective technique when detailed instructions alone still produce inconsistent, non-actionable output. They're especially valuable for demonstrating AMBIGUOUS-case handling — showing the reasoning for why one action was chosen over a plausible alternative lets the model generalize that judgment to novel, unseen patterns, rather than only matching pre-specified cases.

For extraction tasks, few-shot examples reduce hallucination by demonstrating correct handling of varied document structures (inline citations vs. bibliographies, narrative vs. tabular data) — this is what fixes empty/null extraction on documents whose format differs from what the model has implicitly assumed.

> **Exam key point:** 2–4 TARGETED examples for genuinely ambiguous scenarios beats a large generic set. Show format (location, issue, severity, fix) for consistency, and include examples DISTINGUISHING acceptable patterns from genuine issues — not just examples of issues — so the model learns the boundary, not just the positive class.

#### Task 4.3 — Enforcing structured output with tool_use and JSON schemas (8 min)

`tool_use` with a JSON schema is the most reliable way to GUARANTEE schema-compliant structured output — it eliminates JSON syntax errors entirely, unlike asking the model to 'return valid JSON' in prose.

Crucial limitation: strict schemas eliminate SYNTAX errors but do NOT prevent SEMANTIC errors — line items that don't sum to the stated total, a value placed in the wrong field. Schema compliance is a floor, not a correctness guarantee.

Design fields as optional/nullable when the source document may genuinely not contain that information — a required field the model can't fill will get FABRICATED to satisfy the schema, which is worse than a null. Use enum values like `"unclear"` for ambiguous cases and an `"other"` + free-text-detail pattern for categories you can't fully enumerate in advance.

> **Exam key point:** `tool_choice: "any"` guarantees SOME structured tool call when multiple extraction schemas exist and the document type is unknown up front. Forced selection (`{"type": "tool", "name": "extract_metadata"}`) guarantees a SPECIFIC tool runs first, e.g. before enrichment steps that depend on its output.

#### Task 4.4 — Validation, retry, and feedback loops for extraction quality (7 min)

On a validation failure, retry by appending the SPECIFIC validation error to the prompt (not a generic 'try again') — this gives the model something concrete to correct. Include the original document, the failed extraction, and the specific error in the follow-up request.

Retries have a hard limit: they're effective for format/structural mismatches (fixable) but USELESS when the required information is simply absent from the source document — no amount of retrying recovers information that was never there. Recognizing which failure mode you're looking at determines whether to retry or to route elsewhere (e.g., to human review, or to skip the field as legitimately unavailable).

> **Exam key point:** Feedback-loop design tip worth remembering: add a `detected_pattern` field to structured findings so you can later analyze WHICH code constructs trigger a given finding — this turns 'developers keep dismissing this finding' into a systematic, fixable pattern rather than an anecdote.

#### Task 4.5 — Efficient batch processing strategies (7 min)

The Message Batches API: 50% cost savings, up to a 24-hour processing window, and NO guaranteed latency SLA. It does NOT support multi-turn tool calling within a single request — you can't execute a tool mid-request and get the result back inside that same batched call.

Match the API to the workflow's latency tolerance: synchronous API for blocking workflows (a pre-merge check developers are waiting on); batch API for non-blocking, latency-tolerant workloads (overnight reports, weekly audits, nightly test generation). `custom_id` fields correlate each batch request with its response, including for resubmitting only the failed subset after a partial failure.

> **Exam key point:** '50% cheaper' is not sufficient justification to batch a BLOCKING workflow — a pre-merge check that developers wait on cannot tolerate a non-guaranteed, up-to-24-hour SLA, no matter the savings. Split mixed proposals: batch what's genuinely latency-tolerant, keep synchronous calls for what isn't. Also refine prompts on a small sample BEFORE submitting a large batch, since a batch failure often means re-submitting many documents at once.

#### Task 4.6 — Multi-instance and multi-pass review architectures (7 min)

Self-review has a structural limitation: a model that just generated code retains its own reasoning context in the same session, making it LESS likely to question its own decisions — this holds even with explicit self-review instructions or extended thinking. An INDEPENDENT review instance, without that prior reasoning context, catches more subtle issues.

For large multi-file changes, split review into per-file LOCAL passes (for local issues) plus a separate cross-file INTEGRATION pass (for data-flow issues spanning files) — a single combined pass over many files causes attention dilution: inconsistent depth, missed bugs, and contradictory findings on identical patterns in different files.

> **Exam key point:** A higher-tier model with a larger context window does NOT fix attention-dilution inconsistency — that's a quality-of-attention problem, not a capacity problem. Splitting into focused passes is the fix. Requiring 2-of-3 independent runs to agree before flagging an issue can actually SUPPRESS real bugs that are only caught intermittently — it's not a free reliability upgrade.


### D5 — Context Management & Reliability (15%)

_Preserving critical information across long interactions; escalation and ambiguity resolution; error propagation across multi-agent systems; managing context in large codebase exploration; human review workflows and confidence calibration; provenance and uncertainty in multi-source synthesis._

#### Task 5.1 — Preserving critical information across long interactions (8 min)

Progressive summarization risk: condensing numerical values, percentages, dates, and customer-stated expectations into a vague summary loses exactly the details that matter most. The **lost-in-the-middle effect**: models reliably process the BEGINNING and END of long inputs but may under-weight findings buried in the middle.

Tool results accumulate in context disproportionately to their relevance — an order lookup might return 40+ fields when only 5 matter for the task at hand, silently eating context budget turn after turn.

The fix: extract transactional facts (amounts, dates, order numbers, statuses) into a persistent 'case facts' block included in every prompt, OUTSIDE the summarized history — so critical numbers survive even as older turns get compressed. Trim verbose tool outputs to only the relevant fields before they accumulate, and place key findings at the BEGINNING of aggregated inputs with explicit section headers to counteract the lost-in-the-middle effect.

> **Exam key point:** When downstream agents have a limited context budget, have UPSTREAM agents return structured data (key facts, citations, relevance scores) instead of verbose prose and full reasoning chains — push the summarization work to the source, not the consumer.

#### Task 5.2 — Escalation and ambiguity resolution (8 min)

Three legitimate escalation triggers: an explicit customer request for a human, a genuine policy gap or exception (not just 'this is complex'), and inability to make meaningful progress. Honor an explicit request for a human IMMEDIATELY, without first attempting investigation the customer didn't ask for.

When the issue is straightforward and within the agent's capability, acknowledge any frustration but OFFER resolution rather than escalating preemptively — only escalate if the customer reiterates their preference for a human after that offer.

When a tool returns multiple matching customers, ask for an additional identifier rather than guessing via heuristics — a wrong heuristic pick here is exactly the kind of error that erodes trust.

> **Exam key point:** Sentiment-based escalation and self-reported model confidence scores are both UNRELIABLE proxies for actual case complexity — an agent can be sentiment-neutral on a genuinely hard case, or confidently wrong on one. The fix for poor escalation calibration is explicit criteria with few-shot examples showing when to escalate vs. resolve — not a confidence threshold, not a separate ML classifier, not sentiment analysis.

#### Task 5.3 — Error propagation across multi-agent systems (7 min)

Structured error context — failure type, the query that was attempted, any partial results, potential alternatives — is what lets a coordinator make an intelligent recovery decision. A generic status like 'search unavailable' hides exactly the context the coordinator needs.

Two anti-patterns to recognize: silently suppressing an error by returning an empty result marked as success (hides the failure entirely, risks incomplete output presented as complete), and terminating the ENTIRE workflow on a single subagent failure (throws away everything that DID succeed when partial results plus an annotated gap would have been more useful).

> **Exam key point:** Same access-failure-vs-valid-empty-result distinction from Task 2.2 reappears here at the multi-agent level: a timeout needs a retry decision; a successful query that legitimately found nothing does not. Structure synthesis output with explicit coverage annotations — which findings are well-supported vs. which topic areas have gaps due to a source that failed — rather than presenting a silently incomplete result as if it were comprehensive.

#### Task 5.4 — Managing context in large codebase exploration (7 min)

In extended sessions, models start giving inconsistent answers and referencing 'typical patterns' instead of the SPECIFIC classes/functions discovered earlier — a sign of context degradation, not a model getting 'dumber.' Scratchpad files persist key findings across context boundaries so later questions can reference concrete earlier discoveries instead of re-deriving them from a degraded working context.

Delegate verbose exploration (find all test files, trace a dependency chain) to a subagent so the main agent keeps coordinating at a high level instead of drowning in raw search output. Summarize key findings from one exploration phase and inject that summary into the next phase's initial context, rather than carrying the full raw exploration forward.

For crash recovery in long-running work, each agent exports its state to a known location; the coordinator loads a manifest on resume and re-injects it into agent prompts, rather than starting over.

> **Exam key point:** `/compact` is the direct tool for reducing context usage mid-session when it's filled with verbose discovery output — a small, specific fact worth remembering alongside the bigger architectural techniques above.

#### Task 5.5 — Human review workflows and confidence calibration (7 min)

An aggregate accuracy number (e.g., '97% overall') can MASK poor performance on a specific document type or field — the average hides the segment that's actually failing. Validate accuracy broken out BY document type and field segment before you trust an aggregate enough to reduce human review based on it.

Stratified random sampling of even HIGH-confidence extractions is how you measure the true error rate and catch novel error patterns you wouldn't find by only auditing the low-confidence tail.

> **Exam key point:** Have the model output field-level confidence scores, then calibrate the review-routing THRESHOLD using a labeled validation set — don't just trust a raw confidence number at face value (echoing the same 'self-reported confidence is unreliable on its own' theme from Task 5.2). Route low-confidence or contradictory-source extractions to human review first, prioritizing scarce reviewer capacity where it has the most impact.

#### Task 5.6 — Provenance and uncertainty in multi-source synthesis (7 min)

Source attribution gets lost during summarization when findings are compressed WITHOUT preserving which claim came from which source. Subagents should output structured claim-source mappings (source URL, document name, relevant excerpt) that downstream agents PRESERVE and merge, rather than flattening everything into unattributed prose.

When credible sources genuinely conflict on a statistic, ANNOTATE the conflict with source attribution for both values — don't arbitrarily pick one. Requiring publication/collection dates in structured outputs prevents a real temporal difference (the numbers changed over time) from being misread as a contradiction between sources.

> **Exam key point:** Structure reports to explicitly distinguish well-established findings from contested ones, preserving each source's original characterization and methodological context — and render different content types appropriately (financial data as tables, news as prose, technical findings as structured lists) rather than flattening everything into one uniform format.


## Practice questions

### D1 — Agentic Architecture & Orchestration

**Q1. (medium) 🏛️ OFFICIAL** Production data shows that in 12% of cases, your agent skips get_customer entirely and calls lookup_order using only the customer's stated name, occasionally leading to misidentified accounts and incorrect refunds. What change would most effectively address this reliability issue?

- ✅ **A.** Add a programmatic prerequisite that blocks lookup_order and process_refund calls until get_customer has returned a verified customer ID.
- — **B.** Enhance the system prompt to state that customer verification via get_customer is mandatory before any order operations.
- — **C.** Add few-shot examples showing the agent always calling get_customer first, even when customers volunteer order details.
- — **D.** Implement a routing classifier that analyzes each request and enables only the subset of tools appropriate for that request type.

*Explanation:* When a specific tool sequence is required for critical business logic (like verifying customer identity before processing refunds), programmatic enforcement provides deterministic guarantees that prompt-based approaches cannot. Options B and C rely on probabilistic LLM compliance, which is insufficient when errors have financial consequences. Option D addresses tool availability rather than tool ordering, which is not the actual problem. _(Source: Official Exam Guide — Sample Question 1 (Scenario: Customer Support Resolution Agent))_

**Q2. (medium) 🏛️ OFFICIAL** After running the system on the topic "impact of AI on creative industries," you observe that each subagent completes successfully: the web search agent finds relevant articles, the document analysis agent summarizes papers correctly, and the synthesis agent produces coherent output. However, the final reports cover only visual arts, completely missing music, writing, and film production. When you examine the coordinator's logs, you see it decomposed the topic into three subtasks: "AI in digital art creation," "AI in graphic design," and "AI in photography." What is the most likely root cause?

- — **A.** The synthesis agent lacks instructions for identifying coverage gaps in the findings it receives from other agents.
- ✅ **B.** The coordinator agent's task decomposition is too narrow, resulting in subagent assignments that don't cover all relevant domains of the topic.
- — **C.** The web search agent's queries are not comprehensive enough and need to be expanded to cover more creative industry sectors.
- — **D.** The document analysis agent is filtering out sources related to non-visual creative industries due to overly restrictive relevance criteria.

*Explanation:* The coordinator's logs reveal the root cause directly: it decomposed "creative industries" into only visual arts subtasks (digital art, graphic design, photography), completely omitting music, writing, and film. The subagents executed their assigned tasks correctly — the problem is what they were assigned. Options A, C, and D incorrectly blame downstream agents that are working correctly within their assigned scope. _(Source: Official Exam Guide — Sample Question 7 (Scenario: Multi-Agent Research System))_

**Q3. (easy)** In an agentic loop built with the Claude Agent SDK, which stop_reason value tells your code to execute a tool and feed the result back into the conversation before continuing the loop?

- — **A.** "end_turn"
- ✅ **B.** "tool_use"
- — **C.** "max_tokens"
- — **D.** There is no stop_reason for this; you must parse the assistant's text

*Explanation:* stop_reason: "tool_use" means Claude wants to call a tool. Your code executes it and appends the result to conversation history for the next iteration. "end_turn" is the signal to stop looping and present the final response. Relying on text-parsing instead of stop_reason is an explicitly named anti-pattern. _(Source: D1 Task 1.1 — Agentic loops for autonomous task execution)_

**Q4. (medium)** A coordinator needs to spawn a subagent using the Claude Agent SDK's Task tool. The coordinator's call fails immediately with an error that the tool isn't available. What is the most likely cause?

- ✅ **A.** The coordinator's allowedTools list doesn't include "Task"
- — **B.** The subagent's AgentDefinition is missing a system prompt
- — **C.** fork_session was not called before invoking the subagent
- — **D.** The subagent needs its own auto memory scope configured first

*Explanation:* A coordinator can only spawn subagents if "Task" is explicitly included in its allowedTools configuration. Without it, the coordinator has no mechanism to invoke the Task tool at all. _(Source: D1 Task 1.3 — Subagent invocation, context passing, and spawning)_

**Q5. (medium)** A synthesis subagent produces a report that omits information the coordinator clearly had available from an earlier research subagent's findings. Investigation shows the coordinator only passed a one-line task description to the synthesis subagent. What is the most likely root cause?

- — **A.** Subagents automatically share context, so the omission must be a model reasoning failure
- ✅ **B.** Subagent context must be explicitly included in the prompt — it is not automatically inherited from the parent or other subagents
- — **C.** The synthesis subagent needs a larger context window
- — **D.** fork_session should have been used instead of the Task tool

*Explanation:* Subagents do not automatically inherit the coordinator's conversation history or another subagent's findings. Complete findings must be included directly in the subagent's prompt — a one-line task description with no supporting data means the subagent genuinely never received the earlier research. _(Source: D1 Task 1.3 — Subagent invocation, context passing, and spawning)_

**Q6. (medium)** Your team needs Claude to normalize timestamps returned from three different MCP tools (one returns Unix time, one ISO 8601, one a custom numeric format) into one consistent shape before the model reasons about them. Which mechanism fits?

- ✅ **A.** A PostToolUse hook that transforms each tool's result before the model processes it
- — **B.** A system prompt instruction asking Claude to convert timestamps itself
- — **C.** tool_choice: "any" to force a specific tool call order
- — **D.** fork_session to run each tool's format through a separate branch

*Explanation:* A PostToolUse hook intercepts a tool's result and can transform it — exactly the pattern for normalizing heterogeneous data formats from different MCP tools before the model ever sees the inconsistency. _(Source: D1 Task 1.5 — Agent SDK hooks for interception and normalization)_

**Q7. (medium)** A code review agent needs to check each file in a PR for style issues in a fixed, predictable order, then run one final pass checking cross-file consistency. Which task decomposition strategy is this?

- — **A.** Dynamic adaptive decomposition
- ✅ **B.** Prompt chaining
- — **C.** fork_session branching
- — **D.** Hub-and-spoke orchestration

*Explanation:* Prompt chaining is a fixed sequential pipeline appropriate for predictable, multi-aspect work — exactly a known, ordered set of per-file passes followed by one integration pass. Dynamic decomposition is for open-ended work where the steps aren't known in advance. _(Source: D1 Task 1.6 — Task decomposition strategies)_

**Q8. (hard)** You're resuming a long-running investigation session, but three of the files it previously analyzed have since been modified. What is the most reliable way to continue?

- — **A.** Use --resume and let the agent re-discover the changes on its own
- ✅ **B.** Use --resume and explicitly tell the agent which files changed so it can target re-analysis
- — **C.** Always start a brand-new session from scratch regardless of how much prior context is still valid
- — **D.** Use fork_session to create a branch that ignores the file changes

*Explanation:* When resuming a session after code has changed, explicitly inform the agent about the specific file changes so it can do targeted re-analysis rather than requiring full re-exploration or silently reasoning from stale prior tool results. _(Source: D1 Task 1.7 — Session state, resumption, and forking)_


### D2 — Tool Design & MCP Integration

**Q1. (medium) 🏛️ OFFICIAL** Production logs show the agent frequently calls get_customer when users ask about orders (e.g., "check my order #12345"), instead of calling lookup_order. Both tools have minimal descriptions ("Retrieves customer information" / "Retrieves order details") and accept similar identifier formats. What's the most effective first step to improve tool selection reliability?

- — **A.** Add few-shot examples to the system prompt demonstrating correct tool selection patterns, with 5-8 examples showing order-related queries routing to lookup_order.
- ✅ **B.** Expand each tool's description to include input formats it handles, example queries, edge cases, and boundaries explaining when to use it versus similar tools.
- — **C.** Implement a routing layer that parses user input before each turn and pre-selects the appropriate tool based on detected keywords and identifier patterns.
- — **D.** Consolidate both tools into a single lookup_entity tool that accepts any identifier and internally determines which backend to query.

*Explanation:* Tool descriptions are the primary mechanism LLMs use for tool selection. When descriptions are minimal, models lack the context to differentiate between similar tools. Option B directly addresses this root cause with a low-effort, high-leverage fix. Few-shot examples (A) add token overhead without fixing the underlying issue. A routing layer (C) is over-engineered and bypasses the LLM's natural language understanding. Consolidating tools (D) is a valid architectural choice but requires more effort than a "first step" warrants when the immediate problem is inadequate descriptions. _(Source: Official Exam Guide — Sample Question 2 (Scenario: Customer Support Resolution Agent))_

**Q2. (hard) 🏛️ OFFICIAL** During testing, you observe that the synthesis agent frequently needs to verify specific claims while combining findings. Currently, when verification is needed, the synthesis agent returns control to the coordinator, which invokes the web search agent, then re-invokes synthesis with results. This adds 2-3 round trips per task and increases latency by 40%. Your evaluation shows that 85% of these verifications are simple fact-checks (dates, names, statistics) while 15% require deeper investigation. What's the most effective approach to reduce overhead while maintaining system reliability?

- ✅ **A.** Give the synthesis agent a scoped verify_fact tool for simple lookups, while complex verifications continue delegating to the web search agent through the coordinator.
- — **B.** Have the synthesis agent accumulate all verification needs and return them as a batch to the coordinator at the end of its pass, which then sends them all to the web search agent at once.
- — **C.** Give the synthesis agent access to all web search tools so it can handle any verification need directly without round-trips through the coordinator.
- — **D.** Have the web search agent proactively cache extra context around each source during initial research, anticipating what the synthesis agent might need to verify.

*Explanation:* Option A applies the principle of least privilege by giving the synthesis agent only what it needs for the 85% common case (simple fact verification) while preserving the existing coordination pattern for complex cases. Option B's batching approach creates blocking dependencies since synthesis steps may depend on earlier verified facts. Option C over-provisions the synthesis agent, violating separation of concerns. Option D relies on speculative caching that cannot reliably predict what the synthesis agent will need to verify. _(Source: Official Exam Guide — Sample Question 9 (Scenario: Multi-Agent Research System))_

**Q3. (medium)** An MCP tool call fails. Your error response returns only {"isError": true, "message": "Operation failed"}. What is the main problem with this response?

- — **A.** isError should be a string, not a boolean
- ✅ **B.** A uniform, generic failure message prevents the agent from deciding whether to retry, explain a policy issue, or take some other recovery action
- — **C.** MCP tools should never return errors, only empty results
- — **D.** The message field is not part of the MCP specification

*Explanation:* A generic 'Operation failed' response gives the agent no way to distinguish a transient error (worth retrying) from a validation error, business-rule violation, or permission error — each of which calls for a different response. Structured metadata (errorCategory, isRetryable) is what enables appropriate recovery. _(Source: D2 Task 2.2 — Structured error responses for MCP tools)_

**Q4. (medium)** A team is deciding where to configure two MCP servers: one used by the whole team for shared internal tooling, and one a single developer wants to experiment with personally. What is the correct scoping?

- — **A.** Both servers in .mcp.json so everyone has access to both
- ✅ **B.** The shared server in .mcp.json (project-level, version-controlled); the personal server in ~/.claude.json (user-level)
- — **C.** Both servers in ~/.claude.json so no configuration is ever committed to the repo
- — **D.** MCP servers cannot be scoped differently per developer

*Explanation:* .mcp.json is project-level and shared via version control — correct for team-wide tooling. ~/.claude.json is user-level and personal — correct for an individual's experimental server that shouldn't be pushed onto the whole team. _(Source: D2 Task 2.4 — Integrating MCP servers into Claude Code and agent workflows)_

**Q5. (easy)** You need to find every file in a repository whose filename matches **/*.config.ts. Which built-in tool is designed for this?

- — **A.** Grep
- ✅ **B.** Glob
- — **C.** Read
- — **D.** Bash with a hand-written find command, since no built-in tool does this

*Explanation:* Glob matches file paths by name/extension pattern. Grep searches file CONTENTS for patterns, which is a different job — a common exam distractor pairing. _(Source: D2 Task 2.5 — Selecting built-in tools effectively)_


### D3 — Claude Code Configuration & Workflows

**Q1. (easy) 🏛️ OFFICIAL** You want to create a custom /review slash command that runs your team's standard code review checklist. This command should be available to every developer when they clone or pull the repository. Where should you create this command file?

- ✅ **A.** In the .claude/commands/ directory in the project repository
- — **B.** In ~/.claude/commands/ in each developer's home directory
- — **C.** In the CLAUDE.md file at the project root
- — **D.** In a .claude/config.json file with a commands array

*Explanation:* Project-scoped custom slash commands should be stored in the .claude/commands/ directory within the repository. These commands are version-controlled and automatically available to all developers when they clone or pull the repo. Option B (~/.claude/commands/) is for personal commands that aren't shared via version control. Option C (CLAUDE.md) is for project instructions and context, not command definitions. Option D describes a configuration mechanism that doesn't exist in Claude Code. _(Source: Official Exam Guide — Sample Question 4 (Scenario: Code Generation with Claude Code))_

**Q2. (medium) 🏛️ OFFICIAL** You've been assigned to restructure the team's monolithic application into microservices. This will involve changes across dozens of files and requires decisions about service boundaries and module dependencies. Which approach should you take?

- ✅ **A.** Enter plan mode to explore the codebase, understand dependencies, and design an implementation approach before making changes.
- — **B.** Start with direct execution and make changes incrementally, letting the implementation reveal the natural service boundaries.
- — **C.** Use direct execution with comprehensive upfront instructions detailing exactly how each service should be structured.
- — **D.** Begin in direct execution mode and only switch to plan mode if you encounter unexpected complexity during implementation.

*Explanation:* Plan mode is designed for complex tasks involving large-scale changes, multiple valid approaches, and architectural decisions — exactly what monolith-to-microservices restructuring requires. It enables safe codebase exploration and design before committing to changes. Option B risks costly rework when dependencies are discovered late. Option C assumes you already know the right structure without exploring the code. Option D ignores that the complexity is already stated in the requirements, not something that might emerge later. _(Source: Official Exam Guide — Sample Question 5 (Scenario: Code Generation with Claude Code))_

**Q3. (hard) 🏛️ OFFICIAL** Your codebase has distinct areas with different coding conventions: React components use functional style with hooks, API handlers use async/await with specific error handling, and database models follow a repository pattern. Test files are spread throughout the codebase alongside the code they test (e.g., Button.test.tsx next to Button.tsx), and you want all tests to follow the same conventions regardless of location. What's the most maintainable way to ensure Claude automatically applies the correct conventions when generating code?

- ✅ **A.** Create rule files in .claude/rules/ with YAML frontmatter specifying glob patterns to conditionally apply conventions based on file paths
- — **B.** Consolidate all conventions in the root CLAUDE.md file under headers for each area, relying on Claude to infer which section applies
- — **C.** Create skills in .claude/skills/ for each code type that include the relevant conventions in their SKILL.md files
- — **D.** Place a separate CLAUDE.md file in each subdirectory containing that area's specific conventions

*Explanation:* Option A is correct because .claude/rules/ with glob patterns (e.g., **/*.test.tsx) allows conventions to be automatically applied based on file paths regardless of directory location — essential for test files spread throughout the codebase. Option B relies on inference rather than explicit matching, making it unreliable. Option C requires manual skill invocation or relies on Claude choosing to load them, contradicting the need for deterministic "automatic" application based on file paths. Option D can't easily handle files spread across many directories since CLAUDE.md files are directory-bound. _(Source: Official Exam Guide — Sample Question 6 (Scenario: Code Generation with Claude Code))_

**Q4. (easy) 🏛️ OFFICIAL** Your pipeline script runs claude "Analyze this pull request for security issues" but the job hangs indefinitely. Logs indicate Claude Code is waiting for interactive input. What's the correct approach to run Claude Code in an automated pipeline?

- ✅ **A.** Add the -p flag: claude -p "Analyze this pull request for security issues"
- — **B.** Set the environment variable CLAUDE_HEADLESS=true before running the command
- — **C.** Redirect stdin from /dev/null: claude "Analyze this pull request for security issues" < /dev/null
- — **D.** Add the --batch flag: claude --batch "Analyze this pull request for security issues"

*Explanation:* The -p (or --print) flag is the documented way to run Claude Code in non-interactive mode. It processes the prompt, outputs the result to stdout, and exits without waiting for user input — exactly what CI/CD pipelines require. The other options reference non-existent features (CLAUDE_HEADLESS environment variable, --batch flag) or use Unix workarounds that don't properly address Claude Code's command syntax. _(Source: Official Exam Guide — Sample Question 10 (Scenario: Claude Code for Continuous Integration))_

**Q5. (medium)** A newly onboarded developer reports that Claude Code isn't following any of the team's coding conventions, even though every other teammate says it works fine for them. What should you check first?

- ✅ **A.** Whether the conventions live in that developer's ~/.claude/CLAUDE.md (user-level, personal) instead of the project-level CLAUDE.md
- — **B.** Whether the developer's machine has enough RAM
- — **C.** Whether the developer needs a different Claude model
- — **D.** Whether .claude/rules/ files support YAML frontmatter on that developer's OS

*Explanation:* The classic diagnostic scenario: instructions that live in user-level configuration are personal and not shared via version control. If a new team member isn't receiving conventions everyone else has, the conventions are almost always in the wrong scope — user-level instead of project-level — not a hardware or model issue. _(Source: D3 Task 3.1 — CLAUDE.md hierarchy, scoping, and modular organization)_

**Q6. (medium)** You're building a skill that performs a verbose, multi-step codebase analysis. You want its exploratory output kept out of the main conversation, with only a summary returned. Which SKILL.md frontmatter option does this?

- — **A.** argument-hint
- — **B.** allowed-tools
- ✅ **C.** context: fork
- — **D.** disableBundledSkills

*Explanation:* context: fork runs the skill in an isolated sub-agent context, so its verbose output doesn't pollute the main conversation — the skill returns only its result back to the parent session. _(Source: D3 Task 3.2 — Custom slash commands and skills)_

**Q7. (hard)** You want a testing convention to apply automatically to every *.test.tsx file, regardless of which directory it's in, without loading that convention into context for files that aren't tests. What's the correct mechanism?

- ✅ **A.** A .claude/rules/ file with YAML frontmatter paths: ["**/*.test.tsx"]
- — **B.** A CLAUDE.md file placed in the root test/ directory
- — **C.** A skill invoked manually before running any tests
- — **D.** A single monolithic CLAUDE.md covering every convention, always loaded

*Explanation:* Path-scoped rules with a glob pattern load only when Claude touches a matching file, regardless of which directory it lives in — exactly what's needed for conventions on files scattered throughout a codebase. Directory-bound CLAUDE.md files can't generalize across scattered locations the same way. _(Source: D3 Task 3.3 — Path-specific rules for conditional convention loading)_

**Q8. (medium)** Test failures keep getting fixed inconsistently because natural-language descriptions of the desired transformation are interpreted differently each time. What single change most reliably improves consistency?

- — **A.** Write a longer, more detailed prose description of the transformation
- ✅ **B.** Provide 2-3 concrete input/output examples of the expected transformation
- — **C.** Switch to a higher-effort reasoning setting
- — **D.** Ask Claude to rate its own confidence in each fix

*Explanation:* Concrete input/output examples communicate an expected transformation far more reliably than additional prose, which is prone to inconsistent interpretation. This is the same iterative-refinement principle behind few-shot prompting applied to a development workflow. _(Source: D3 Task 3.5 — Iterative refinement for progressive improvement)_


### D4 — Prompt Engineering & Structured Output

**Q1. (medium) 🏛️ OFFICIAL** Your team wants to reduce API costs for automated analysis. Currently, real-time Claude calls power two workflows: (1) a blocking pre-merge check that must complete before developers can merge, and (2) a technical debt report generated overnight for review the next morning. Your manager proposes switching both to the Message Batches API for its 50% cost savings. How should you evaluate this proposal?

- ✅ **A.** Use batch processing for the technical debt reports only; keep real-time calls for pre-merge checks.
- — **B.** Switch both workflows to batch processing with status polling to check for completion.
- — **C.** Keep real-time calls for both workflows to avoid batch result ordering issues.
- — **D.** Switch both to batch processing with a timeout fallback to real-time if batches take too long.

*Explanation:* The Message Batches API offers 50% cost savings but has processing times up to 24 hours with no guaranteed latency SLA. This makes it unsuitable for blocking pre-merge checks where developers wait for results, but ideal for overnight batch jobs like technical debt reports. Option B is wrong because relying on "often faster" completion isn't acceptable for blocking workflows. Option C reflects a misconception — batch results can be correlated using custom_id fields. Option D adds unnecessary complexity when the simpler solution is matching each API to its appropriate use case. _(Source: Official Exam Guide — Sample Question 11 (Scenario: Claude Code for Continuous Integration))_

**Q2. (medium) 🏛️ OFFICIAL** A pull request modifies 14 files across the stock tracking module. Your single-pass review analyzing all files together produces inconsistent results: detailed feedback for some files but superficial comments for others, obvious bugs missed, and contradictory feedback — flagging a pattern as problematic in one file while approving identical code elsewhere in the same PR. How should you restructure the review?

- ✅ **A.** Split into focused passes: analyze each file individually for local issues, then run a separate integration-focused pass examining cross-file data flow.
- — **B.** Require developers to split large PRs into smaller submissions of 3-4 files before the automated review runs.
- — **C.** Switch to a higher-tier model with a larger context window to give all 14 files adequate attention in one pass.
- — **D.** Run three independent review passes on the full PR and only flag issues that appear in at least two of the three runs.

*Explanation:* Splitting reviews into focused passes directly addresses the root cause: attention dilution when processing many files at once. File-by-file analysis ensures consistent depth, while a separate integration pass catches cross-file issues. Option B shifts burden to developers without improving the system. Option C misunderstands that larger context windows don't solve attention quality issues. Option D would actually suppress detection of real bugs by requiring consensus on issues that may only be caught intermittently. _(Source: Official Exam Guide — Sample Question 12 (Scenario: Claude Code for Continuous Integration))_

**Q3. (medium)** A review prompt instructs Claude to "be conservative" and "only report high-confidence findings" to reduce false positives, but the false-positive rate hasn't improved. What's the most likely reason?

- ✅ **A.** Vague qualifiers like "be conservative" don't provide checkable criteria — they shift ambiguity rather than resolving it
- — **B.** Claude cannot follow negative instructions
- — **C.** The prompt needs to be much longer
- — **D.** False positives can only be reduced with a separate classifier model

*Explanation:* General instructions like "be conservative" don't define what counts as high-confidence — the ambiguity just moves. Explicit, checkable criteria (e.g., specific conditions for flagging vs. skipping an issue) are what actually reduce false positives. _(Source: D4 Task 4.1 — Explicit criteria to reduce false positives)_

**Q4. (medium)** You're adding few-shot examples to an extraction prompt to reduce hallucinated values. What is the most effective set of examples to include?

- — **A.** 20+ examples covering every possible document format to maximize coverage
- ✅ **B.** 2-4 targeted examples covering genuinely ambiguous or varied-format cases, including examples that distinguish acceptable patterns from real issues
- — **C.** Only examples of successful extractions, since showing failures would confuse the model
- — **D.** One single canonical example repeated in different wordings

*Explanation:* A small number of targeted examples covering ambiguous cases — including examples that show the boundary between acceptable and problematic — generalizes better to novel patterns than a large generic set or examples of only one class. _(Source: D4 Task 4.2 — Few-shot prompting for output consistency)_

**Q5. (hard)** An extraction tool using tool_use with a strict JSON schema never produces malformed JSON, but downstream code still occasionally receives line items that don't sum to the stated invoice total. What does this indicate?

- — **A.** The schema itself is broken and needs to be redefined
- ✅ **B.** tool_use with JSON schema eliminates syntax errors but does not prevent semantic errors — schema compliance is not a correctness guarantee
- — **C.** tool_choice must be set to "any" instead of a forced tool
- — **D.** The model needs a larger context window

*Explanation:* Strict JSON schemas via tool_use guarantee the output is syntactically valid and matches the schema's types, but they cannot verify semantic correctness like values summing correctly or landing in the right field. That requires a separate validation step. _(Source: D4 Task 4.3 — Enforcing structured output with tool_use and JSON schemas)_

**Q6. (medium)** A structured-extraction retry loop keeps failing on the same document even after several retries with error feedback. Investigation shows the required field simply isn't present anywhere in the source document. What should happen?

- — **A.** Keep retrying with increasingly detailed error messages until it succeeds
- ✅ **B.** Recognize this as a case where retries are fundamentally ineffective — the information is absent from the source, not malformed — and route it elsewhere (null the field, or send to human review)
- — **C.** Switch tool_choice to force a different extraction tool
- — **D.** Increase max_tokens and retry

*Explanation:* Retry-with-error-feedback is effective for format and structural mismatches, but useless when information is genuinely absent from the source — no amount of retrying recovers data that was never there. Recognizing this failure mode is what prevents wasted retry cycles. _(Source: D4 Task 4.4 — Validation, retry, and feedback loops for extraction quality)_


### D5 — Context Management & Reliability

**Q1. (medium) 🏛️ OFFICIAL** Your agent achieves 55% first-contact resolution, well below the 80% target. Logs show it escalates straightforward cases (standard damage replacements with photo evidence) while attempting to autonomously handle complex situations requiring policy exceptions. What's the most effective way to improve escalation calibration?

- ✅ **A.** Add explicit escalation criteria to your system prompt with few-shot examples demonstrating when to escalate versus resolve autonomously.
- — **B.** Have the agent self-report a confidence score (1-10) before each response and automatically route requests to humans when confidence falls below a threshold.
- — **C.** Deploy a separate classifier model trained on historical tickets to predict which requests need escalation before the main agent begins processing.
- — **D.** Implement sentiment analysis to detect customer frustration levels and automatically escalate when negative sentiment exceeds a threshold.

*Explanation:* Adding explicit escalation criteria with few-shot examples directly addresses the root cause: unclear decision boundaries. This is the proportionate first response before adding infrastructure. Option B fails because LLM self-reported confidence is poorly calibrated — the agent is already incorrectly confident on hard cases. Option C is over-engineered, requiring labeled data and ML infrastructure when prompt optimization hasn't been tried. Option D solves a different problem entirely; sentiment doesn't correlate with case complexity, which is the actual issue. _(Source: Official Exam Guide — Sample Question 3 (Scenario: Customer Support Resolution Agent))_

**Q2. (medium) 🏛️ OFFICIAL** The web search subagent times out while researching a complex topic. You need to design how this failure information flows back to the coordinator agent. Which error propagation approach best enables intelligent recovery?

- ✅ **A.** Return structured error context to the coordinator including the failure type, the attempted query, any partial results, and potential alternative approaches.
- — **B.** Implement automatic retry logic with exponential backoff within the subagent, returning a generic "search unavailable" status only after all retries are exhausted.
- — **C.** Catch the timeout within the subagent and return an empty result set marked as successful.
- — **D.** Propagate the timeout exception directly to a top-level handler that terminates the entire research workflow.

*Explanation:* Structured error context gives the coordinator the information it needs to make intelligent recovery decisions — whether to retry with a modified query, try an alternative approach, or proceed with partial results. Option B's generic status hides valuable context from the coordinator, preventing informed decisions. Option C suppresses the error by marking failure as success, which prevents any recovery and risks incomplete research outputs. Option D terminates the entire workflow unnecessarily when recovery strategies could succeed. _(Source: Official Exam Guide — Sample Question 8 (Scenario: Multi-Agent Research System))_

**Q3. (medium)** A long customer support conversation gets progressively summarized to save context. After several rounds of summarization, the agent starts giving the wrong refund amount because a specific dollar figure from early in the conversation was compressed into a vague summary. What's the most direct fix?

- ✅ **A.** Extract transactional facts (amounts, dates, order numbers) into a persistent "case facts" block included in every prompt, outside the summarized history
- — **B.** Summarize even more aggressively so the context stays smaller
- — **C.** Switch to a model with a larger context window so summarization is never needed
- — **D.** Ask the customer to repeat the dollar figure at the end of the conversation

*Explanation:* Progressive summarization risks losing exactly the precise numerical details that matter. Extracting critical transactional facts into a persistent block that survives summarization directly prevents this class of error, rather than just delaying it with a bigger context window. _(Source: D5 Task 5.1 — Preserving critical information across long interactions)_

**Q4. (medium)** A research coordinator's web search subagent times out on one of five subtopics. The coordinator has good results for the other four. What is the best way to handle this?

- — **A.** Terminate the entire research workflow since one subtopic failed
- — **B.** Silently return the four successful results as if the research were complete
- ✅ **C.** Proceed with the four successful results and annotate the final output with the coverage gap for the failed subtopic
- — **D.** Retry the failed subtopic indefinitely until the whole workflow can complete

*Explanation:* Both terminating the entire workflow on a single failure and silently presenting a gap as if it were complete coverage are named anti-patterns. The correct approach preserves what succeeded and is explicit about what's missing, so downstream consumers know the report's actual coverage. _(Source: D5 Task 5.3 — Error propagation across multi-agent systems)_

**Q5. (medium)** During a long codebase-exploration session, Claude starts describing a specific class it examined 40 turns ago using generic language like "typical patterns" instead of the class's actual, specific structure. What does this most likely indicate, and what helps?

- — **A.** The model has a bug; switching models is the only fix
- ✅ **B.** Context degradation in the extended session; a scratchpad file recording the earlier concrete finding would let Claude reference it directly instead of reasoning from a degraded working context
- — **C.** This is expected and requires no action
- — **D.** The session needs to be forked, not resumed

*Explanation:* Referencing 'typical patterns' instead of specific earlier findings is the named symptom of context degradation in extended sessions. Scratchpad files that persist key findings across context boundaries are the documented countermeasure. _(Source: D5 Task 5.4 — Managing context in large codebase exploration)_

**Q6. (hard)** An extraction pipeline reports 97% overall accuracy, and the team is ready to remove human review entirely. What should be checked before doing so?

- — **A.** Nothing further — 97% aggregate accuracy is sufficient justification
- ✅ **B.** Whether accuracy is consistent when broken out by document type and field, since an aggregate number can mask poor performance on a specific segment
- — **C.** Whether the model's self-reported confidence score is above 9/10 on average
- — **D.** Whether the extraction ran fast enough to meet the batch SLA

*Explanation:* An aggregate accuracy metric can hide poor performance on a specific document type or field. Validating accuracy by segment — before trusting the aggregate enough to reduce human review — is the documented safeguard against this exact mistake. _(Source: D5 Task 5.5 — Human review workflows and confidence calibration)_

**Q7. (medium)** Two credible sources report different figures for the same statistic in a research synthesis. What should the synthesis output do?

- — **A.** Arbitrarily pick the more recent-sounding source and present only that value
- — **B.** Average the two values to produce a single number
- ✅ **C.** Preserve both values with source attribution, annotated as a conflict, rather than silently resolving it
- — **D.** Omit the statistic entirely to avoid the conflict

*Explanation:* When credible sources genuinely conflict, the correct handling is to annotate the conflict with source attribution for both values, letting the coordinator or reader decide how to reconcile it — not to silently pick one, average them, or drop the information. _(Source: D5 Task 5.6 — Provenance and uncertainty in multi-source synthesis)_


## Glossary

### API

- **/compact** — Reduces context usage mid-session when it has filled with verbose discovery output.
- **Attention dilution** — Processing many files/items in a single pass causes inconsistent depth and contradictory findings on identical patterns. The fix is splitting into focused per-item passes plus a separate integration pass — not a bigger context window, which doesn't address attention quality.
- **Case facts block** — A persistent block of transactional facts (amounts, dates, order numbers) included in every prompt outside the summarized history, so critical numbers survive progressive summarization.
- **Claim-source mapping** — Structured output (source URL, document name, excerpt) that subagents attach to each finding and downstream agents preserve, preventing source attribution from being lost during summarization.
- **Explicit criteria** — Checkable, specific instructions ('flag only when claimed behavior contradicts actual code behavior') reduce false positives; vague qualifiers ('be conservative') do not — they just relocate the ambiguity.
- **Few-shot prompting** — 2-4 targeted examples for genuinely ambiguous scenarios generalize better than a large generic set. Most effective when detailed prose instructions alone produce inconsistent output, and when examples demonstrate the boundary between acceptable and problematic cases.
- **Lost-in-the-middle effect** — Models reliably process the beginning and end of long inputs but may under-weight findings buried in the middle. Mitigate by placing key findings at the start of aggregated inputs with explicit section headers.
- **Message Batches API** — 50% cost savings, up to 24-hour processing window, no guaranteed latency SLA, no multi-turn tool calling within a request. Fits non-blocking latency-tolerant workloads; wrong for blocking workflows regardless of cost savings. custom_id correlates request/response pairs.
- **Nullable / optional schema fields** — Design extraction fields as optional when a source document may not contain that information. A required field the model can't fill gets fabricated to satisfy the schema — worse than returning null.
- **Retry effectiveness limits** — Retry-with-error-feedback fixes format/structural mismatches. It cannot recover information that is genuinely absent from the source document — recognizing which case you're in determines whether to retry or route elsewhere.
- **Scratchpad files** — Persist key findings across context boundaries during extended sessions, countering context degradation — the symptom where a model starts describing earlier specific findings in vague, generic terms.
- **Self-reported confidence (unreliable)** — Model self-reported confidence scores and customer sentiment are both poor proxies for actual case complexity — a recurring theme across escalation and human-review task statements. Explicit criteria and calibration against labeled data are the reliable alternatives.
- **Semantic vs. syntax error** — tool_use with a strict JSON schema eliminates syntax errors (malformed JSON) but not semantic errors (values that don't sum correctly, or land in the wrong field) — schema compliance is a floor, not a correctness guarantee.
- **Stratified random sampling** — Sampling even high-confidence extractions (not just the low-confidence tail) to measure true error rate and catch novel error patterns an aggregate accuracy number would mask.

### Agents

- **--resume** — Continues a specific named prior conversation. Best used when prior context is still mostly valid; starting a fresh session with an injected summary is often more reliable when prior tool results are stale.
- **AgentDefinition** — Configuration for a subagent type: its description, system prompt, and tool restrictions.
- **Agentic loop** — Send a request, inspect stop_reason, execute any requested tool, append the result to conversation history, repeat until end_turn. The tool results in context are what let the model reason about its next action.
- **Hub-and-spoke orchestration** — A coordinator agent manages all inter-subagent communication, error handling, and information routing — subagents don't talk to each other directly, giving observability and consistent error handling.
- **PostToolUse hook** — Intercepts a tool's result and can transform it before the model processes it — e.g. normalizing inconsistent timestamp formats returned by different MCP tools.
- **Programmatic enforcement** — Using hooks or prerequisite gates to guarantee a required order of operations or a business rule, in contrast to prompt-based guidance, which has a non-zero failure rate. Required whenever compliance must be deterministic (e.g. identity verification before a refund).
- **Prompt chaining** — A fixed, sequential task decomposition for predictable multi-aspect work (e.g. per-file analysis, then one cross-file integration pass) — contrast with dynamic adaptive decomposition for open-ended investigation.
- **Task tool** — The Claude Agent SDK mechanism for spawning subagents. A coordinator's allowedTools must explicitly include "Task" or it cannot invoke subagents.
- **Tool call interception hook** — Intercepts an outgoing tool call before it executes, to enforce a compliance rule deterministically (e.g. blocking a refund tool call above a threshold amount).
- **fork_session** — Creates an independent branch from a shared analysis baseline, letting you explore divergent approaches (e.g. comparing two refactoring strategies) without the branches interfering with each other.
- **stop_reason** — The field on a Claude response that drives agentic-loop control flow. "tool_use" means execute the requested tool and continue the loop; "end_turn" means present the response and stop. Parsing assistant text instead of checking stop_reason is a named anti-pattern.

### Claude Code

- **--output-format json / --json-schema** — Enforces machine-parseable structured output in CI, e.g. for posting findings as inline PR comments programmatically.
- **-p / --print flag** — Runs Claude Code non-interactively. Required in CI/CD pipelines — without it, a script hangs waiting for interactive input that will never come.
- **.claude/rules/** — Topic-specific rule files, alternative to a monolithic CLAUDE.md. With YAML frontmatter paths glob patterns, a rule loads only when Claude works with a matching file — the correct mechanism for conventions that must apply regardless of directory location (e.g. all *.test.tsx files).
- **@import** — References external files from CLAUDE.md to keep it modular — e.g. a monorepo package importing only the standards files relevant to it.
- **CLAUDE.md hierarchy** — User-level (~/.claude/CLAUDE.md, personal, not version-controlled), project-level (.claude/CLAUDE.md or root CLAUDE.md, team-shared), and directory-level scopes. A new teammate missing conventions everyone else has is almost always a user-vs-project scoping bug.
- **Custom slash commands** — Project-scoped commands live in .claude/commands/ (version-controlled, team-shared). User-scoped commands live in ~/.claude/commands/ (personal).
- **Explore subagent** — Isolates verbose discovery output (searching, reading many files) and returns a summary, preserving the main conversation's context during multi-phase investigation.
- **Interview pattern** — Having Claude ask clarifying questions before implementing, to surface considerations you may not have anticipated — useful in unfamiliar domains.
- **Plan mode** — For complex tasks with large-scale changes, multiple valid approaches, or architectural decisions — enables safe exploration and design before committing to changes. Direct execution fits simple, well-scoped changes with a clear path.
- **Session context isolation** — A session that just generated code retains its own reasoning context, making it less likely to question its own decisions in review. An independent review instance (without that prior context) catches more subtle issues.
- **Skill frontmatter** — SKILL.md options: context: fork (run in isolated sub-agent context, keeping verbose output out of the main conversation), allowed-tools (restrict tool access during skill execution), argument-hint (prompt for required parameters).

### MCP

- **.mcp.json** — Project-level MCP server configuration, shared via version control, supporting environment-variable expansion (e.g. ${GITHUB_TOKEN}) for credentials that shouldn't be committed.
- **Access failure vs. valid empty result** — A timeout or unreachable service (needs a retry decision) is a different situation from a successful query that legitimately found nothing (needs no retry). Conflating the two either wastes retries or hides a real failure as 'no results.'
- **Grep vs. Glob** — Grep searches file CONTENTS for patterns (function names, error messages). Glob matches file PATHS by name/extension pattern. A common exam distractor pairing.
- **MCP resources** — Expose content catalogs (issue summaries, doc hierarchies, database schemas) so an agent has visibility into available data without needing exploratory tool calls to discover it.
- **Scoped tool access** — Giving each agent only the tools its role needs, with a small number of deliberately scoped cross-role tools for high-frequency needs — too many tools (e.g. 18 instead of 4-5) degrades tool-selection reliability, and out-of-specialization tools tend to get misused.
- **Tool description** — The primary mechanism an LLM uses to select which tool to call. Minimal descriptions cause unreliable selection between similar tools — the single highest-leverage fix for tool-selection problems is almost always a better description.
- **errorCategory** — Structured classification of an MCP tool failure: transient (worth retrying), validation (invalid input), business (policy violation, not retryable), or permission.
- **isError flag** — MCP's mechanism for a tool to signal failure back to the agent. A uniform, generic error message defeats its purpose — structured metadata (errorCategory, isRetryable) is what enables the agent to recover appropriately.
- **tool_choice** — auto (model decides whether/which tool to call), any (must call some tool, model picks which), or forced selection {"type":"tool","name":"..."} (must call this specific tool).
- **~/.claude.json** — User-level MCP server configuration for personal or experimental servers — not shared with the team.
