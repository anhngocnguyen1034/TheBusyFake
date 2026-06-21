# The Busy Simulator 📱

> **Fake it till you make it** — escape any awkward situation with a fake call, chat, or notification.

A Gen-Z themed Android prank app that lets you schedule fake incoming calls, simulate chat conversations, and send fake message notifications — all with a bold Neo-Brutalism UI.

---

## Features

### 📞 Fake Call
- Schedule a fake incoming call with a custom caller name and phone number
- Choose delay: immediately, 1 min, 5 min, 30 min, or custom seconds
- Full-screen incoming call UI (swipe to answer / decline)
- Fake video call screen with camera toggle
- Vibration & LED flash on incoming call
- Cancel pending scheduled calls
- Avatar shown on incoming call screen (pulled from contact photo)

### 💬 Fake Chat
- Create fake chat contacts with custom name, avatar, and verified badge
- Simulate full two-way conversations (send & receive messages)
- Supports image messages, reply-to, and message deletion
- Auto-replies from preset contacts (Mom, Partner, Doctor, Scientist)
- Delete contact and entire chat history

### 🔔 Fake Notification
- Schedule fake message notifications on the lock screen
- Custom sender name and message content
- LED flash option on notification delivery
- Cancel pending scheduled notifications
- Notification history log

### ⚙️ Settings
- Light / Dark / System theme
- Multi-language support (20+ languages)
- Privacy Policy, Rate Us, Feedback

---

## Screenshots

> _Coming soon_

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin 2.0.21 |
| UI | Jetpack Compose + Material Design 3 |
| Architecture | Clean Architecture (domain / data / presentation) |
| Database | Room v8 |
| DI | Manual (AppContainer) |
| Navigation | Compose Navigation |
| Scheduling | AlarmManager (exact alarms) |
| Image loading | Coil |
| Settings | DataStore Preferences |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 36 |

---

## Architecture

```
domain/         # Pure Kotlin — models, repository interfaces, use cases
data/           # Room DAOs, entities, mappers, repository implementations
presentation/   # Jetpack Compose UI, ViewModels, services, receivers
```

Dependency direction: `presentation → domain ← data`

---

## Build & Run

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Run unit tests
./gradlew test

# Lint
./gradlew lint
```

> Requires Android Studio Hedgehog or newer, JDK 11+

---

## CI / CD

- Jenkinsfile triggers on `main`, `develop`, `testing`, `release` branches
- Auto-increments version code per branch (`develop` → patch, `release` → minor, `main` → major)
- Builds APK and uploads to GitHub Releases
- Sends Discord notification with QR code for direct APK download

---

## Permissions

| Permission | Reason |
|-----------|--------|
| `SCHEDULE_EXACT_ALARM` | Schedule fake calls/messages at exact time |
| `USE_FULL_SCREEN_INTENT` | Show full-screen incoming call UI |
| `CAMERA` | Fake video call screen |
| `RECEIVE_BOOT_COMPLETED` | Restore alarms after reboot |
| `INTERNET` | Load privacy policy web page |
| `POST_NOTIFICATIONS` | Show fake message notifications |

---

## Privacy Policy

Hosted at: https://anhngocnguyen1034.github.io/privacy-policy/

---

## License

This project is for personal and educational use only.  
© 2024 anhnn. All rights reserved.
