# Claude Architect Exam Prep — Mobile UI Design

Professional Android application for offline exam preparation. Two exam tracks (Foundations & Professional), fully offline, light + dark themes. Ready for Claude Code implementation.

## Overview

**Target:** Android · **Status:** Design complete · **Handoff:** Design Components + component specs + token system

### Exams

- **CCAR-F (Foundations):** 60 questions, 120 min, 720 passing score
- **CCAR-P (Professional):** 63 questions, 120 min, advanced scenarios
- **Weighting** (Foundations):
  - D1 Agentic Architecture — 27%
  - D2 Claude Code Workflows — 20%
  - D3 Prompt Engineering — 20%
  - D4 Tool Design & MCP — 18%
  - D5 Context Management — 15%

## Design System

### Color Palette (light)

| Token | Value | Usage |
|-------|-------|-------|
| **Background** | `#FAF7F0` | Page bg |
| **Surface** | `#FFFFFF` | Cards, containers |
| **Border** | `#E5DFCF` | Card/input borders |
| **Text (primary)** | `#29261F` | Headlines, body |
| **Text (secondary)** | `#6B6555` | Metadata, labels |
| **Text (tertiary)** | `#8A8371` | Captions, timestamps |
| **Accent** | `#C15F3C` | CTAs, active states, highlights |
| **Accent (muted)** | `#B0483A` | Warnings, weak areas |
| **Accent (light)** | `#E5B394` | Backgrounds, progress bars |
| **Success** | `#5A8A5E` | Pass, positive |
| **Neutral (light)** | `#F0EBDD` | Section BG, inactive states |

### Color Palette (dark)

| Token | Value | Usage |
|-------|-------|-------|
| **Background** | `#211E18` | Page bg |
| **Surface** | `#2A2620` | Cards, containers |
| **Border** | `#3A352B` | Card/input borders |
| **Text (primary)** | `#F0EBDD` | Headlines, body |
| **Text (secondary)** | `#A8A190` | Metadata, labels |
| **Text (tertiary)** | `#8A8371` | Captions, timestamps |
| **Accent** | `#E8956D` | CTAs, active states, highlights |
| **Accent (muted)** | `#B06246` | Warnings, weak areas |
| **Neutral (light)** | `#2E2A22` | Section BG, inactive states |
| **Success** | `#7FB08A` | Pass, positive |

### Typography

- **Serif (headlines, titles):** Source Serif 4 (400, 500, 600, 700)
  - Headlines: 600–700 weight, 26–34px
  - Subheads: 600 weight, 17–22px
  
- **Sans (body, UI):** Public Sans (400, 500, 600, 700)
  - Body: 400 weight, 13.5–14.5px
  - Labels: 600 weight, 12–15px
  - Metadata: 400 weight, 11–12px
  
- **Monospace (codes, timestamps, domains):** IBM Plex Mono (400, 500)
  - Timestamps: 14px weight-500
  - Domain codes (D1, D2): 11px weight-400
  - Exam metadata: 10px weight-400, uppercase, letter-spacing 0.08em

### Spacing & Radius

- **Padding (containers):** 16–20px
- **Gap (flex layouts):** 8–14px
- **Border radius:**
  - Buttons, chips: 14–16px
  - Cards, sections: 16–20px
  - Inline badges: 999px (pill)

### Shadows

- **Cards:** `0 1px 2px rgba(41,38,31,0.08)` (light), `0 1px 2px rgba(0,0,0,0.2)` (dark)
- **Modals/elevated:** `0 8px 24px rgba(41,38,31,0.06)` (light)

## Screens (13 total)

| # | Screen | Purpose | Notes |
|---|--------|---------|-------|
| 01 | Onboarding | Exam & date selection | Track selection, date + daily goal |
| 02 | Study — domains | Lesson picker | 5 domains, progress bars per domain |
| 03 | Lesson detail | Lesson view | Scrolling text, code examples, exam key point callouts |
| 04 | Practice (multi) | Question + answers | Question bank, option colors (correct/selected/flagged) |
| 05 | Flashcards | Spaced repetition | Card flip, difficulty buttons (Again/Hard/Good/Easy) |
| 06 | Mock exam (timed) | Full timed exam | 60 Q, countdown timer, flagging, progress bar |
| 07 | Results | Exam score breakdown | Pass/fail badge, per-domain bars, score history, review link |
| 08 | Progress & history | Dashboard | Score chart (4 mocks), streak calendar, total stats |
| 09 | Exam guide | What to expect | Format, question count, time, domain weights, proctoring info |
| 10 | Glossary | Term reference | 84 terms, search, category tabs (Agents, MCP, Claude Code, API) |
| 11 | Settings | Preferences | Theme toggle, exam date, daily goal, reminder, daily card limit, data export/reset |
| 12 | Lesson dark | Dark theme (lesson) | Same as 03, dark tokens applied |
| 13 | Mock exam dark | Dark theme (exam) | Same as 06, dark tokens applied |

