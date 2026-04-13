# SOEN 345 Test Plan

## Scope
This plan covers the Ticket Reservation Application features required by the project brief:
- authentication and registration
- event browsing, search, and filtering
- organizer event management
- customer reservations and reservation cancellation

## Test Strategy

### Unit Testing
Purpose:
- validate ViewModel and model logic without Android UI dependencies
- verify validation rules and filtering behavior deterministically

Current unit-test focus:
- `LoginViewModel`
- `OtpViewModel`
- `MyReservationsViewModel`
- `EventListViewModel`
- model POJOs such as `User` and `Reservation`

### Component / Integration Testing
Purpose:
- validate interactions between app components and Android UI flow
- verify navigation, input validation messages, and visible screen elements

Current component/integration focus:
- `LoginActivityTest`
- `RegisterActivityTest`
- `MainActivityTest`

### Functional / Acceptance Testing
Purpose:
- validate key user flows against the assignment requirements
- record expected vs actual results for the final report

Current manual/acceptance focus:
- browse events
- filter events by category/date/location
- reserve tickets
- block reservations when seats are unavailable
- cancel reservations and restore seats

## Test Environment
- IDE: Android Studio
- Build system: Gradle 9.1.0 wrapper
- Language: Java
- Unit test framework: JUnit 4
- UI test framework: Espresso
- Backend: Firebase Auth + Firestore
- CI: GitHub Actions workflow in `.github/workflows/android.yml`

## Entry Criteria
- project builds successfully
- Firebase configuration is present
- test sources compile
- target feature code is merged into `main`

## Exit Criteria
- JVM unit tests pass
- applicable instrumented tests pass in emulator/device environment
- critical user flows have test case evidence recorded
- no blocking defects remain for reservation, cancellation, or filtering flows

## Out of Scope / Constraints
- SMS confirmation depends on external Firebase/Twilio extension configuration
- Firestore transaction behavior is not currently covered by emulator-backed automated integration tests
- screenshot capture requires emulator/device execution and is tracked in the test case document

## Risks
- reservation flow depends on Firebase runtime configuration
- CI currently runs `./gradlew build`, but emulator-based tests are not executed in CI
- missing emulator access can block screenshot and Espresso execution evidence

## Latest Execution Result
- JVM suite executed with `env GRADLE_USER_HOME=/Users/kouim07/SOEN345_GOATS/.gradle-home bash gradlew test`
- Result: passed
- Timestamp from Gradle XML reports: `2026-04-08`
- Total JVM tests passed: `61`
- Failures: `0`
- Errors: `0`
- Skipped: `0`
- HTML report: `app/build/reports/tests/testDebugUnitTest/index.html`
- XML reports: `app/build/test-results/testDebugUnitTest/`

## Current Gaps In Evidence
- Espresso / instrumented tests were not executed in this run because that requires emulator or device setup.
- Manual screenshots for reservation and filtering flows still need to be captured from a running app session.
