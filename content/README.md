# Content — CCAR-F Knowledge Base

This folder is the **authored knowledge base** for the app, structured as a
content pack per `docs/DEVELOPMENT_DESIGN.md` §6.

```
content/
├── SOURCES.md              Provenance log: every source used, with gaps flagged
├── packs/ccar-f/           The v1 content pack for the Foundations track
│   ├── manifest.json       Pack metadata, counts, source list
│   ├── domains.json        Track info + 5 domains with weights
│   ├── guide.json          Exam guide screen content
│   ├── lessons.json        18 lessons across the 5 domains
│   ├── questions.json      25 practice questions with explanations
│   └── glossary.json       46 glossary terms across 4 categories
└── README.md                This file
```

A human-readable rollup of the same content lives at
`docs/KNOWLEDGE_BASE.md` for easy review without parsing JSON.

## Status: v1 draft — needs re-verification

This pack was authored from official Anthropic docs that were reachable from
the authoring session's network (`platform.claude.com`). Two categories of
source were **not** reachable and so are based on trained knowledge rather
than a fresh fetch — **read `SOURCES.md` "Known gaps" before treating this as
exam-ready**:

1. `code.claude.com` (Claude Code's own docs) — affects all of **D2**.
2. The "Building effective agents" engineering blog post — affects **D1
   Lesson 2** (the five workflow patterns).

Everything else (D3, D4, D5, and D1's multiagent/reliability lessons) is
grounded in documentation fetched directly in this session.

## Content model

Matches the Room schema in `docs/DEVELOPMENT_DESIGN.md` §5.1:

- **Domain** → **Lesson** (ordered body blocks: `text`, `code`, `callout`)
- **Domain** → **Question** (single/multi choice, with `explanation` and `sourceRef`)
- **GlossaryTerm** (categorized, searchable)

IDs are stable strings (`d1-l1`, `q-d1-001`, `g-agent`, …) so future content
updates can revise text without breaking a user's progress records, per the
"golden rule" in the dev design doc §5.2.

## Regenerating / extending

There is no `content-cli` build tool yet (planned in the dev design doc's
milestone M5) — these files are hand-authored JSON, validated with:

```bash
python3 -c "import json; [json.load(open(f'content/packs/ccar-f/{f}')) for f in \
  ['domains.json','guide.json','lessons.json','questions.json','glossary.json']]"
```

To extend: add entries with new stable IDs, keep `sourceRef` pointing at a
real source, and update `manifest.json` counts + `SOURCES.md` if you add a
new source.
