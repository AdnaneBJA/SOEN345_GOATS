package com.example.ticketreservationapp;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.ticketreservationapp.view.EventListActivity;
import com.example.ticketreservationapp.view.MyReservationsActivity;
import com.google.firebase.auth.FirebaseAuth;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

/**
 * Instrumented UI tests for MainActivity navigation buttons.
 *
 * Verifies that tapping each navigation button on the home screen
 * opens the correct target activity. Extends the existing MainActivityTest
 * with navigation coverage for Browse Events and My Reservations.
 */
@RunWith(AndroidJUnit4.class)
public class MainActivityNavigationTest {

    private ActivityScenario<MainActivity> scenario;

    @Before
    public void setUp() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(
            ApplicationProvider.getApplicationContext(), MainActivity.class);
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
    public void browseEventsButton_isDisplayed() {
        onView(withId(R.id.btn_browse_events)).check(matches(isDisplayed()));
    }

    @Test
    public void myReservationsButton_isDisplayed() {
        onView(withId(R.id.btn_my_reservations)).check(matches(isDisplayed()));
    }

    @Test
    public void browseEventsButton_hasCorrectText() {
        onView(withId(R.id.btn_browse_events)).check(matches(withText("Browse Events")));
    }

    @Test
    public void myReservationsButton_hasCorrectText() {
        onView(withId(R.id.btn_my_reservations)).check(matches(withText("My Reservations")));
    }

    @Test
    public void signOutButton_hasCorrectText() {
        onView(withId(R.id.btn_sign_out)).check(matches(withText("Sign Out")));
    }

    // ── Navigation to Browse Events ─────────────────────────────────────────

    @Test
    public void clickBrowseEvents_opensEventListActivity() {
        Intents.init();
        try {
            onView(withId(R.id.btn_browse_events)).perform(click());
            intended(hasComponent(EventListActivity.class.getName()));
        } finally {
            Intents.release();
        }
    }

    // ── Navigation to My Reservations ───────────────────────────────────────

    @Test
    public void clickMyReservations_opensMyReservationsActivity() {
        Intents.init();
        try {
            onView(withId(R.id.btn_my_reservations)).perform(click());
            intended(hasComponent(MyReservationsActivity.class.getName()));
        } finally {
            Intents.release();
        }
    }
}
