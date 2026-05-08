# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this
repository.

## Project Overview

CloudStream is an extension-based Android multimedia player with tracking support. The app itself
provides no video sources by default — functionality comes from installable extensions (plugins). It
supports phone, Android TV, and Chromecast.

## Build Commands

```bash
# Build debug APK (prerelease flavor, used in CI)
./gradlew assemblePrereleaseDebug

# Run lint checks
./gradlew lint

# Run unit tests
./gradlew test

# Run a single test class
./gradlew :app:testDebugUnitTest --tests "com.lagradost.cloudstream3.SubtitleSelectionTest"

# Generate API documentation
./gradlew dokkaGenerate
```

- Min SDK: 23, Target/Compile SDK: 36
- JDK Toolchain: Java 21, JVM Target: Java 1.8
- Gradle version: 9.3.1

## Module Structure

- **`:app`** — Android application (activities, UI, services, sync providers)
- **`:library`** — Kotlin Multiplatform library (commonMain/jvmMain/androidMain) containing
  providers, extractors, plugin system, and network utilities
- **`:docs`** — Documentation generation (Dokka)

## Architecture

**MVVM pattern** with `Resource<T>` sealed class (Success/Failure/Loading) for state management.
Uses `safe()` and `safeAsync()` coroutine wrappers for error handling.

### Provider System (`library/src/commonMain/kotlin/com/lagradost/cloudstream3/`)

- **`MainAPI`** — Base class for content providers. Registered in `APIHolder` with thread-safe
  collections.
- **`ExtractorApi`** (`utils/ExtractorApi.kt`) — Base class for video host extractors. 100+
  implementations in `extractors/`.
- **Meta providers** in `metaproviders/`: TMDB, MyDramaList, Trakt, AniList.

### Plugin System (`library/src/commonMain/kotlin/com/lagradost/cloudstream3/plugins/`)

- **`BasePlugin`** — Plugins register `MainAPI` and `ExtractorApi` instances via `registerMainAPI()`
  and `registerExtractorAPI()`.
- **`@CloudstreamPlugin`** annotation marks plugin entry points.
- **`PluginManager`** (app module) handles loading/unloading plugins at runtime.
- Extension dev docs: https://recloudstream.github.io/csdocs/devs/gettingstarted/

### Sync Providers (`app/src/main/java/com/lagradost/cloudstream3/syncproviders/`)

AniList, MAL, Simkl, Kitsu, OpenSubtitles, Addic7ed, LocalList.

### Key Libraries

- **Media3/ExoPlayer** (1.9.2) — Video playback with HLS, DASH, casting
- **Coil** (3.3.0) — Image loading
- **NiceHttp** — HTTP client
- **Jsoup** — HTML parsing
- **Jackson** — JSON serialization
- **Rhino** — JavaScript execution engine
- **NewPipeExtractor** — Video extraction utilities
- **FuzzyWuzzy** — Fuzzy string matching for search

### App Entry Points

- `AccountSelectActivity` — Launcher activity (account selection)
- `MainActivity` — Main app screen
- `CloudStreamApp` — Application class (image loading, crash handling, global context)

### Custom URI Schemes

`cloudstreamplayer://`, `cloudstreamapp://`, `cloudstreamrepo://`, `cloudstreamsearch://`,
`magnet://`

## Build Variants

Two product flavors: **stable** and **prerelease**, each with separate signing configs. CI runs
`assemblePrereleaseDebug` and `lint` on pull requests.

## AI Policy

Per `AI-POLICY.md`: State AI usage in PRs, always test code before submitting, be able to explain
and fix any submitted code. Contributors know the codebase better than AI — defer to human
reviewers.

## Translations

Managed via Weblate. Lint rules for `ByteOrderMark` and `MissingTranslation` are suppressed since
Weblate handles those.
