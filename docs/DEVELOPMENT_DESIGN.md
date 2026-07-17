# Claude Architect Exam Prep — Android Development Design Document

**Status:** Draft v1.0 · **Date:** July 2026 · **Owner:** adkhadilkar
**Companion to:** `design/README.md` (visual design, tokens, 13 screens) on branch `base_design`

---

## 0. How to read this document

The visual/UX design is already done and lives in the `design/` folder (design
system, color tokens, typography, and 13 fully-specified screens for two exam
tracks, light + dark). **This document is the engineering counterpart:** it
specifies the app architecture, the offline data model, the content pipeline
(how we source and refresh knowledge from official Anthropic material), the
algorithms (spaced repetition, scoring, readiness), and the build/release plan.

Sections most relevant to your explicit asks:

- **Offline + "lots of data"** → §3 (Architecture), §5 (Data model), §6 (Content packs).
- **Getting updated knowledge from official docs / videos / training** → §7 (Content Pipeline) and §8 (Legal & sourcing — read this first).

---

## 1. Product summary

A **100% offline** Android app that prepares candidates for Anthropic's
**Claude Certified Architect (CCA)** exams.

| Track | Code | Questions | Time | Pass | Notes |
|-------|------|-----------|------|------|-------|
| Foundations | CCAR-F | 60 | 120 min | 720 / 1000 | Proctored, closed-book |
| Professional | CCAR-P | 63 | 120 min | (advanced) | Advanced scenarios |

**Foundations domain weighting** (drives question counts and progress rings):

| Domain | Weight | App code |
|--------|--------|----------|
| Agentic Architecture & Orchestration | 27% | D1 |
| Claude Code Configuration & Workflows | 20% | D2 |
| Prompt Engineering & Structured Output | 20% | D3 |
| Tool Design & MCP Integration | 18% | D4 |
| Context Management | 15% | D5 |

> **Accuracy note:** Anthropic's official exam blueprint is the source of truth
> and *will* change. The percentages above match the current design and public
> descriptions of the exam (5 domains). Domain weights, question counts, and the
> pass score must be treated as **content-pack data (§6), not hardcoded
> constants**, so they can be corrected via a content update without shipping a
> new APK.

### Core features (from the design)

1. Domain-by-domain **lessons** (~42 lessons, code examples, "exam key point" callouts)
2. **Practice questions** with explanations (single- and multi-answer)
3. **Flashcards** with spaced repetition (Again / Hard / Good / Easy)
4. Timed **mock exams** (full length, countdown, flagging, submit confirmation)
5. **Results** with per-domain breakdown + score history
6. **Progress dashboard** (score trend, streak calendar, weak-area highlights)
7. **Exam guide** + searchable **glossary** (~84 terms)
8. **Settings** (theme, exam date, daily goal, reminders, export/reset)

---

## 2. Goals, non-goals, constraints

**Goals**
- Fully usable with the device in airplane mode after first install.
- Content (lessons, questions, glossary) is **updatable without a Play Store
  release** via signed content packs.
- Fast: cold start < 1.5s, screen transitions < 100ms, question navigation instant.
- Accessible: WCAG AA+, touch targets ≥ 44dp, dynamic type, TalkBack labels.

**Non-goals (v1)**
- No account system, no cloud sync, no leaderboard, no telephony/ads.
- No live proctoring or real exam delivery (we are *prep*, not the exam).
- No AI-in-app answer generation (keeps it offline and avoids stale/incorrect
  generated answers; all content is human-reviewed — see §8).

**Constraints**
- Offline-first: network is used **only** for optional content-pack downloads.
- Content licensing: we do not redistribute Anthropic's copyrighted text/video.
  We author our own study material derived from public docs (see §8).

---

## 3. Architecture

### 3.1 Stack recommendation

