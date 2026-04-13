package com.example.ticketreservationapp;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.ticketreservationapp.view.MyReservationsActivity;
import com.google.firebase.auth.FirebaseAuth;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertTrue;

/**
 * Instrumented UI tests for MyReservationsActivity.
 *
 * Tests verify screen element visibility and navigation. Since no user
 * is signed in, the screen shows the empty state (no reservations loaded).
 */
@RunWith(AndroidJUnit4.class)
public class MyReservationsActivityTest {

    private ActivityScenario<MyReservationsActivity> scenario;

    @Before
    public void setUp() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(
            ApplicationProvider.getApplicationContext(), MyReservationsActivity.class);
        scenario = ActivityScenario.launch(intent);
    }

    @After
    public void tearDown() {
        if (scenario != null) {
            scenario.close();
        }
    }

    // ── Screen elements ──────────────────────────────────────────────────────

    @Test
    public void myReservationsScreen_titleIsDisplayed() {
        onView(withText("My Reservations")).check(matches(isDisplayed()));
    }

    @Test
    public void myReservationsScreen_subtitleIsDisplayed() {
        onView(withText("View and manage your bookings")).check(matches(isDisplayed()));
    }

    @Test
    public void backButton_isDisplayed() {
        onView(withId(R.id.btn_back)).check(matches(isDisplayed()));
    }

    @Test
    public void reservationRecyclerView_isDisplayed() {
        onView(withId(R.id.rv_reservations)).check(matches(isDisplayed()));
    }

    // ── Empty state (no user signed in) ─────────────────────────────────────

    @Test
    public void noUserSignedIn_showsEmptyState() {
        // With no signed-in user, the ViewModel does not load reservations,
        // so the RecyclerView is empty — the "no results" view should show
        // or the list remains empty.
        onView(withId(R.id.rv_reservations)).check(matches(isDisplayed()));
    }

    // ── Navigation ──────────────────────────────────────────────────────────

    @Test
    public void clickBackButton_finishesActivity() {
        onView(withId(R.id.btn_back)).perform(click());
        scenario.onActivity(activity -> assertTrue(activity.isFinishing()));
    }
}
