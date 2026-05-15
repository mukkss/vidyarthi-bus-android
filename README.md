# Vidyarthi Bus - Crowdsourced Bus Alert

Vidyarthi Bus is a lightweight Android application designed to solve the last-mile information problem for rural college students in India. It empowers students with real-time bus crowd intelligence using a crowdsourced reporting model.

## Features
- **Real-time Crowd Meter:** View bus occupancy (Empty, Seated, Full) with sub-2 second updates.
- **One-tap Reporting:** Students on board can report crowd status with GPS validation.
- **GPS Corridor Validation:** Prevents false reports by ensuring users are within 500m of the bus route.
- **Automatic Report Expiry:** Stale reports expire after 15 minutes to ensure data freshness.
- **Shared Auto Directory:** Find alternative transport contacts when the bus is full.
- **Offline Support:** Displays last known status when internet is unavailable.

## Tech Stack
- **Kotlin & Jetpack Compose:** Modern Android development.
- **MVVM + Repository Pattern:** Clean architecture for scalability.
- **Firebase Realtime Database:** Low-latency live synchronization.
- **Dagger Hilt:** Dependency Injection.
- **Kotlin Coroutines & Flow:** Reactive programming.
- **Material 3:** Modern, accessible UI/UX.

## Setup Instructions

### Prerequisites
1. Android Studio Ladybug or later (recommended).
2. Firebase account.

### Firebase Configuration
1. Create a new Firebase project at [firebase.google.com](https://firebase.google.com).
2. Add an Android app with package name `com.mukesh.vidyarthibus`.
3. Download the `google-services.json` and place it in the `app/` directory.
4. Enable **Anonymous Authentication** in the Firebase Console.
5. Enable **Realtime Database** and set the rules from `firebase/database.rules.json`.
6. (Optional) Deploy Cloud Functions from `firebase/functions/` for automatic report expiry.

### Build and Run
1. Clone the repository.
2. Run `./gradlew assembleDebug` to build the debug APK.
3. Install the APK on your device/emulator.

## Firebase Data Schema
The app expects the following structure in RTDB:
- `routes/`: List of bus routes with corridor coordinates.
- `crowd_reports/`: Real-time status reports per route.
- `alternatives/`: Contact information for shared autos per route.

## License
Copyright © 2025 Mukesh. All rights reserved.