| Concern | Choice | Why |
|---------|--------|-----|
| Language | **Kotlin** | Android standard |
| UI | **Jetpack Compose** (Material 3) | The design is card/token based; Compose maps cleanly to the token system and light/dark theming |
| Min / target SDK | min 26 (Android 8) / target 35 | ~99% device coverage; modern APIs |
| Local DB | **Room** over SQLite | Content is relational (domains→lessons→questions); FTS for glossary/lesson search |
| Key-value prefs | **DataStore (Proto)** | Theme, exam date, daily goal, streak counters |
| Async | Kotlin Coroutines + Flow | Reactive progress/timer state |
| DI | Hilt | Testable repositories |
| Content pack fetch | **WorkManager** + OkHttp | Deferred, retryable, battery-aware downloads |
| Serialization | kotlinx.serialization (JSON) | Content-pack format |
| Charts | Compose Canvas (custom) | Score trend + streak calendar are simple; avoids heavy deps |

> The current `design/` files are HTML "Design Components" (`.dc.html`) used as a
> visual spec, not the shipping code. They are the **pixel reference**; the app is
> native Compose. (If a WebView/PWA path is ever preferred, see §12 Alternatives.)

### 3.2 Module layout (single app, layered)

```
app/
 ├─ core-ui/         Compose theme, tokens (colors, type, spacing, shadows)
 ├─ core-data/       Room DB, DAOs, DataStore, content-pack importer
 ├─ core-domain/     Models, spaced-repetition scheduler, scoring, readiness
 ├─ feature-onboarding/   Screen 01
 ├─ feature-study/        Screens 02, 03 (lessons)
 ├─ feature-practice/     Screen 04
 ├─ feature-flashcards/   Screen 05
 ├─ feature-exam/         Screens 06, 07 (mock + results)
 ├─ feature-progress/     Screen 08
 ├─ feature-reference/    Screens 09 (guide), 10 (glossary)
 └─ feature-settings/     Screen 11
```

Navigation: single-Activity, Compose Navigation, 5-tab bottom bar
(**Home · Study · Practice · Exam · Progress**), matching the design.

### 3.3 Design tokens → Compose

Port the token tables from `design/README.md` directly into `core-ui`:

```kotlin
// Light
object LightColors {
    val background = Color(0xFFFAF7F0)
    val surface    = Color(0xFFFFFFFF)
    val border      = Color(0xFFE5DFCF)
    val textPrimary = Color(0xFF29261F)
    val textSecondary = Color(0xFF6B6555)
    val accent      = Color(0xFFC15F3C)
    val success     = Color(0xFF5A8A5E)
    // …dark set from README's dark table
}
```

Fonts: bundle **Source Serif 4** (headlines), **Public Sans** (body/UI),
**IBM Plex Mono** (codes/timestamps) as app assets — do **not** fetch Google
Fonts at runtime (offline requirement). Register as a Compose `FontFamily`.

---

## 4. Screen → implementation map

| # | Screen | Key state | Persisted | Notes |
|---|--------|-----------|-----------|-------|
| 01 | Onboarding | selected track, exam date, daily goal | DataStore | One-time; re-runnable from Settings |
| 02 | Study (domains) | per-domain % complete | Room (progress) | Progress = lessons done / total in domain |
| 03 | Lesson detail | scroll pos, "mark done" | Room | Renders lesson body blocks (text/code/callout) |
| 04 | Practice | current Q, selected, flagged | session (Room) | Single/multi answer; explanation reveal |
| 05 | Flashcards | due queue, flip state | Room (SR schedule) | Again/Hard/Good/Easy → scheduler (§9.1) |
| 06 | Mock exam | timer, answers, flags, index | Room (attempt) | Autosave every answer; survive process death |
| 07 | Results | score, per-domain, pass/fail | Room (attempt) | Review link back into questions |
| 08 | Progress | last N mock scores, streak, totals | Room (derived) | Canvas chart + calendar heatmap |
| 09 | Exam guide | static (from content pack) | content pack | Format, weights, proctoring info |
| 10 | Glossary | search query, category tab | Room FTS | ~84 terms; instant search |
| 11 | Settings | theme, reminders, limits | DataStore | Export (JSON), reset, content-update trigger |

