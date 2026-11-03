# HDDC — Home Dashboard for Clash

HDDC is a professional-grade Clash of Clans intelligence and management platform for Android built with modern Android development practices (Kotlin, Jetpack Compose, Room SQLite persistence, OkHttp with proxy support, and Material Design 3).

## Features

- **Executive Dashboard**: Quick search for players and clans, summary statistics, tracked player cards, and recent snapshot activity.
- **Player Intelligence**: Live Supercell API player statistics, Town Hall badge, heroes, hero equipment, troops, spells, and achievements.
- **Historical Snapshots & Charts**: Canvas-based trophy progression line charts and donation health ratios.
- **Clan Intelligence**: Clan details, clan capital hall level & points, war streak, policies, and complete member rosters with sorting.
- **War Center & CWL**: Live war telemetry, stars and destruction percentage scoreboard, member attack log, personal war planner with target base assignments, and Clan War League tracking.
- **Analytics**: Town Hall level breakdown distribution bar chart, head-to-head player comparisons, and war win rate metrics.
- **Clash Calculators**: Builder upgrade time reduction (Gold Pass 10%, 15%, 20% + Builder Potions), wall resource & ring planner, war tiebreaker analyzer, and donation health calculator.
- **Secure Credentials**: AES-256-GCM encryption for Supercell API tokens and Node4 proxy credentials with live connection testing.

---

## GitHub Actions CI/CD

This repository includes automated CI/CD workflows configured in `.github/workflows/android.yml`:
- **Triggers**: On every `push` to `main` or `master`, pull requests, and manual triggers (`workflow_dispatch`).
- **Tasks**:
  1. Sets up JDK 21 and Android SDK environment.
  2. Restores `debug.keystore` from `debug.keystore.base64`.
  3. Runs unit tests (`./gradlew testDebugUnitTest`).
  4. Builds the debug APK (`./gradlew assembleDebug`).
  5. Uploads the compiled APK as a downloadable GitHub Actions artifact (`hddc-debug-apk`).

---

## Local Build Instructions

```bash
# Make gradlew executable
chmod +x gradlew

# Run Unit Tests
./gradlew testDebugUnitTest

# Build Debug APK
./gradlew assembleDebug
```

The output APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`
