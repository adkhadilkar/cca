# Knowledge Base — Claude Certified Architect (Foundations)

Generated from `content/packs/ccar-f/*.json` — the single source of truth. Regenerate this file after editing the JSON rather than hand-editing it directly.

**Read `content/SOURCES.md` first** for provenance and known gaps (D2 and D1's workflow-pattern lesson are based on trained knowledge, not a fresh fetch this session, because `code.claude.com` and the Anthropic engineering blog were unreachable).

## Exam overview

- **CCAR-F** — 60 questions, 120 min, pass score 720/1000

| Domain | Title | Weight |
|---|---|---|
| D1 | Agentic Architecture & Orchestration | 27% |
| D2 | Claude Code Configuration & Workflows | 20% |
| D3 | Prompt Engineering & Structured Output | 20% |
| D4 | Tool Design & MCP Integration | 18% |
| D5 | Context Management | 15% |

### What to expect
- Anthropic Academy hosts free courses that map to the exam domains (Claude API, MCP, Claude Code). Work through them alongside this app.
- The exam tests design judgment, not memorization: expect scenarios where you must choose the simplest architecture that meets the requirement.
- Practice reasoning about trade-offs — cost vs. latency, workflow vs. agent, caching vs. compaction — rather than recalling exact parameter names.

> Unofficial preparation material. Not affiliated with, authored by, or endorsed by Anthropic. Exam format, domain weights, question counts, and the passing score are set by Anthropic and may change — always confirm current details on Anthropic's official certification pages before your exam.

## Lessons

### D1 — Agentic Architecture & Orchestration (27%)

_Defining agentic systems; agents vs. workflows vs. conversational systems; the augmented LLM; workflow patterns (prompt chaining, routing, parallelization, orchestrator-workers, evaluator-optimizer); coordinator/subagent multiagent orchestration; error handling and when NOT to use agents._

#### D1.1 Workflows vs. agents vs. the augmented LLM (8 min)

The foundation of agentic design is a single distinction. **Workflows** are systems where LLMs and tools are orchestrated through predefined code paths — you decide the steps in advance. **Agents** are systems where the LLM dynamically directs its own process and tool usage, deciding at runtime what to do next and when it is done.

Both are built on the **augmented LLM**: a model enhanced with retrieval (fetching relevant information), tools (calling functions), and memory (persisting state). The augmented LLM is the primitive; workflows and agents are ways of composing it.

> **Exam key point:** The exam rewards choosing the SIMPLEST thing that works. Reach for a single well-prompted LLM call first, then a workflow, and only then an autonomous agent. Agents trade higher cost, latency, and unpredictability for flexibility — use them only when the task genuinely requires dynamic decision-making over an unknown number of steps.

Use a **workflow** when the task decomposes into fixed, predictable steps (e.g. classify → route → respond). Use an **agent** when the path can't be hardcoded — the number and order of steps depends on inputs discovered along the way, such as an autonomous coding task that explores a repo before editing.

A **conversational system** simply exchanges turns with a user and may call tools, but does not pursue a goal autonomously across many steps. Knowing which of the three a scenario describes is often the whole question.

#### D1.2 The five workflow patterns (10 min)

Anthropic's 'Building effective agents' guidance names five composable workflow patterns. Recognize each by its shape.

**1. Prompt chaining** — decompose a task into a fixed sequence of steps, each LLM call processing the previous output. Add programmatic 'gate' checks between steps. Use when a task cleanly splits into ordered subtasks (e.g. write an outline, then write the doc). Trades latency for accuracy.

**2. Routing** — classify an input, then direct it to a specialized follow-up prompt or model. Use when inputs fall into distinct categories better handled separately (e.g. refund vs. technical-support tickets), or to send easy queries to a cheaper model and hard ones to a stronger one.

**3. Parallelization** — run subtasks simultaneously. Two variants: **sectioning** splits a task into independent parts run in parallel (e.g. one call answers, another screens for policy violations); **voting** runs the same task several times to get diverse outputs and aggregates them (e.g. multiple code-review passes).

**4. Orchestrator-workers** — a central LLM dynamically breaks a task into subtasks, delegates them to worker LLMs, and synthesizes the results. Unlike parallelization, the subtasks are NOT known up front — the orchestrator decides them at runtime. Good for coding changes that touch an unpredictable set of files.

**5. Evaluator-optimizer** — one LLM generates a response while another evaluates it and gives feedback in a loop, iterating until the evaluator is satisfied. Use when you have clear evaluation criteria and iterative refinement measurably helps (e.g. literary translation, complex search).

> **Exam key point:** Orchestrator-workers vs. parallelization is a classic distinction: parallelization runs a FIXED, predefined set of subtasks; the orchestrator DECIDES the subtasks dynamically. If the scenario says 'the number of subtasks isn't known in advance,' it's orchestrator-workers.

#### D1.3 Multiagent orchestration: coordinators and subagents (9 min)

A **coordinator** (orchestrator) agent delegates work to a roster of **subagents**, each with its own model, system prompt, tools, and context. Delegating to context-isolated subagents improves output quality (each stays focused) and can cut wall-clock time by running work in parallel.

Three delegation patterns work well: **parallelization** (fan out independent subtasks and synthesize), **specialization** (route to a domain-focused agent — a security agent, a docs agent — instead of one agent loaded with every capability), and **escalation** (consult a more capable model for the hard subset of subtasks).

```text
Coordinator (Opus)
 ├── Researcher (Haiku)  ← own tools + MCP servers, isolated context
 ├── Reviewer  (Opus)
 └── Test-writer(Sonnet)
Each subagent runs in its own context-isolated thread; the coordinator synthesizes results.
```

In Anthropic's Managed Agents, each agent runs in its own **session thread** (isolated event stream and history). Tools, MCP servers, and context are NOT shared between agents; the shared sandbox filesystem and session-level vault credentials are. A coordinator can delegate only ONE level deep, list at most 20 unique agents in its roster, and a session supports at most 25 concurrent threads.

> **Exam key point:** Don't reach for multiagent by default. It multiplies token cost and coordination overhead. Use it when work spans multiple surfaces or when several well-scoped subtasks each benefit from isolated context and specialized configuration.

#### D1.4 Reliability: error handling, guardrails, and human oversight (7 min)

Autonomous agents accrue error over many steps, so reliability is a design concern, not an afterthought. Give the agent a way to recover: return tool errors as structured `tool_result` blocks with `is_error: true` and a useful message so the model can retry or adapt, rather than throwing and killing the loop.

Bound the loop. Set a maximum number of iterations or a token/time budget so a confused agent can't run forever. Define a clear stopping condition — the agent should know what 'done' looks like.

Add checkpoints for consequential actions. Human-in-the-loop approval before irreversible or costly operations (sending email, deleting data, spending money) is a standard guardrail. In agent harnesses this shows up as tool-permission confirmations.

> **Exam key point:** Keep the agent's action space small and its tools well-documented and tested in a sandbox before granting real-world access. Most agent failures trace back to poorly described tools or an over-broad action space, not to the model 'being wrong.'


### D2 — Claude Code Configuration & Workflows (20%)

_CLAUDE.md memory hierarchy and imports; settings and permissions; hooks and lifecycle events; subagents; slash commands; Agent Skills; MCP in Claude Code; headless/print mode for CI and automation._

#### D2.1 CLAUDE.md and the memory hierarchy (8 min)

`CLAUDE.md` is a special file Claude Code pulls into context automatically at the start of a session. Use it for durable project knowledge: build/test commands, architecture notes, conventions, and 'gotchas' you'd tell a new teammate.

Memory is layered, most-specific wins on conflicts, and all applicable layers are combined:

```text
Enterprise policy   (system-wide managed file)      broadest
Project memory      ./CLAUDE.md (checked into git, shared)
User memory         ~/.claude/CLAUDE.md (all your projects)
Project-local       CLAUDE.local.md (deprecated; use imports)   narrowest
```

Files can pull in others with `@path/to/file` imports, letting you keep CLAUDE.md short and reference deeper docs on demand. Project memory is committed so the whole team shares it; user memory is personal and applies across every project you open.

> **Exam key point:** CLAUDE.md is guidance the model SHOULD follow but MAY not, because it is instruction text. When you need something to happen deterministically — every time, no exceptions — use a hook instead (next lesson). This 'guidance vs. determinism' contrast is a frequent exam point.

#### D2.2 Hooks and lifecycle events (9 min)

Hooks are user-defined shell commands that run at fixed points in Claude Code's lifecycle. Unlike CLAUDE.md instructions, hooks are **deterministic**: they always execute. Configure them in settings (e.g. `.claude/settings.json`).

Key lifecycle events include: **PreToolUse** (before a tool runs — can block it), **PostToolUse** (after a tool succeeds — e.g. auto-format edited files), **UserPromptSubmit** (when you submit a prompt — can inject context or validate), **Notification**, **Stop** and **SubagentStop** (when the agent/subagent finishes), **SessionStart** and **SessionEnd**, and **PreCompact** (before context is compacted).

```json
{
  "hooks": {
    "PostToolUse": [{
      "matcher": "Edit|Write",
      "hooks": [{"type": "command", "command": "prettier --write $CLAUDE_FILE_PATHS"}]
    }]
  }
}
```

A PreToolUse hook's exit code controls flow: exit 0 allows the tool, a non-zero blocking exit stops it and feeds the reason back to Claude. This is how you enforce policy — for example, blocking edits to protected paths or refusing to run destructive commands.

> **Exam key point:** Reach for a hook when you need a guarantee: run tests after every change, format on save, block secrets from being committed. Reach for CLAUDE.md when you want to guide judgment. Deterministic → hook; advisory → memory.

#### D2.3 Subagents, slash commands, and Skills (8 min)

**Subagents** are specialized assistants Claude Code can delegate to, each defined by a markdown file (in `.claude/agents/`) with frontmatter declaring its name, description, allowed tools, and system prompt. A subagent runs with its OWN context window, which keeps the main thread's context clean — delegate large searches or reviews to a subagent so their file dumps don't crowd the main conversation.

**Slash commands** are reusable prompt templates stored as markdown in `.claude/commands/` (project) or `~/.claude/commands/` (personal). Typing `/name` expands the template; `$ARGUMENTS` injects parameters. Use them to standardize repeated workflows like `/review` or `/write-tests`.

**Agent Skills** package instructions, metadata, and optional scripts/resources into a folder with a `SKILL.md` file. They are filesystem-based and load on demand, giving Claude domain expertise without repeating guidance every conversation. Skills work across Claude Code, the API, and claude.ai.

> **Exam key point:** Subagent = isolated context + delegation; Slash command = reusable prompt you invoke; Skill = packaged, auto-loaded capability. Match the tool to the need: context isolation, prompt reuse, or reusable expertise.

#### D2.4 Headless mode, permissions, and CI (7 min)

Headless (print) mode runs Claude Code non-interactively for scripts and CI: `claude -p "<prompt>"`. Add `--output-format json` (or `stream-json`) to get machine-readable results you can parse in a pipeline.

Permissions govern what Claude may do without asking. Modes range from prompting on every action to accepting edits automatically; you can allow/deny specific tools and command patterns in settings. In CI you typically pre-authorize a narrow allowlist so runs don't hang waiting for confirmation.

```bash
# Non-interactive review in CI, JSON output
claude -p "Review the diff on this branch and list correctness bugs" \
  --output-format json > review.json
```

> **Exam key point:** Headless mode + a tight permission allowlist + hooks is the standard recipe for automation (PR review bots, scheduled agents). Never grant broad auto-approval in an environment that can reach production.


### D3 — Prompt Engineering & Structured Output (20%)

_Clear-and-direct prompting; system prompts and roles; multishot (few-shot) examples; chain-of-thought and extended thinking; XML tags; prefilling; and guaranteeing machine-readable output with structured outputs and strict tool use._

#### D3.1 Clear-and-direct prompting, roles, and examples (8 min)

The highest-leverage prompting technique is being explicit. Tell Claude exactly what you want, the audience, the format, and what to avoid. Ambiguity is the main cause of disappointing output — if a smart colleague would need to ask a clarifying question, the prompt is underspecified.

Use the **system prompt** to set role and durable behavior ('You are a senior tax accountant…'); use the `messages` for the task itself. A well-chosen role sharpens tone and domain accuracy.

**Multishot (few-shot)** prompting — including 3–5 well-chosen examples of input→desired output — is one of the most reliable ways to shape format and edge-case handling. Examples often beat lengthy prose instructions for consistency.

> **Exam key point:** Order of impact for most tasks: be clear and direct → give examples → let Claude think (chain-of-thought) → use XML structure → assign a role → prefill. Know this rough hierarchy; questions often ask for the FIRST/most effective technique to try.

#### D3.2 Chain-of-thought, XML tags, and prefilling (8 min)

**Chain-of-thought**: asking Claude to reason step by step before answering improves accuracy on math, logic, and multi-constraint tasks. You can prompt it explicitly ('think step by step') or give a structure to think inside (e.g. a `<thinking>` section). The trade-off is more output tokens and latency.

**Extended thinking** is a model capability where Claude produces internal reasoning (thinking) tokens before its answer, with a configurable budget that is a subset of `max_tokens` and billed as output. Use it for genuinely hard reasoning; it is distinct from simply prompting for step-by-step text.

**XML tags** delimit parts of a prompt so Claude can tell instructions from data from examples — e.g. wrap a document in `<document>…</document>` and the question in `<question>…</question>`. This reduces the chance Claude confuses reference material for commands and makes outputs easier to parse.

**Prefilling** puts the first tokens of the assistant turn into Claude's mouth. Prefill `{` to push toward JSON, or a bracket/heading to enforce a format. Prefilling steers structure cheaply but is a weaker guarantee than structured outputs.

> **Exam key point:** Chain-of-thought and extended thinking are not the same: CoT is a prompting technique (visible reasoning text you request); extended thinking is a model feature with a token budget. Don't conflate them.

#### D3.3 Guaranteeing machine-readable output (8 min)

When downstream code parses Claude's output, prompting for JSON is not enough — the model can still emit malformed or extra text. **Structured outputs** solve this with constrained decoding, guaranteeing the response conforms to a schema.

Two complementary features: **strict tool use** (`strict: true`) guarantees tool names and inputs validate against your `input_schema`; and an output format (`output_config.format`) constrains Claude's final text response to a JSON Schema you supply. Both remove a class of parsing bugs.

```text
Weakest → strongest guarantee of structure:
  prompt for JSON  <  prefill '{'  <  strict tool use / output schema (constrained decoding)
```

> **Exam key point:** If a scenario requires OUTPUT THAT NEVER BREAKS THE PARSER, the answer is structured outputs / strict tool use, not 'ask nicely for JSON' or prefilling. Prefilling improves the odds; constrained decoding guarantees the shape.


### D4 — Tool Design & MCP Integration (18%)

_How tool use works (the tool_use / tool_result round trip); client vs. server tools; writing effective tool schemas and descriptions; tool_choice and parallel tool use; the Model Context Protocol (hosts/clients/servers, transports); and the MCP connector._

#### D4.1 How tool use works (8 min)

Tool use lets Claude call functions you define. The round trip: you send `messages` plus a list of tools (each with a `name`, `description`, and `input_schema`). If Claude decides to use one, it stops with `stop_reason: "tool_use"` and returns one or more `tool_use` blocks naming the tool and its arguments.

Your application executes the tool, then sends a follow-up request appending a `tool_result` block (referencing the `tool_use_id`) with the output. Claude reads the result and produces its final answer — or calls another tool. Tool errors are returned as a `tool_result` with `is_error: true` so Claude can recover.

```text
1. Request: messages + tools
2. Response: stop_reason=tool_use, tool_use{ id, name, input }
3. You run the function
4. Request: append tool_result{ tool_use_id, content }
5. Response: final answer (or another tool_use)
```

**Client tools** run in YOUR application (user-defined tools, plus Anthropic-schema tools like `bash` and `text_editor`). **Server tools** (like `web_search`, `web_fetch`, `code_execution`) run on Anthropic's infrastructure — you get results directly without executing anything.

> **Exam key point:** Client tool → Claude asks, YOU execute, YOU return a tool_result. Server tool → Anthropic executes, results come back inline. Mixing them: if a server tool is called in the same parallel group as a client tool, handling changes — watch the stop reason.

#### D4.2 Designing effective tools (9 min)

A tool is defined by three things: a `name` matching `^[a-zA-Z0-9_-]{1,64}$`, a plaintext `description`, and a JSON Schema `input_schema` for its parameters. Optionally, `input_examples` provide schema-validated example inputs for complex tools.

The single biggest driver of tool performance is the **description**. Explain what the tool does, when to use it, what it returns, and any caveats or limits. Aim for at least 3–4 sentences — more for complex tools. Poor tool use is far more often a description problem than a model problem.

Design the schema for clarity: descriptive parameter names, enums for constrained values, required vs. optional marked correctly, and per-field descriptions. Use meaningful namespacing in tool names when tools span services (`github_list_prs`, `slack_send_message`) so selection stays unambiguous as the library grows.

Control invocation with `tool_choice`: `auto` (Claude decides), `any` (must use some tool), `tool` (force a specific one), or `none`. Set `disable_parallel_tool_use: true` to force one tool call at a time when order matters or your executor isn't safe for concurrency.

> **Exam key point:** When tool use misbehaves, improve the DESCRIPTION and schema before blaming the prompt or model. 'Give extremely detailed descriptions' is the official #1 recommendation.

#### D4.3 The Model Context Protocol (MCP) (9 min)

MCP is an open standard for connecting AI applications to external tools, data, and prompts — 'a USB-C port for AI.' It standardizes how a model discovers and calls capabilities so the same server works across many hosts.

Architecture: an **MCP host** (the AI app, e.g. Claude Code or the Claude API) runs one or more **MCP clients**, each maintaining a 1:1 connection to an **MCP server**. Servers expose three primitives: **tools** (functions the model can call), **resources** (data/context the app can read), and **prompts** (reusable templates).

Transports: **stdio** for local servers (the host launches the server as a subprocess) and **Streamable HTTP** (with SSE) for remote servers. Remote servers must be publicly reachable over HTTP; local stdio servers can't be connected to directly by the API connector.

The **MCP connector** lets the Messages API talk to remote MCP servers without you running a separate MCP client. You pass an `mcp_servers` array (URL + auth), it supports OAuth bearer tokens, and you can allowlist/denylist individual tools via `tool_configuration`. It currently requires a beta header.

> **Exam key point:** Memorize the primitives (tools, resources, prompts) and the host→client→server relationship (one client per server, 1:1). 'Local = stdio, remote = Streamable HTTP/SSE' is a common distractor to get right.


### D5 — Context Management (15%)

_Context windows and what counts toward them; prompt caching (breakpoints, TTLs, pricing); server-side compaction; context editing (tool-result and thinking clearing); the memory tool; and cost/latency levers like batch processing and streaming._

#### D5.1 The context window and what counts (7 min)

The context window is the total tokens a model can consider at once. EVERYTHING counts toward it: the system prompt, every message (including tool results, images, and documents), your tool definitions, and the output Claude generates — including extended-thinking tokens.

Sizes vary by model. Current large models (e.g. Claude Opus 4.8/4.7/4.6, Sonnet 5, Fable 5, Mythos 5) offer a 1M-token context window; older models such as Sonnet 4.5 have 200K. Each response's `usage` field reports exactly what the request consumed; use the token-counting API to estimate before sending.

> **Exam key point:** Longer context is not free quality. As conversations grow, response quality can degrade and cost/latency rise — which is why compaction and context editing exist. 'Just use the 1M window' is usually the wrong answer for a long-running agent; curate context instead.

#### D5.2 Prompt caching (9 min)

Prompt caching stores a prompt prefix so repeated requests that share it skip re-processing — cutting cost and latency. You mark a cacheable boundary with `cache_control: {type: "ephemeral"}` on a content block. Caching covers the whole prefix in order: `tools`, then `system`, then `messages`, up to and including the marked block.

Pricing (relative to base input tokens): a 5-minute cache WRITE costs 1.25×, a 1-hour write costs 2×, and every cache READ (hit) costs 0.1×. The default TTL is 5 minutes and refreshes for free on each use; a 1-hour TTL is available at higher write cost. Cache breakpoints themselves are free — you only pay for what is written and read.

You can define up to **4 cache breakpoints**, with a ~20-block lookback for cache hits and a per-platform minimum cacheable prompt length. Automatic caching (a single top-level `cache_control`) places the breakpoint on the last cacheable block and moves it forward as the conversation grows — but it consumes one of the 4 slots.

> **Exam key point:** Put the cache breakpoint AFTER your large, STABLE content (system prompt, tool defs, big documents) and BEFORE the parts that change each turn. Caching the changing tail wastes writes and never gets a hit — a favorite scenario trap.

#### D5.3 Compaction, context editing, and memory (9 min)

**Server-side compaction** is the recommended way to manage long-running conversations. When input tokens hit a configured threshold, the API summarizes older turns into a `compaction` block and continues from the summary; on later requests it automatically drops content before that block. You keep your full history client-side and just append responses.

**Context editing** selectively clears content as context grows. `clear_tool_uses_20250919` removes the oldest tool results (optionally the tool inputs too) once you pass a threshold, replacing them with placeholder text — ideal for tool-heavy agents where old file contents are no longer needed. `clear_thinking_20251015` manages `thinking` blocks. Both run server-side; your client keeps the full, unmodified history.

The **memory tool** lets Claude persist information outside the context window — writing notes to a store it can read back later — so knowledge survives across turns and sessions without bloating the window. Memory endpoints use their own beta header.

> **Exam key point:** Compaction = summarize old context automatically (best default for long agents). Context editing = surgically clear tool results/thinking. Memory tool = persist facts OUTSIDE the window. Caching = reuse a stable prefix. Know which lever each scenario calls for — they solve different problems.

#### D5.4 Cost and latency levers: batch and streaming (6 min)

**Batch processing** (Message Batches API) handles large volumes asynchronously at **50% lower cost**. A batch holds up to 100,000 requests or 256 MB, whichever comes first; most finish within 1 hour, and results are available when done or after 24 hours (batches expire if not complete within 24 hours). Use it for non-urgent work: evaluations, bulk classification, content moderation.

**Streaming** (SSE) returns tokens as they are generated, improving perceived latency for interactive UIs and long outputs. It doesn't reduce total tokens or cost, but the user sees output sooner and you can render progressively.

> **Exam key point:** Batch = cheaper + asynchronous (throughput, not speed). Streaming = faster PERCEIVED latency (not cheaper). Pick batch when latency doesn't matter and volume is high; pick streaming for interactive experiences.


## Practice questions

### D1 — Agentic Architecture & Orchestration

**Q1. (easy, single-answer)** A team wants Claude to always run: classify the incoming ticket, then route it to one of three fixed follow-up prompts based on the category. The set of steps is known in advance and never changes. What is this an example of?

- — **A.** An autonomous agent
- ✅ **B.** The routing workflow pattern
- — **C.** Orchestrator-workers
- — **D.** Evaluator-optimizer

*Explanation:* Routing classifies an input and directs it to one of several specialized, predefined follow-up prompts. Because the steps and destinations are fixed in advance, this is a workflow, not an autonomous agent — the LLM isn't deciding its own process. _(Source: D1 Lesson 2 — The five workflow patterns)_

**Q2. (medium, single-answer)** A code-migration task needs to touch an unknown set of files discovered only by exploring the repository first. Which pattern best fits?

- — **A.** Parallelization (sectioning)
- — **B.** Prompt chaining
- ✅ **C.** Orchestrator-workers
- — **D.** Routing

*Explanation:* Orchestrator-workers is for tasks where the subtasks can't be predefined — a central LLM dynamically decides what work is needed and delegates it. Parallelization requires the subtasks to be known ahead of time; here the set of files to touch is discovered at runtime. _(Source: D1 Lesson 2 — The five workflow patterns)_

**Q3. (medium, multi-answer)** Which of the following are true about running multiple subagents under a coordinator in Anthropic's Managed Agents? (Select all that apply)

- ✅ **A.** Each subagent runs in its own context-isolated session thread
- — **B.** Tools and MCP servers are automatically shared across all agents in the roster
- ✅ **C.** The coordinator can delegate to a maximum of one level of agents
- ✅ **D.** A session supports at most 25 concurrent threads

*Explanation:* Each agent gets its own isolated thread and configuration (model, prompt, tools, MCP servers) — tools and context are NOT shared automatically, only the sandbox filesystem and session-level vault credentials are. Delegation depth is capped at one level, and a session allows at most 25 concurrent threads. _(Source: D1 Lesson 3 — Multiagent orchestration)_

**Q4. (easy, single-answer)** According to Anthropic's guidance on building effective agents, what should you try FIRST before reaching for a multi-step workflow or an autonomous agent?

- ✅ **A.** A single, well-optimized LLM call
- — **B.** An orchestrator-workers system
- — **C.** A multiagent coordinator with at least 3 subagents
- — **D.** Evaluator-optimizer with two models

*Explanation:* The guidance is to find the simplest solution possible, and only increase complexity when a simpler solution falls short. A single well-prompted LLM call is the starting point; workflows and agents add cost, latency, and unpredictability and should be justified by the task. _(Source: D1 Lesson 1 — Workflows vs. agents vs. the augmented LLM)_

**Q5. (medium, single-answer)** A tool call inside an agent loop fails (e.g. the file doesn't exist). What is the recommended way to handle this so the agent can recover?

- — **A.** Silently drop the tool call and continue with the next step
- — **B.** Terminate the agent loop immediately to avoid compounding errors
- ✅ **C.** Return a tool_result block with is_error true and a useful message, so Claude can adapt
- — **D.** Retry the exact same tool call up to 10 times before giving up

*Explanation:* Errors should be surfaced back to the model as structured tool results (is_error: true) with enough information for Claude to adapt or retry sensibly, rather than crashing the loop or hiding the failure. _(Source: D1 Lesson 4 — Reliability: error handling, guardrails, and human oversight)_

**Q6. (hard, single-answer)** A team runs the same summarization prompt three times and picks the best of three outputs by majority agreement. Which workflow pattern is this?

- ✅ **A.** Parallelization — voting
- — **B.** Parallelization — sectioning
- — **C.** Evaluator-optimizer
- — **D.** Prompt chaining

*Explanation:* Voting runs the same task multiple times to get diverse outputs, then aggregates them (e.g. by majority) — distinct from sectioning, which splits a task into independent parts run in parallel. _(Source: D1 Lesson 2 — The five workflow patterns)_


### D2 — Claude Code Configuration & Workflows

**Q1. (easy, single-answer)** You need Claude Code to ALWAYS run the test suite after every file edit, with no exceptions, even if the model 'forgets.' What should you use?

- — **A.** Add an instruction to CLAUDE.md asking Claude to run tests after edits
- ✅ **B.** A PostToolUse hook matching Edit/Write that runs the test command
- — **C.** A slash command the user has to remember to invoke
- — **D.** A more detailed system prompt

*Explanation:* Hooks are deterministic shell commands that always fire at a lifecycle event — unlike CLAUDE.md instructions, which the model may or may not follow. A PostToolUse hook guarantees the test command runs after every matching edit. _(Source: D2 Lesson 2 — Hooks and lifecycle events)_

**Q2. (medium, single-answer)** A project's CLAUDE.md and a developer's personal ~/.claude/CLAUDE.md both define a coding convention, and they conflict. Which wins?

- — **A.** User memory always overrides project memory
- ✅ **B.** The most specific (narrowest-scope) memory file wins
- — **C.** They are merged alphabetically
- — **D.** Enterprise policy always wins regardless of scope

*Explanation:* Claude Code's memory hierarchy combines all applicable layers, with the most specific scope taking precedence on conflicts. Project memory is more specific than user (cross-project) memory, so it wins for that project. _(Source: D2 Lesson 1 — CLAUDE.md and the memory hierarchy)_

**Q3. (medium, multi-answer)** Which of the following correctly describe Claude Code's extension mechanisms? (Select all that apply)

- ✅ **A.** A subagent runs with its own separate context window
- ✅ **B.** A slash command is a reusable prompt template invoked with /name
- ✅ **C.** An Agent Skill is defined by a SKILL.md file and loads on demand
- — **D.** Hooks and CLAUDE.md instructions provide the exact same guarantee of execution

*Explanation:* Subagents isolate context, slash commands are invocable prompt templates, and Skills package instructions/resources behind a SKILL.md loaded on demand. Hooks and CLAUDE.md are NOT equivalent — hooks are deterministic; CLAUDE.md is advisory. _(Source: D2 Lesson 3 — Subagents, slash commands, and Skills)_

**Q4. (medium, single-answer)** You want to run Claude Code as part of a CI pipeline and parse its result programmatically. What is the correct approach?

- — **A.** Run claude interactively and copy the transcript
- ✅ **B.** Use headless (print) mode with claude -p and --output-format json
- — **C.** Use a slash command inside an interactive session
- — **D.** There is no way to run Claude Code non-interactively

*Explanation:* Headless/print mode (claude -p "...") runs Claude Code non-interactively for scripts and CI, and --output-format json (or stream-json) gives machine-readable output suitable for pipeline parsing. _(Source: D2 Lesson 4 — Headless mode, permissions, and CI)_

**Q5. (hard, single-answer)** A PreToolUse hook exits with a non-zero 'blocking' exit code. What happens?

- — **A.** The tool call proceeds anyway; hooks can't block tools
- ✅ **B.** The tool call is stopped and the reason is fed back to Claude
- — **C.** Claude Code crashes and the session ends
- — **D.** The hook is retried automatically up to 3 times

*Explanation:* A PreToolUse hook's exit code controls flow: a blocking non-zero exit stops the tool call and surfaces the reason back to Claude, which is how hooks enforce policy (e.g. blocking edits to protected paths). _(Source: D2 Lesson 2 — Hooks and lifecycle events)_


### D3 — Prompt Engineering & Structured Output

**Q1. (easy, single-answer)** A prompt produces inconsistent formatting across runs. Which single change is generally the most reliable first fix?

- ✅ **A.** Add 3–5 well-chosen input→output examples (multishot prompting)
- — **B.** Increase max_tokens
- — **C.** Switch to a larger model
- — **D.** Add more adjectives describing the desired tone

*Explanation:* Multishot (few-shot) prompting — showing several concrete examples of the desired input/output — is one of the most reliable ways to shape consistent format and edge-case handling, often more effective than additional prose instructions. _(Source: D3 Lesson 1 — Clear-and-direct prompting, roles, and examples)_

**Q2. (medium, single-answer)** What is the key difference between chain-of-thought prompting and extended thinking?

- — **A.** They are two names for the exact same thing
- ✅ **B.** Chain-of-thought is a prompting technique requesting visible reasoning; extended thinking is a model feature with a configurable, separately billed thinking token budget
- — **C.** Extended thinking only works with prefilling
- — **D.** Chain-of-thought is only available in Claude Code, not the API

*Explanation:* Chain-of-thought is something you prompt for (e.g. 'think step by step'). Extended thinking is a distinct model capability that produces internal reasoning tokens up to a configured budget, billed as output tokens — the two should not be conflated. _(Source: D3 Lesson 2 — Chain-of-thought, XML tags, and prefilling)_

**Q3. (medium, single-answer)** You must guarantee that Claude's response is always valid JSON matching an exact schema, with zero chance of a downstream parser failing. What should you use?

- — **A.** Ask nicely for JSON in the system prompt
- — **B.** Prefill the response with '{'
- ✅ **C.** Structured outputs / strict tool use with constrained decoding against your schema
- — **D.** Add an XML tag around the expected output

*Explanation:* Structured outputs and strict tool use use constrained decoding to guarantee the response conforms to a supplied JSON Schema. Prompting or prefilling improve the odds but do not guarantee valid, schema-conformant output the way constrained decoding does. _(Source: D3 Lesson 3 — Guaranteeing machine-readable output)_

**Q4. (easy, single-answer)** What is the primary purpose of wrapping reference material in XML tags like <document>...</document> in a prompt?

- — **A.** It reduces token usage
- ✅ **B.** It helps Claude distinguish instructions from data/examples, and makes output easier to parse
- — **C.** It is required syntax for the Messages API
- — **D.** It enables extended thinking

*Explanation:* XML tags delimit different parts of a prompt so the model can tell instructions apart from reference data or examples, reducing confusion and making structured responses easier to parse downstream. _(Source: D3 Lesson 2 — Chain-of-thought, XML tags, and prefilling)_


### D4 — Tool Design & MCP Integration

**Q1. (easy, single-answer)** Claude returns a response with stop_reason: "tool_use" for a tool you defined yourself (not web_search or code_execution). What must your application do next?

- — **A.** Nothing — Anthropic executes the tool automatically
- ✅ **B.** Execute the tool yourself and send back a tool_result block referencing the tool_use_id
- — **C.** Restart the conversation from scratch
- — **D.** Wait for the user to manually approve before any further action is possible

*Explanation:* User-defined tools are client tools: your application must execute the requested function and return the output in a tool_result block referencing the original tool_use_id so Claude can continue. _(Source: D4 Lesson 1 — How tool use works)_

**Q2. (medium, single-answer)** According to Anthropic's tool-design guidance, what is the single most important factor in getting Claude to use a custom tool correctly?

- — **A.** Keeping the tool name as short as possible
- ✅ **B.** Writing an extremely detailed description explaining what the tool does, when to use it, and its caveats
- — **C.** Always setting tool_choice to a specific tool
- — **D.** Minimizing the number of parameters to one

*Explanation:* Anthropic's guidance calls detailed descriptions 'by far the most important factor in tool performance,' recommending at least 3–4 sentences covering what the tool does, when to use it, what it returns, and any caveats. _(Source: D4 Lesson 2 — Designing effective tools)_

**Q3. (medium, multi-answer)** Which statements about the Model Context Protocol (MCP) are correct? (Select all that apply)

- ✅ **A.** An MCP host runs one or more MCP clients, each with a 1:1 connection to an MCP server
- ✅ **B.** MCP servers can expose tools, resources, and prompts
- — **C.** Local stdio MCP servers can be connected to directly by the Messages API MCP connector
- ✅ **D.** Remote MCP servers typically use Streamable HTTP (with SSE) as their transport

*Explanation:* MCP's architecture is host → client(s) → server, one client per server. Servers expose tools, resources, and prompts. The MCP connector requires servers to be publicly reachable over HTTP (Streamable HTTP/SSE) — local stdio servers cannot be connected to directly by the API connector. _(Source: D4 Lesson 3 — The Model Context Protocol (MCP))_

**Q4. (hard, single-answer)** You need Claude to call at most one tool per turn, never in parallel, because your executor isn't safe for concurrent calls. What should you set?

- — **A.** tool_choice: {type: "none"}
- ✅ **B.** tool_choice: {type: "auto", disable_parallel_tool_use: true}
- — **C.** Remove all but one tool from the tools array
- — **D.** input_schema: {"type": "object", "maxProperties": 1}

*Explanation:* disable_parallel_tool_use forces Claude to call at most one tool at a time, which is the correct lever when your tool executor cannot safely handle concurrent tool calls — while still letting Claude choose whether and which tool to call via tool_choice: auto. _(Source: D4 Lesson 2 — Designing effective tools)_


### D5 — Context Management

**Q1. (medium, single-answer)** Where should you place the cache_control breakpoint in a request that includes a large, unchanging system prompt and tool definitions, followed by a conversation history that grows every turn?

- — **A.** On the very last message block, so the whole growing history is cached
- ✅ **B.** After the stable system prompt/tool definitions and before the frequently-changing tail
- — **C.** It does not matter where the breakpoint is placed
- — **D.** On the system prompt only, never on messages

*Explanation:* Caching only helps when the cached prefix is stable across requests. Placing the breakpoint after the large stable content (system + tools) and before content that changes every turn ensures cache hits; caching the changing tail wastes writes and never gets reused. _(Source: D5 Lesson 2 — Prompt caching)_

**Q2. (easy, single-answer)** What is the default TTL (time to live) for a prompt cache entry, and does it refresh on use?

- — **A.** 1 hour, no refresh on use
- ✅ **B.** 5 minutes, refreshed for free each time the cached content is used
- — **C.** 24 hours, refreshed for free each time the cached content is used
- — **D.** There is no default; you must always specify a TTL

*Explanation:* The default cache lifetime is 5 minutes, and it is refreshed at no additional cost each time the cached content is used. A 1-hour TTL is available at additional cost for longer-lived caching needs. _(Source: D5 Lesson 2 — Prompt caching)_

**Q3. (medium, single-answer)** An agentic workflow makes heavy use of tools and accumulates many large tool results (like file contents) that are no longer needed once Claude has processed them. Which context-management feature directly targets this?

- — **A.** Prompt caching
- ✅ **B.** The clear_tool_uses_20250919 context editing strategy
- — **C.** Batch processing
- — **D.** Extended thinking

*Explanation:* clear_tool_uses_20250919 clears the oldest tool results (and optionally the tool inputs) once a threshold is passed, replacing them with placeholders — purpose-built for tool-heavy agents accumulating stale results like file contents. _(Source: D5 Lesson 3 — Compaction, context editing, and memory)_

**Q4. (medium, single-answer)** A long-running agent session is approaching the context window limit and you want the API to automatically summarize older turns and continue, without you writing client-side summarization code. What should you enable?

- — **A.** Prompt caching
- ✅ **B.** Server-side compaction
- — **C.** The memory tool
- — **D.** Batch processing

*Explanation:* Server-side compaction automatically summarizes older context into a compaction block when a configured token threshold is reached, and the API drops content before that block on subsequent requests — the recommended default for long-running conversations. _(Source: D5 Lesson 3 — Compaction, context editing, and memory)_

**Q5. (easy, single-answer)** A company needs to classify 80,000 support tickets overnight and latency is not a concern. Which approach minimizes cost?

- ✅ **A.** The Message Batches API
- — **B.** Streaming responses
- — **C.** A 1-hour prompt cache TTL
- — **D.** Extended thinking with a large budget

*Explanation:* The Message Batches API processes large volumes asynchronously at 50% lower cost, fits well within its 100,000-request/256MB batch limit, and is ideal when results aren't needed immediately — exactly this scenario. _(Source: D5 Lesson 4 — Cost and latency levers: batch and streaming)_

**Q6. (hard, single-answer)** Does streaming a response reduce the total token cost of a request compared to a non-streaming call with identical content?

- — **A.** Yes, streaming always cuts token costs by roughly half
- ✅ **B.** No — streaming improves perceived latency by returning tokens as generated, but does not change total tokens or cost
- — **C.** Yes, but only for batch requests
- — **D.** No, streaming actually increases cost

*Explanation:* Streaming is a latency/UX improvement — it delivers tokens as they're generated so users see output sooner — but it does not itself change how many tokens are processed or billed. _(Source: D5 Lesson 4 — Cost and latency levers: batch and streaming)_


## Glossary

### API

- **Cache breakpoint** — A cache_control marker on a content block indicating the boundary of what should be cached. Up to 4 explicit breakpoints are supported per request.
- **Compaction** — Server-side context management that automatically summarizes older conversation turns into a compaction block once a token threshold is reached, letting long-running conversations continue without manual client-side summarization.
- **Context editing** — A server-side feature that selectively clears content (old tool results via clear_tool_uses, or thinking blocks via clear_thinking) from conversation history as it grows, while the client keeps its full unmodified history.
- **Context window** — The total number of tokens a model can consider at once, including the system prompt, all messages, tool definitions, and generated output (including extended thinking). Current large Claude models support up to 1M tokens.
- **Extended thinking** — A model capability that produces internal reasoning tokens before the final answer, with a configurable token budget billed as output — distinct from prompted chain-of-thought.
- **Memory tool** — A tool that lets Claude persist information outside the context window by writing to and reading from an external store, so knowledge survives across turns without consuming context space.
- **Message Batches API** — An asynchronous API for processing large volumes of requests (up to 100,000 requests or 256MB per batch) at 50% lower cost than synchronous calls, with most batches completing within an hour and a 24-hour expiry.
- **Multishot (few-shot) prompting** — Including several concrete input→output examples in a prompt to shape format and behavior — often more reliable than lengthy prose instructions.
- **Prefilling** — Supplying the opening tokens of Claude's response (e.g. a leading '{') to steer output format. A weaker guarantee of structure than constrained decoding.
- **Prompt caching** — A feature that stores a prompt prefix marked with cache_control so repeated requests sharing that prefix skip reprocessing, cutting cost and latency. Default TTL is 5 minutes (refreshed free on use); a 1-hour TTL costs more to write.
- **Streaming** — Returning a response as server-sent events (tokens as they're generated) to improve perceived latency for interactive use, without changing total token cost.
- **Strict tool use** — A tool-use mode (strict: true) that guarantees schema validation on tool names and inputs via constrained decoding.
- **Structured outputs** — An API feature that uses constrained decoding to guarantee Claude's response conforms exactly to a supplied JSON Schema, eliminating malformed-output parsing failures.
- **Tool use** — The mechanism by which Claude calls functions you define (client tools) or that Anthropic executes (server tools), returning a tool_use block that your application handles by supplying a tool_result.
- **tool_choice** — A Messages API parameter controlling whether/which tool Claude must call: auto (model decides), any (must call some tool), tool (force a specific tool), or none.

### Agents

- **Agent** — A system where the LLM dynamically directs its own process and tool usage, deciding at runtime what to do next and when the task is complete — as opposed to a workflow, where the steps are predefined in code.
- **Augmented LLM** — A model enhanced with retrieval, tools, and memory. The basic building block underlying both workflows and agents.
- **Coordinator** — In multiagent orchestration, the agent that delegates work to a roster of subagents and synthesizes their results. Also called the orchestrator.
- **Evaluator-optimizer** — A workflow pattern where one LLM generates a response while a second LLM evaluates it and provides feedback in an iterative loop until quality criteria are met.
- **Human-in-the-loop** — A design pattern that requires human approval before an agent takes a consequential or irreversible action, such as sending an email or spending money.
- **Orchestrator-workers** — A workflow pattern where a central LLM dynamically breaks a task into subtasks not known in advance, delegates them to worker LLMs, and synthesizes the results.
- **Parallelization** — A workflow pattern that runs independent subtasks simultaneously. Two variants: sectioning (splitting a task into independent parallel parts) and voting (running the same task multiple times and aggregating).
- **Prompt chaining** — A workflow pattern that decomposes a task into a fixed sequence of LLM calls, each processing the output of the previous one, often with programmatic checks between steps.
- **Routing** — A workflow pattern that classifies an input and directs it to one of several specialized, predefined follow-up prompts or models.
- **Session thread** — A context-isolated event stream and conversation history for one agent within a Managed Agents session. The primary thread belongs to the coordinator; additional threads are spawned when it delegates.
- **Subagent** — In Claude Code, a specialized assistant with its own context window, tools, and system prompt that the main agent can delegate to. In Managed Agents, a worker agent a coordinator delegates to within its own isolated session thread.
- **Workflow** — A system where LLMs and tools are orchestrated through predefined, fixed code paths. More predictable and easier to debug than an autonomous agent, but less flexible for open-ended tasks.

### Claude Code

- **Agent Skill** — A modular, filesystem-based capability packaged with a SKILL.md file plus optional scripts and resources, loaded on demand ('progressive disclosure') rather than kept in context at all times.
- **CLAUDE.md** — A special file Claude Code automatically loads into context at the start of a session, used for durable project knowledge such as build commands, architecture notes, and conventions. Instructions in it are advisory, not guaranteed.
- **Headless (print) mode** — Non-interactive Claude Code execution via `claude -p "<prompt>"`, typically with --output-format json, used for scripting and CI pipelines.
- **Hook** — A user-defined shell command that runs deterministically at a fixed point in Claude Code's lifecycle (e.g. PreToolUse, PostToolUse), unlike CLAUDE.md instructions which the model may or may not follow.
- **Memory hierarchy** — The layered precedence of Claude Code memory files — enterprise policy, project (CLAUDE.md), and user (~/.claude/CLAUDE.md) — where more specific scopes take precedence on conflicts.
- **Permissions** — Claude Code settings that govern which tools and commands may run without asking the user for confirmation, configurable via allow/deny lists — especially important to scope narrowly in CI.
- **PostToolUse** — A hook event that fires after a tool call completes successfully — commonly used to auto-format or lint edited files.
- **PreToolUse** — A hook event that fires before a tool call executes. A blocking non-zero exit code stops the tool call and returns the reason to Claude.
- **Slash command** — A reusable prompt template stored as markdown and invoked by typing /name, optionally with arguments injected via $ARGUMENTS.

### MCP

- **MCP (Model Context Protocol)** — An open standard that connects AI applications to external tools, data, and prompts through a common interface, so the same server can work across many host applications.
- **MCP client** — A component maintained by the host that holds a 1:1 connection to a single MCP server.
- **MCP connector** — A Messages API feature that connects directly to remote MCP servers (via an mcp_servers array) without requiring you to run a separate MCP client.
- **MCP host** — The AI application (e.g. Claude Code, or an app using the Claude API) that runs one or more MCP clients to connect to MCP servers.
- **MCP prompt** — A reusable prompt template an MCP server exposes for the host or user to invoke.
- **MCP resource** — Data or context an MCP server exposes that the host application can read, distinct from a callable tool.
- **MCP server** — A program that exposes tools, resources, and/or prompts to an MCP client over a standardized protocol, via stdio (local) or Streamable HTTP/SSE (remote).
- **MCP tool** — A function an MCP server exposes that the model can call, analogous to a tool defined directly in the Messages API.
- **Streamable HTTP** — The MCP transport (with SSE) used for remote servers that must be publicly reachable over HTTP.
- **stdio transport** — The MCP transport used for local servers, where the host launches the server as a subprocess and communicates over standard input/output.
