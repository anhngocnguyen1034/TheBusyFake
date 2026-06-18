# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
./gradlew assembleDebug           # Build debug APK
./gradlew assembleRelease         # Build release APK
./gradlew test                    # Run unit tests
./gradlew connectedAndroidTest    # Run instrumented tests (device/emulator required)
./gradlew lint                    # Run lint checks
./gradlew clean build             # Clean and full build
```

Run a single test class:
```bash
./gradlew test --tests "com.example.thebusysimulator.ExampleUnitTest"
```

CI builds are handled by `.anhnn/build.sh` (called from `Jenkinsfile`) — it auto-increments version, builds, and uploads to GitHub Releases with a QR code Discord notification.

## SDK & Language Versions

- `compileSdk` / `targetSdk` = 36, `minSdk` = 24
- Kotlin 2.0.21, Java 11, KSP for annotation processing
- Compose BOM 2024.09.00, Room 2.6.1, Navigation Compose 2.8.4

## Architecture

Clean Architecture with 3 layers. Dependency direction: `presentation → domain ← data`.

```
domain/       # Pure Kotlin — no Android dependencies
  model/      # FakeCall, Message, ChatMessage, FakeNotification
  repository/ # Repository interfaces
  usecase/    # Schedule/Cancel/Get/MarkCompleted for fake calls
  util/       # CallScheduler interface

data/         # Android-aware implementations
  dao/        # Room DAOs (FakeCall, Message, ChatMessage, FakeNotification)
  database/   # AppDatabase (Room v8, 7 migrations)
  datasource/ # Local data sources (Room + DataStore)
  mapper/     # Entity ↔ Domain model converters
  model/      # Room entities + data models
  repository/ # Repository implementations

presentation/ # UI layer (Jetpack Compose)
  di/         # AppContainer — manual DI via lazy singletons (no Hilt/Koin)
  navigation/ # NavGraph + Screen sealed class (11 routes)
  receiver/   # BroadcastReceivers: FakeCallReceiver, FakeMessageReceiver
  service/    # Foreground services: FakeCallNotificationService, FakeCallService, FakeMessageNotificationService
  ui/screen/  # One Compose file per screen
  ui/theme/   # Material Design 3, Light/Dark/System via DataStore
  ui/component/ # Reusable Compose components
  viewmodel/  # FakeCallViewModel, MessageViewModel, FakeMessageViewModel
  util/       # AlarmSchedulerImpl, PermissionHelper, LanguageManager, DateUtils, ImageHelper
```

Activities: `MainActivity`, `FakeCallActivity` (full-screen incoming call UI), `FakeVideoCallActivity`.

## Key Patterns

- **Dependency Injection**: `AppContainer` singleton requires `AppContainer.init(context)` before use. All deps are `lazy`.
- **Navigation**: Compose Navigation. `Screen.kt` sealed class defines all 11 routes. `Chat` route uses URL-encoded args: `chat/{contactName}/{messageId}`.
- **Database**: Room with explicit migrations. **Always add a migration when changing the schema.** `AppDatabase` is at version 8. Version code formula: `major*1000000 + minor*10000 + patch*100 + develop`.
- **Settings persistence**: DataStore Preferences for theme mode and language. Language switching requires `attachBaseContext` override.
- **Fake Call flow**: `ScheduleFakeCallUseCase` → `AlarmSchedulerImpl` (AlarmManager exact alarm) → `FakeCallReceiver` → `FakeCallNotificationService` → `FakeCallActivity` (full-screen intent).
- **Permissions required**: `SCHEDULE_EXACT_ALARM`, `USE_FULL_SCREEN_INTENT`, `CAMERA`, `SYSTEM_ALERT_WINDOW` (overlay).

## CI / Discord Notifications

Jenkinsfile triggers on branches: `main`, `develop`, `testing`, `release`.
Three Discord webhooks in `Jenkinsfile` env block: GitHub events, Jenkins CI status, build success (with APK QR code).
Version auto-bump per branch: `develop` → patch4, `testing` → patch3, `release` → minor, `main` → major.