Screens 12–13 are dark-theme variants — handled by the theme system, not
separate screens.

---

## 5. Data model (Room)

### 5.1 Content entities (read-mostly, from content packs)

```
Track(id, code, title, questionCount, timeLimitMin, passScore, contentVersion)
Domain(id, trackId, code /*D1..D5*/, title, weightPct, orderIndex)
Lesson(id, domainId, title, orderIndex, estMinutes, bodyJson /*ordered blocks*/)
LessonBlock  // stored inside bodyJson: {type: text|code|callout|image, ...}
Question(id, domainId, type /*single|multi*/, stem, choicesJson,
         correctJson, explanation, difficulty, tags, sourceRef)
GlossaryTerm(id, term, definition, category /*Agents|MCP|ClaudeCode|API*/, relatedIds)
```

Glossary and lesson bodies get a **Room FTS4/5** mirror table for search
(screens 03, 10).

### 5.2 User/progress entities (read-write, never in content packs)

```
LessonProgress(lessonId, status /*unseen|in_progress|done*/, lastViewedAt)
QuestionAttempt(id, questionId, sessionId, chosenJson, correct, timeMs, ts)
FlashcardState(cardId, ease, intervalDays, dueAt, reps, lapses, lastGrade)  // §9.1
MockAttempt(id, trackId, startedAt, submittedAt, score, perDomainJson,
            answersJson, flaggedJson, status /*in_progress|submitted*/)
StreakDay(date, studiedSeconds, goalMet)
UserPrefs   // DataStore, not Room: theme, examDate, dailyGoal, reminders, cardLimit
```

**Golden rule:** content packs may replace *all* content rows but must **never**
touch user/progress rows. Keep them in the same DB but treat progress tables as
append/update-only during import. IDs in content packs are **stable UUIDs** so
progress survives content updates (a re-authored question keeps its id).

### 5.3 Migrations

- Schema versioned with Room migrations for **structure**.
- Content versioned separately with `contentVersion` per track (see §6).

---

## 6. Content packs (the "lots of data", offline)

All study material ships as **versioned content packs**, decoupled from the app
binary so knowledge can be corrected/expanded without a Play Store review.

### 6.1 Format

A pack is a signed `.zip`:

```
pack-ccar-f-v14.zip
 ├─ manifest.json     # id, track, contentVersion, createdAt, checksum, signature
 ├─ domains.json
 ├─ lessons.json      # ~42 lessons with ordered body blocks
 ├─ questions.json    # question bank, ≥ 60 per domain target
 ├─ glossary.json     # ~84 terms
 ├─ guide.json        # exam format, weights, proctoring notes
 └─ assets/           # diagrams (WebP), optional
```

`manifest.json`:

```json
{
  "packId": "ccar-f",
  "track": "CCAR-F",
  "contentVersion": 14,
  "minAppVersion": 3,
  "createdAt": "2026-07-10T00:00:00Z",
  "sha256": "…",
  "signature": "…(Ed25519 over sha256)…",
  "sources": [
    {"title": "Claude Docs — Hooks", "url": "https://docs.claude.com/…", "retrieved": "2026-07-01"}
  ]
}
```

### 6.2 Shipping & updating

- **v1 install:** the newest pack is **bundled in `assets/`** → app is fully
  usable offline on first launch, zero network.
- **Updates:** Settings → "Check for content updates" (manual) or a **weekly
  WorkManager job** pings a static JSON index (e.g. on GitHub Releases / a CDN),
  compares `contentVersion`, downloads + verifies signature, imports in a
  transaction, then swaps atomically. Fully optional; skipping it never breaks
  the app.
- **Verification:** reject any pack whose `sha256`/`signature` fails or whose
  `minAppVersion` exceeds the installed app.

