# Architect Exam Prep

A comprehensive Kotlin + Jetpack Compose Android application for studying and practicing for the AWS Solutions Architect Associate (CCARF) and Professional (CCARP) certifications.

## Features

- **Study Module**: Organize lessons by domain with progress tracking
- **Practice Questions**: Timed quizzes by domain with spaced-repetition flashcards
- **Mock Exams**: Full-length mock exams with timed sessions and detailed results
- **Progress Dashboard**: Readiness scores, weekly streaks, weak-area identification
- **Dark Theme**: Full light/dark theme support with optimized contrast for readability
- **Offline-First**: All content bundled in the app; zero internet required
- **Responsive Design**: Pixel-perfect Material Design 3 UI with real variable fonts

## Technology Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose + Material 3
- **Database**: Room (SQLite) with Flow-based observables
- **State Management**: ViewModel + MutableStateFlow
- **Preferences**: DataStore (Proto)
- **Image Loading**: Coil with SVG decoder for bundled diagrams
- **Serialization**: kotlinx.serialization
- **Architecture**: MVVM with dependency injection via factory pattern

## Project Structure

```
app/src/main/java/com/architectprep/app/
├── ui/
│   ├── home/              # Dashboard & exam readiness tracking
│   ├── study/             # Lesson browser & detailed lesson view with zoom
│   ├── practice/          # Question practice sessions & flashcards
│   ├── exam/              # Mock exam timer & results
│   ├── progress/          # Progress analytics & streak tracking
│   ├── settings/          # User preferences & data management
│   ├── reference/         # Exam guide & glossary
│   └── theme/             # Design tokens, fonts, colors
├── data/
│   ├── db/                # Room database entities & DAOs
│   ├── content/           # Content pack import/serialization
│   └── prefs/             # DataStore preferences
└── domain/                # Business logic (streak tracking, etc.)

data/
├── content/               # Content pack JSON definitions
└── fonts/                 # Variable font assets
```

## Building

### Prerequisites

- Android SDK 34+ (API level 34)
- JDK 17+
- Gradle (included via wrapper)

### Local Build

```bash
./gradlew :app:assembleDebug
```

Output APK: `app/build/outputs/apk/debug/app-debug.apk`

### CI/CD

GitHub Actions automatically builds the APK on every push to `main` and feature branches. Artifacts are retained for 30 days.

## Design System

- **Typography**: Source Serif 4 (headers), Public Sans (body), IBM Plex Mono (code)
- **Colors**: Custom domain-ramp for progress visualization, hero dark cards, semantic tokens
- **Spacing**: 4dp grid with Material 3 conventions
- **Components**: Rounded 14-20dp cards, 10-14dp buttons, consistent padding/gap

All design tokens are defined in `ui/theme/Tokens.kt` and sourced from `design/README.md`.

## Content Format

Lessons, questions, and exam metadata are stored as JSON in the bundled `data/content/` directory. The app imports them on first launch via idempotent Room upserts, preserving user progress across content updates.

## Key Implementation Details

- **Progress Bars**: Fixed modifier ordering (padding before height) to ensure visibility
- **Dark Theme**: Dedicated color tokens for code blocks and callouts to maintain readability
- **Image Zoom**: Full-screen pinch-zoom and pan viewer with 1x–6x scale range
- **Spaced Repetition**: SM-2 algorithm for flashcard scheduling (ease, interval, reps tracking)
- **Streak Tracking**: Daily goal auditing via `StreakTracker`; activity recorded on lesson completion and question attempts
- **Navigation**: Single-activity bottom-tab architecture with nested sub-graphs for Study

## License

Proprietary. This app is designed for the AWS Solutions Architect certification track.
