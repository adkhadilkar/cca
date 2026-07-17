# Content — CCAR-F Knowledge Base

This folder is the **authored knowledge base** for the app, structured as a
content pack per `docs/DEVELOPMENT_DESIGN.md` §6.

```
content/
├── SOURCES.md              Provenance log: every source used, with gaps flagged
├── packs/ccar-f/           The v2 content pack for the Foundations track
│   ├── manifest.json       Pack metadata, counts, source list, changelog
│   ├── domains.json        Track info + 5 domains with weights
│   ├── guide.json          Exam guide screen content
│   ├── lessons.json        19 lessons across the 5 domains
│   ├── questions.json      26 practice questions with explanations
│   └── glossary.json       49 glossary terms across 4 categories
└── README.md                This file
```

A human-readable rollup of the same content lives at
`docs/KNOWLEDGE_BASE.md` for easy review without parsing JSON.

## Status: v2 — D1 and D2 verified

v1 shipped with D2 (and D1 Lesson 2) authored from trained knowledge because
`code.claude.com` and `www.anthropic.com` were unreachable from the authoring
session's network. v2 closes that gap: the user ran a crawler from their own
machine against `code.claude.com/docs` (168 pages) and the engineering blog,
and uploaded the result. D1 Lesson 2 was confirmed accurate as originally
written; D2 needed real corrections (memory-scope concatenation vs.
"most-specific-wins", missing auto memory, wrong hook exit-code semantics,
commands-merged-into-Skills, and a new Routines topic) — see `SOURCES.md`
and `manifest.json`'s changelog for the full list.

**Remaining known gap:** the exam format itself (60 questions / 120 min /
720 pass score / domain weights) is still sourced from web-search summaries
of Pearson VUE's page, not a direct fetch. See `SOURCES.md` "Remaining known
gap" before treating `guide.json` as authoritative.

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
