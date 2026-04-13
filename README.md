# Cloud-based Ticket Reservation App

A native Android application that lets users browse events, reserve tickets, and receive digital confirmations.

## Team

| Name | Student ID |
|------|------------|
| Karim Mawji | 40281154 |
| Yanis Djeridi | 40227313 |
| Adnane Bejja | 40264362 |
| Omar Abdullah Ghazaly | 40280795 |

## Overview

This app was developed as a team project for **SOEN 345 (Software Testing, Verification and Quality Assurance)** at Concordia University. Customers can search and filter events by category, location, or date, then reserve tickets. Organizers can create, edit, and cancel events. The backend uses Firebase Authentication for user management and Cloud Firestore for data persistence.

## Features

**Customer**
- Register and log in with email/password or phone (OTP)
- Browse all available events
- Search by keyword and filter by category, location, and date
- View event details (price, seats, organizer)
- Reserve tickets and receive a confirmation code
- View and cancel reservations

**Organizer**
- Create new events with full details (title, date, location, category, price, seats)
- Edit or cancel existing events
- View a dashboard of personally created events

**Testing & QA**
- 143 JVM unit tests (JUnit 5) covering models, ViewModels, and validation logic
- 109 Espresso instrumented tests covering UI elements, navigation, and client-side validation
- JaCoCo code coverage (94.5% line coverage on JVM-testable code)
- GitHub Actions CI for automated build, test, and coverage reporting

## Tech Stack

| Layer | Technology |
|-------|------------|
| Language | Java 11 |
| Platform | Android SDK 36 (min SDK 24) |
| Auth | Firebase Authentication |
| Database | Cloud Firestore |
| UI | Material Components for Android |
| Architecture | MVVM (ViewModel + LiveData) |
| Build | Gradle 9.1.0 (Kotlin DSL) |
| Unit Tests | JUnit Jupiter 5.10.2 |
| UI Tests | Espresso 3.7.0, AndroidX Test 1.6.1 |
| Coverage | JaCoCo 0.8.13 |
| CI | GitHub Actions |

## Project Structure

```
app/src/main/java/.../
├── model/          Data classes (User, Event, Reservation)
├── repository/     Firebase calls (Auth, Events, Reservations)
├── viewmodel/      ViewModels with LiveData and validation
├── view/           Activities (Login, Register, EventList, etc.)
├── MainActivity.java

app/src/test/       JVM unit tests (JUnit 5)
app/src/androidTest/ Instrumented UI tests (Espresso)
.github/workflows/  CI pipeline (android.yml)
gradle/libs.versions.toml  Centralized dependency versions
```

## Setup

**Prerequisites**
- Android Studio
- JDK 17
- An Android device or emulator (API 24+)

**Steps**
1. Clone the repository
2. Open the project in Android Studio
3. Add your `google-services.json` to `app/` (requires a Firebase project with Auth and Firestore enabled)
4. Sync Gradle
5. Run on an emulator or connected device

## Running Tests

```bash
# JVM unit tests
./gradlew test

# Instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest

# Generate JaCoCo coverage report
./gradlew jacocoTestReport
# Report: app/build/reports/jacoco/jacocoTestReport/html/index.html
```

## CI/CD

GitHub Actions runs on every push and PR to `main`:
1. Builds the project with JDK 17
2. Runs all JVM unit tests
3. Generates and uploads the JaCoCo coverage report

## Documentation

The full project report covers architecture diagrams, design decisions, testing strategy, coverage analysis, and future work. See the report submitted for SOEN 345 for details.

The full project report is available in the submitted zip folder under the file `SOEN345_FinalReport_GOATS`