## Files

```
design/
├── Home Variations.dc.html      # Home screen: 3 directions (pick 1 for final)
├── App Screens.dc.html          # 13 screens (light + dark samples)
├── android-frame.jsx            # Android device bezel (starter component)
├── support.js                   # Design Component runtime (auto-generated)
└── README.md                    # This file
```

## Component Architecture

**All files are Design Components (`.dc.html`)**, ready to open directly in a browser or import into other DCs via `<dc-import>`.

### Template Structure

Each DC uses:
- **Helmet (styles + fonts):** Google Fonts (Source Serif 4, Public Sans, IBM Plex Mono), color tokens, body resets
- **Inline styles:** All colors, spacing, shadows — no external CSS
- **Android frame wrapper:** `<x-import component-from-global-scope="AndroidDevice" from="./android-frame.jsx">` for device bezel + status bar + nav
- **Data screens:** Realistic placeholder content (exam domains, lesson snippets, sample questions with explanations)

### Key Elements

- **Progress bars:** Conic gradients for readiness ring, linear for per-domain bars
- **Cards:** White bg, 1px border, 16–20px border-radius
- **Buttons:** Solid (primary accent) or outlined (secondary)
- **Tabs:** Toggle background (active) vs. light background (inactive)
- **Modals/overlays:** Rounded corners, shadow, centered content

## Design Rationale

### Color & Tone

- **Warm palette** (terracotta accent, cream/warm-gray backgrounds) conveys approachability and focus
- **Serif for headlines** builds confidence in exam content (authoritative without cold)
- **Monospace for metadata** (domain codes, timestamps, scores) grounds technical information
- **Two themes** (light + dark) support extended study sessions and user preference

### Layout & Density

- **Card-based layout:** Clear content separation, scannable
- **Bottom nav (5 items):** Home, Study, Practice, Exam, Progress — mirrors study flow
- **Streak, countdown, readiness:** Gamification elements without distraction
- **Accessibility:** All text >11pt, touch targets ≥44px, sufficient contrast (WCAG AA+)

### Features

1. **Domain-by-domain lessons** — users master one area, see progress immediately
2. **Practice questions w/ explanations** — answers aren't just right/wrong; they educate
3. **Timed mock exams** — builds stamina, confidence
4. **Spaced repetition flashcards** — long-term retention
5. **Progress dashboard** — score trends, streak, weak-area highlights
6. **Exam guide & glossary** — reference always available
7. **100% offline** — no network required; all data persists locally (localStorage / SQLite)

## Handoff to Claude Code

### What's Ready

- ✅ Complete visual design (13 screens, light + dark)
- ✅ Component structure (modular .dc.html files)
- ✅ Color + typography token system (documented above)
- ✅ Real placeholder content (exam domains, sample questions, lesson text, glossary terms)
- ✅ Device framing (Android 393px × 780px viewport)

### What Claude Code Needs to Build

1. **State management** — track current track (CCAR-F / CCAR-P), progress per domain, mock scores, flashcard due dates
2. **Offline storage** — localStorage or SQLite for lessons, questions, results, streak
3. **Navigation** — bottom tab nav driving screens
4. **Interactivity:**
   - Lesson scrolling + "Mark done"
   - Practice question selection, flagging, multi-answer support
   - Flashcard flip, difficulty routing (spaced repetition scheduler)
   - Mock exam timer, question navigation, submit confirmation
   - Theme toggle (light/dark)
   - Search in glossary
5. **Data sync** — import exam content at first launch (lessons, questions, glossary), persist locally
6. **Analytics** (optional) — session time, accuracy by domain, score trajectory

### Content to Import

The design includes sample content for 5 domains (Foundations exam):
- **Lesson snippet** (D2, Lesson 7: Hooks & lifecycle events)
- **Practice question + explanation** (D4: MCP architecture)
- **Flashcard** (D5: Context compaction)
- **Glossary** (sample terms: CLAUDE.md, Compaction, Context window, Hook, MCP)

Full exam content (42 lessons, 60+ questions per domain) should be imported from official Anthropic exam resources or structured as a JSON/SQLite dataset, then integrated into the app.

---

**Design created:** July 2026 · **Version:** 1.0 · **Status:** Ready for handoff
