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

CI builds are handled by `.anhnn/build.sh` (called from `Jenkinsfile`) — it auto-increments version, builds, and sends Discord notifications.

## Architecture

Clean Architecture with 3 layers. Dependency direction: `presentation → domain ← data`.

```
domain/       # Pure Kotlin — no Android dependencies
  model/      # Domain entities (FakeCall, Message, ChatMessage, FakeNotification)
  repository/ # Repository interfaces
  usecase/    # Business logic (Schedule/Cancel/Get/MarkCompleted for fake calls)

data/         # Android-aware implementations
  dao/        # Room DAOs
  database/   # AppDatabase (Room v8, 7 migrations)
  datasource/ # Local data sources (Room + DataStore)
  mapper/     # Entity ↔ Domain model converters
  repository/ # Repository implementations

presentation/ # UI layer (Jetpack Compose)
  di/         # Manual DI via AppContainer (lazy singletons — not Hilt/Koin)
  navigation/ # NavGraph + Screen sealed class
  receiver/   # BroadcastReceivers for fake call/message triggers
  service/    # Foreground services for fake call notifications
  ui/screen/  # Compose screens (one file per screen)
  ui/theme/   # Material Design 3, supports Light/Dark/System via DataStore
  viewmodel/  # FakeCallViewModel, MessageViewModel, FakeMessageViewModel
  util/       # AlarmSchedulerImpl, PermissionHelper, LanguageManager
```

## Key Patterns

- **Dependency Injection**: Manual `AppContainer` in `presentation/di/`. All dependencies are `lazy`. No Hilt/Koin.
- **Navigation**: Compose Navigation. Screens defined in `Screen.kt` as a sealed class/object.
- **Database**: Room with explicit migrations. Always add a migration when changing schema — `AppDatabase` is currently at version 8.
- **Settings persistence**: DataStore Preferences (theme mode, language).
- **Fake Call flow**: `ScheduleFakeCallUseCase` → `AlarmSchedulerImpl` (AlarmManager exact alarm) → `FakeCallReceiver` → `FakeCallNotificationService` → `FakeCallActivity` (full-screen intent).
- **Permissions required**: `SCHEDULE_EXACT_ALARM`, `USE_FULL_SCREEN_INTENT`, camera.

## CI / Discord Notifications

Jenkinsfile triggers on branches: `main`, `develop`, `testing`, `release`.
Three Discord webhooks are configured in `Jenkinsfile` env block (GitHub events, Jenkins CI status, build success).
Version is auto-bumped per branch: `develop` → patch4, `testing` → patch3, `release` → minor, `main` → major.
