# Android app — M1 (Study + Reference)

Status: **M1 done** from `docs/DEVELOPMENT_DESIGN.md` §13. Built, installed,
and verified end-to-end on an Android emulator (API 35, `etf_test` AVD) —
`gradlew` is now committed, so `./gradlew :app:assembleDebug` /
`:app:installDebug` work directly, no Android Studio required.

## What's implemented

- Gradle project (version catalog, Kotlin 2.0.21, AGP 8.7.3, Compose, KSP),
  with the `gradlew` wrapper (Gradle 8.14.3) committed
- Room database (`data/db/`) with entities matching the content-pack schema
  in `docs/DEVELOPMENT_DESIGN.md` §5: `Track`, `Domain`, `Lesson`, `Question`,
  `GlossaryTerm`, plus a `LessonProgress` table for user data
- `ContentImporter` (`data/content/`): parses the bundled `assets/content/
  ccar-f/*.json` content pack (kotlinx.serialization) and upserts it into
  Room on first launch — idempotent, safe to call every launch
- Compose theme (`ui/theme/`) with the light/dark color tokens ported
  verbatim from `design/README.md`
- A 5-tab bottom-nav shell (`ui/MainActivity.kt`) matching the design's
  Home/Study/Practice/Exam/Progress structure, with per-tab back-stack
  save/restore
- **Home screen** (`ui/home/`): triggers the content import, then renders
  the real track title, exam format line, and a card per domain (code,
  title, weight%, lesson count, real completion progress bar) — all sourced
  from Room, not hardcoded.
- **Study screen** (`ui/study/`, screen 02): domain list with live
  lesson-completion progress → per-domain lesson list → lesson detail.
  "Mark done" writes `LessonProgress` to Room and flows reactively back up
  through the lesson list, domain list, and Home's progress bars.
- **Lesson-body renderer** (`ui/study/LessonDetailScreen.kt`): handles all
  four block types authored into the content pack — `text`, `code`,
  `callout`, and `image`. Bundled SVG diagrams render via Coil + `coil-svg`;
  the one PNG diagram renders via Coil's default decoder; both load
  straight from `assets/`, no network involved.
- **Exam guide** (`ui/reference/ExamGuideScreen.kt`, screen 09) and
  **Glossary** (`ui/reference/GlossaryScreen.kt`, screen 10, with live
  search) — both reachable from cards at the bottom of the Study domain
  list. Guide content is read directly from `guide.json` in assets (no Room
  table — matches the design doc's screen table, which marks 09 as
  content-pack-static).

## What's NOT implemented yet

Practice (M2), Flashcards (M2), Mock exam (M3), Results (M3), Progress
dashboard (M4), Onboarding (M4), Settings (M4), the content-pipeline signed
pack updater (M5), and hardening/a11y/tests (M6) are all still
`PlaceholderScreen`s or unbuilt. See `docs/DEVELOPMENT_DESIGN.md` §13 for
the full milestone list.

## Known gaps carried over from the content pack

As of content pack v3, the entire pack — domains, exam mechanics, all 30
lessons, and 12 of the 34 practice questions — is rebuilt directly from
Anthropic's own official Exam Guide, Certification Exam Policy, and
Certification Terms and Conditions (user-supplied PDFs). See
`content/SOURCES.md` for full provenance. The one remaining gap: the Exam
Guide is versioned ("Version 0.2") and may be revised by Anthropic — check
for a newer version periodically. This doesn't block building or running
the app, only the long-term currency of what it teaches.

## Building locally

```bash
# from the repo root
echo "sdk.dir=/path/to/Android/sdk" > local.properties   # if ANDROID_HOME isn't set
./gradlew :app:assembleDebug
./gradlew :app:installDebug   # with a device/emulator connected
```

`gradlew` is committed (Gradle 8.14.3), so no Android Studio provisioning
step is required — any machine with the Android SDK and JDK 17 can build and
install directly.
