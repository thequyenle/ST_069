---
description: Repository Information Overview
alwaysApply: true
---

# Fake Call Phone Prank App Information

## Summary
An Android application that allows users to set up and receive fake phone calls. The app provides features for scheduling calls, customizing caller information, and simulating incoming call screens.

## Structure
- **app/**: Main application module containing all source code and resources
  - **src/main/java/net/android/st069_fakecallphoneprank/**: Contains all Kotlin source files
  - **src/main/res/**: Contains Android resources (layouts, drawables, values)
  - **src/test/**: Contains unit tests
  - **src/androidTest/**: Contains instrumentation tests

## Language & Runtime
**Language**: Kotlin
**Version**: 1.9.24
**Build System**: Gradle (Kotlin DSL)
**Package Manager**: Gradle
**Compile SDK**: 34
**Min SDK**: 26
**Target SDK**: 34
**JVM Target**: 11

## Dependencies
**Main Dependencies**:
- AndroidX Core KTX (1.12.0)
- AndroidX AppCompat (1.7.1)
- Material Design (1.13.0)
- AndroidX Activity (1.8.2)
- AndroidX ConstraintLayout (2.2.1)
- Room Database (2.5.2)
- SQLite (2.3.1)
- Retrofit (2.9.0)
- OkHttp (4.11.0)
- Coroutines (1.7.3)
- Lifecycle Components (2.7.0)
- WorkManager (2.9.0)
- ViewPager2 (1.0.0)
- Navigation Components (2.7.5)
- Glide (4.16.0)

**Development Dependencies**:
- JUnit (4.13.2)
- AndroidX Test JUnit (1.3.0)
- Espresso (3.7.0)
- KSP (1.9.0-1.0.13)

## Build & Installation
```bash
# Clean and build the project
./gradlew clean build

# Install the debug version on a connected device
./gradlew installDebug

# Run unit tests
./gradlew test

# Run instrumentation tests
./gradlew connectedAndroidTest
```

## Main Components
**Activities**:
- SplashActivity: Entry point (launcher activity)
- IntroActivity: Onboarding screens
- HomeActivity: Main dashboard
- AddFakeCallActivity: Create new fake calls
- IncomingCallActivity: Simulates incoming call screen
- ActiveCallActivity: Simulates active call screen

**Services & Receivers**:
- FakeCallReceiver: Handles scheduled call alarms
- WorkManager jobs for background processing

**Architecture Components**:
- ViewModel: FakeCallViewModel for UI state management
- Room Database: For local storage of call data
- Retrofit: For API communication

## Testing
**Framework**: JUnit, Espresso
**Test Location**: 
- Unit tests: app/src/test/java/
- Instrumentation tests: app/src/androidTest/java/
**Run Command**:
```bash
# Run unit tests
./gradlew test

# Run instrumentation tests
./gradlew connectedAndroidTest
```