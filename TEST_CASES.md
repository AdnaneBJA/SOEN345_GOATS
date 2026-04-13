# SOEN 345 Test Cases

## Automated Test Evidence

### JVM Unit Tests
- `LoginViewModelTest`
- `OtpViewModelTest`
- `MyReservationsViewModelTest`
- `EventListViewModelTest`
- `UserTest`
- `ReservationTest`

### Instrumented Tests
- `LoginActivityTest`
- `RegisterActivityTest`
- `MainActivityTest`

## Functional Test Matrix

| ID | Requirement | Test Type | Steps | Expected Result | Actual Result | Evidence |
| --- | --- | --- | --- | --- | --- | --- |
| TC-01 | Search events | Unit / Functional | Load sample events and apply search query | Only matching events remain in filtered list | Passed in JVM suite | `EventListViewModelTest` |
| TC-02 | Filter by category | Unit / Functional | Load events and apply category filter | Only selected category remains | Passed in JVM suite | `EventListViewModelTest` |
| TC-03 | Filter by date | Unit / Functional | Load events and apply date filter | Only matching date remains | Passed in JVM suite | `EventListViewModelTest` |
| TC-04 | Filter by location | Unit / Functional | Load events and apply location filter | Only matching location remains | Passed in JVM suite | `EventListViewModelTest` |
| TC-05 | Combined event filters | Unit / Functional | Apply search, category, location, and date filters together | Filtered list reflects the intersection of all filters | Passed in JVM suite | `EventListViewModelTest` |
| TC-06 | Successful reservation | Manual / Functional | Open event detail, reserve valid quantity, confirm dialog | Reservation is created, seats decrease, confirmation dialog appears | Pending manual execution | Screenshot pending |
| TC-07 | Sold-out / insufficient seats | Manual / Functional | Attempt reservation with requested quantity greater than available seats | Reservation is blocked and user sees error message | Pending manual execution | Screenshot pending |
| TC-08 | Cancel reservation | Manual / Functional | Open My Reservations, cancel reservation | Reservation is removed and seats are restored on event | Pending manual execution | Screenshot pending |
| TC-09 | Login validation | Automated UI | Submit invalid login form | Correct validation message is shown | Implemented in Espresso suite; not executed in this run | `LoginActivityTest` |
| TC-10 | Registration validation | Automated UI | Submit invalid registration form | Correct validation message is shown | Implemented in Espresso suite; not executed in this run | `RegisterActivityTest` |
| TC-11 | Main screen sign-out | Automated UI | Tap sign out from home screen | User session clears and login screen opens | Implemented in Espresso suite; not executed in this run | `MainActivityTest` |

## Screenshot Checklist
- [ ] Reservation success dialog
- [ ] Reservation blocked due to insufficient seats
- [ ] My Reservations cancellation confirmation
- [ ] Event filtering UI with category/date/location example

## Execution Notes
- Update the `Actual Result` column after running the automated suite and manual scenarios.
- Attach captured screenshots to the final report or submission appendix.
- Latest JVM execution: `BUILD SUCCESSFUL` with `61` tests passed, `0` failed.
- Source reports:
  - `app/build/reports/tests/testDebugUnitTest/index.html`
  - `app/build/test-results/testDebugUnitTest/`
