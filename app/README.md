# Android app — M0 scaffold

Status: **M0 (skeleton)** from `docs/DEVELOPMENT_DESIGN.md` §13. Not yet built
or run anywhere — this container has no Android SDK and no network access to
`dl.google.com` (Google's Maven repo), so it could not be compiled in this
session. Open it in Android Studio locally to build; the SDK + `dl.google.com`
will be available there.

## What's implemented

- Gradle project (version catalog, Kotlin 2.0.21, AGP 8.7.3, Compose, KSP)
- Room database (`data/db/`) with entities matching the content-pack schema
  in `docs/DEVELOPMENT_DESIGN.md` §5: `Track`, `Domain`, `Lesson`, `Question`,
  `GlossaryTerm`, plus a `LessonProgress` table for user data
- `ContentImporter` (`data/content/`): parses the bundled `assets/content/
  ccar-f/*.json` content pack (kotlinx.serialization) and upserts it into
  Room on first launch — idempotent, safe to call every launch
- Compose theme (`ui/theme/`) with the light/dark color tokens ported
  verbatim from `design/README.md`
- A 5-tab bottom-nav shell (`ui/MainActivity.kt`) matching the design's
  Home/Study/Practice/Exam/Progress structure
- **Home screen** (`ui/home/`) is the one fully wired screen: it triggers the
  import, then renders the real track title, exam format line, and a card
  per domain (code, title, weight%, lesson count) — all sourced from Room,
  not hardcoded. This is the proof that the content pack → app pipeline works
  end to end.

## What's NOT implemented yet

Everything else is a `PlaceholderScreen`. See the root-level task list for
the exact remaining work (screens 02–13, user progress writes, spaced
repetition, mock exam timer/scoring, settings, content-pack updater).

## Known gaps carried over from the content pack

As of content pack v2, D1 and D2 are both verified against official docs
(see `content/SOURCES.md`). The one remaining gap is the exam format itself
(question count/time limit/pass score/domain weights in `guide.json` and
`domains.json`) — still sourced from web-search summaries, not a direct
fetch of Anthropic's certification pages. This doesn't block building or
running the app, only the correctness of what it teaches.

## Building locally

```bash
# from the repo root, in Android Studio or via CLI with ANDROID_HOME set
./gradlew :app:assembleDebug
```

There's no `gradlew` wrapper committed yet (this session couldn't download
the Gradle distribution zip either) — run `gradle wrapper --gradle-version 8.14.3`
once locally to generate it, or open the project directly in Android Studio,
which provisions its own Gradle/SDK.