### 6.3 Volume estimate

~42 lessons + ~300 questions + ~84 glossary terms + diagrams ≈ **2–8 MB per
track** compressed. Trivial to bundle; no streaming needed.

---

## 7. Content pipeline — sourcing & refreshing knowledge

This is the part that keeps the app *correct over time*. It is a **build-time
authoring pipeline run by maintainers**, not something the app does at runtime.
The app only ever consumes finished, signed packs (§6).

```
Official Anthropic sources                Authoring pipeline (repo: content/)
──────────────────────────                ─────────────────────────────────────
 docs.claude.com  ─────────┐
 Anthropic Academy /       ├──►  1. Fetch & snapshot (dated)  ──►  content/sources/
   Skilljar courses        │      2. Extract & normalize      ──►  content/raw/
 Anthropic YouTube /       │      3. Human authoring/review   ──►  content/authored/
   training videos ────────┤      4. Validate (schema+lint)   ──►  CI
 Anthropic Cookbook /      │      5. Build pack (zip+sign)     ──►  dist/pack-*.zip
   engineering blog  ──────┘      6. Publish index + release  ──►  CDN / GH Releases
```

### 7.1 Primary official sources (authoritative)

| Source | URL | Use |
|--------|-----|-----|
| Claude Docs | https://docs.claude.com | Ground truth for API, Claude Code, MCP, prompt engineering |
| Anthropic Academy / Skilljar | via https://www.anthropic.com (13 free courses) | Maps directly to exam domains; lesson scaffolding |
| Certification hub (Pearson VUE) | https://www.pearsonvue.com/us/en/anthropic.html | Exam format, blueprint, pass score, updates |
| Anthropic Cookbook | https://github.com/anthropics/anthropic-cookbook | Worked examples for questions (MIT-licensed code) |
| MCP spec & docs | https://modelcontextprotocol.io | D4 tool/MCP questions |
| Anthropic Engineering blog | https://www.anthropic.com/engineering | Best-practice framing (agents vs workflows, context mgmt) |
| Anthropic YouTube / course videos | official channels | Transcribe → author lessons/flashcards (see §7.3) |

### 7.2 Step-by-step pipeline

**1. Fetch & snapshot.** A maintainer script pulls each source and stores a
**dated snapshot** (HTML/MD + the source URL + retrieval date) under
`content/sources/YYYY-MM-DD/`. Snapshots give us provenance and a diff base to
detect when Anthropic changes something.

**2. Change detection.** A scheduled job (weekly) re-fetches key doc URLs and
diffs against the last snapshot. A meaningful diff opens a "content review"
issue listing what changed (e.g. "Claude Code hooks page updated") so the
relevant lessons/questions get re-checked. This is how we **keep knowledge
current**.

**3. Extract & normalize.** Convert snapshots to clean Markdown; strip
navigation/boilerplate; keep headings, code blocks, tables.

**4. Human authoring & review (required).** Maintainers write **original**
lessons, questions, and glossary entries *informed by* the sources — not
copied. Every question carries a `sourceRef` to the doc section it tests. A
second reviewer signs off (see §8 on why authoring, not scraping, is the model).

**5. Validate.** CI runs JSON-schema validation, a link checker on every
`sourceRef`, a duplicate-question detector, and a domain-coverage report
(e.g. "D1 has 71 questions, D5 has 44 — rebalance").

**6. Build & sign.** `content-cli build --track ccar-f` produces the zip,
computes `sha256`, signs with the project's Ed25519 private key (kept in CI
secrets), bumps `contentVersion`.

**7. Publish.** Upload the pack + update the `index.json` the app polls. Tag a
GitHub Release. The app's WorkManager job picks it up.

### 7.3 Handling video / training material

Videos can't be embedded offline (size + licensing), so we **turn them into
text study material**:

