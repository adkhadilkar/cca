# Content — CCAR-F Knowledge Base

This folder is the **authored knowledge base** for the app, structured as a
content pack per `docs/DEVELOPMENT_DESIGN.md` §6.

```
content/
├── SOURCES.md              Provenance log: every source used, with gaps flagged
├── packs/ccar-f/           The v3 content pack for the Foundations track
│   ├── manifest.json       Pack metadata, counts, source list, changelog
│   ├── domains.json        Track info + 5 domains, official order/weights
│   ├── guide.json          Exam guide screen content + scenario bank + policy
│   ├── lessons.json        30 lessons, one per official task statement
│   ├── questions.json      34 practice questions — 12 official + 22 original, all single-answer
│   └── glossary.json       46 glossary terms across 4 categories
└── README.md                This file
```

A human-readable rollup of the same content lives at
`docs/KNOWLEDGE_BASE.md` for easy review without parsing JSON.

## Status: v3 — rebuilt against the official Exam Guide

v3 supersedes v1/v2 entirely. The user supplied three official Anthropic
PDFs — the Exam Guide, the Certification Exam Policy, and the Certification
Terms and Conditions — which turned out to correct real errors in the
earlier, web-search-derived exam facts: the domain order/names were wrong
(v1/v2 had D2="Claude Code", official D2="Tool Design & MCP Integration"),
and the response format was wrong (v1/v2 included multi-select questions;
the real exam is single-answer, 4-choice, only). Every lesson was rewritten
one-per-official-task-statement, and 12 official sample questions were
added with clear attribution alongside 22 originally-authored ones. See
`SOURCES.md` and `manifest.json`'s changelog for the full diff.

**Remaining known gap:** the Exam Guide is versioned ("Version 0.2") and
dated June 30 2026 — Anthropic may revise it. Check for a newer version
before treating this pack as permanently current.

## Content model

Matches the Room schema in `docs/DEVELOPMENT_DESIGN.md` §5.1:

- **Domain** → **Lesson** (ordered body blocks: `text`, `code`, `callout`)
- **Domain** → **Question** (single-answer, 4-choice, with `explanation` and `sourceRef`)
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