1. Get the official transcript/captions (or transcribe with an ASR tool).
2. A maintainer distills key points into lesson callouts and flashcards.
3. Store `sourceRef` = video title + timestamp for traceability.
4. Optionally include a **deep link** to the official video in the lesson ("Watch
   the official walkthrough") — a link is fine; redistributing the video is not.

### 7.4 Tooling to build (in a separate `content/` repo or folder)

- `content-cli fetch` — snapshot sources.
- `content-cli diff` — change detection → review issues.
- `content-cli validate` — schema + coverage + link + dupe checks.
- `content-cli build --track <t>` — produce signed pack.
- CI workflow: on merge to `content/authored/**`, validate → build → release.

---

## 8. Legal & sourcing considerations (read before §7)

**This gates the whole content strategy — treat it as a hard requirement.**

- **Do not redistribute Anthropic's copyrighted material.** Docs text, course
  content, and videos are Anthropic's IP. The app must ship **original study
  material we author**, using the official docs as *reference/ground truth*, the
  same way any third-party exam-prep book is written. Direct copy-paste of doc
  prose or verbatim course questions is out.
- **Trademarks.** "Claude", "Anthropic", "Claude Certified Architect" are
  Anthropic marks. Present the app as **unofficial / independent exam prep** and
  include a clear disclaimer ("Not affiliated with or endorsed by Anthropic").
  Do not imply official endorsement in the name, icon, or store listing.
- **Respect site terms & robots.** Fetch official docs politely (rate-limited,
  cached snapshots), honor `robots.txt`, and never bulk-scrape gated course
  content. Anthropic Academy courses are free but access-controlled — use them
  to *learn and author*, not to extract.
- **Code samples** from the Anthropic Cookbook are typically MIT-licensed —
  usable with attribution; verify the license per repo before including code in
  a question.
- **Video:** link out to official videos; don't rehost. Transcribe for your own
  note-taking/authoring, keep transcripts internal.
- **Accuracy disclaimer** in-app: content is best-effort prep and the official
  blueprint governs; encourage users to verify against docs.claude.com.

> Recommendation: keep a `SOURCES.md` in the content repo logging every source
> URL, retrieval date, and license, so provenance is auditable. The pack
> `manifest.sources[]` surfaces a subset in-app ("Based on official docs as of …").

---

## 9. Key algorithms

### 9.1 Spaced repetition (flashcards)

The design shows **Again / Hard / Good / Easy** with intervals (1 min / 2 d /
4 d / 8 d). Implement a lightweight **SM-2 variant**:

- State per card: `ease` (start 2.5), `intervalDays`, `reps`, `lapses`, `dueAt`.
- On grade:
  - **Again** → `reps=0`, interval → learning step (1 min today), `lapses++`,
    `ease = max(1.3, ease-0.2)`.
  - **Hard** → `interval *= 1.2`, `ease -= 0.15`.
  - **Good** → `reps==0 ? 1d/4d graduate : interval *= ease`.
  - **Easy** → `interval *= ease * 1.3`, `ease += 0.15`.
- `dueAt = now + intervalDays`. Daily queue = cards with `dueAt <= today`,
  capped by the **daily card limit** setting.
- Keep the exact first-step intervals data-driven so they can match the design's
  displayed values.

### 9.2 Mock exam scoring

- Score scaled to **/1000** (matching the 720 pass line). Simplest defensible
  mapping: `score = round(1000 * correct / total)`; refine to weighted-by-domain
  if the official exam is weighted.
- **Per-domain breakdown** for the results screen: `correct_d / total_d` per D1–D5.
- Pass = `score >= track.passScore`.
- **Persist every answer immediately** (survive process death): the mock attempt
  row holds `answersJson`; on relaunch, offer "Resume exam" if `status=in_progress`.

### 9.3 Timer

- Countdown from `track.timeLimitMin`. Store `startedAt`; compute remaining from
  wall clock each tick so backgrounding/rotation can't drift it. Auto-submit at 0.

### 9.4 Readiness ring (home)

A blended 0–100 signal for the conic gradient on Home:
`readiness = 0.5*avgMockScore/1000 + 0.3*lessonCompletion + 0.2*flashcardMastery`,
weighted by domain so weak domains pull it down and surface as "weak areas".

### 9.5 Streak

`StreakDay.goalMet = studiedSeconds >= dailyGoal`. Streak = consecutive days
with `goalMet`. Calendar heatmap reads the last 28 days (design shows 4 weeks).

---

## 10. Offline, privacy, storage

- **No network permission needed for core use.** Add `INTERNET` only for the
  optional content-update fetch; guard it behind an explicit user action + the
  weekly opt-in job.
- **All user data local.** No analytics SDKs by default. If any telemetry is
  added later, make it opt-in and local-first.
- **Export/reset** (Settings): export progress as JSON to share/back up; reset
  wipes progress tables but keeps content.
- **Encryption:** progress isn't sensitive; rely on app-private storage. Offer
  SQLCipher only if user demand appears.

---

## 11. Testing, quality, release

- **Unit:** scheduler (§9.1), scoring (§9.2), readiness (§9.4), pack
  verification (signature/checksum/`minAppVersion`).
- **DB:** Room migration tests; content-import idempotency (import twice = same
  state, progress preserved).
- **UI:** Compose UI tests for exam timer, autosave/resume, flag flow, theme
  toggle, glossary search.
- **Content CI:** schema + link + coverage + dupe checks (§7.2 step 5).
- **Accessibility:** TalkBack pass, contrast check against tokens, large-font
  layout check.
- **Release:** internal → closed beta (real candidates) → production. Content
  packs release independently and more often than the APK.

---

## 12. Alternatives considered

- **WebView / PWA wrapping the existing `.dc.html`:** fastest reuse of the
  design files, but weaker offline DB story, worse a11y, and janky timers.
  **Rejected** for the shipping app; the `.dc.html` files remain the visual spec.
- **Flutter / React Native:** viable, but Kotlin+Compose is the lowest-risk path
  for a single-platform, token-driven, offline app. Revisit only if iOS is added.
- **Bundling AI in-app to generate questions:** breaks offline, risks incorrect
  content, and raises the same IP questions. **Rejected**; all content is
  human-authored (§8).

---

## 13. Milestones

| M | Scope | Exit criteria |
|---|-------|---------------|
| M0 | Skeleton: nav, theme/tokens, Room + DataStore, bundled seed pack | App opens, 5 tabs, light/dark, seed content loads offline |
| M1 | Study + Reference: lessons, glossary(FTS), exam guide | Screens 02,03,09,10 done from pack data |
| M2 | Practice + Flashcards: attempts, SR scheduler | Screens 04,05; scheduler unit-tested |
| M3 | Mock + Results: timer, autosave/resume, scoring | Screens 06,07; survives process death |
| M4 | Progress + Onboarding + Settings: charts, streak, export | Screens 01,08,11; readiness ring live |
| M5 | Content pipeline + signed pack updates | `content-cli` builds/signs; in-app update verified |
| M6 | Hardening: a11y, tests, beta | AA+ pass, CI green, closed beta shipped |

---

## 14. Open questions / to confirm

1. **Exact CCAR-P blueprint** (63 Q weighting) — confirm from Pearson VUE before
   authoring D-weights for Professional.
2. **Pass score for Professional** — is it also /1000 with a 720-ish cut? Data-drive it.
3. **App name & branding** that avoids trademark issues (see §8) — decide before store listing.
4. **Content hosting** for updates — GitHub Releases vs a small CDN bucket.
5. **Number of full mock exams** to author per track at launch (design shows 4 in history).

---

*Companion visual spec: `design/README.md`, `design/App Screens.dc.html`,
`design/Home Variations.dc.html` (branch `base_design`).*
