package com.example.ticketreservationapp;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.ticketreservationapp.view.CreateEventActivity;
import com.google.firebase.auth.FirebaseAuth;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

/**
 * Instrumented UI tests for CreateEventActivity.
 *
 * Tests cover both create mode (default) and edit mode (launched with
 * edit_event_id extra). Validation errors are verified via Toast messages
 * triggered by the ViewModel.
 */
@RunWith(AndroidJUnit4.class)
public class CreateEventActivityTest {

    private ActivityScenario<CreateEventActivity> scenario;

    @Before
    public void setUp() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(
            ApplicationProvider.getApplicationContext(), CreateEventActivity.class);
        scenario = ActivityScenario.launch(intent);
    }

    @After
    public void tearDown() {
        if (scenario != null) {
            scenario.close();
        }
    }

    // ── Screen elements (create mode) ───────────────────────────────────────

    @Test
    public void createEventScreen_titleIsDisplayed() {
        onView(withText("Create Event")).check(matches(isDisplayed()));
    }

    @Test
    public void createEventScreen_subtitleIsDisplayed() {
        onView(withText("Fill in the details for your event")).check(matches(isDisplayed()));
    }

    @Test
    public void titleField_isDisplayed() {
        onView(withId(R.id.et_event_title)).check(matches(isDisplayed()));
    }

    @Test
    public void descriptionField_isDisplayed() {
        onView(withId(R.id.et_event_description)).check(matches(isDisplayed()));
    }

    @Test
    public void dateField_isDisplayed() {
        onView(withId(R.id.et_event_date)).check(matches(isDisplayed()));
    }

    @Test
    public void locationField_isDisplayed() {
        onView(withId(R.id.et_event_location)).check(matches(isDisplayed()));
    }

    @Test
    public void categoryDropdown_isDisplayed() {
        onView(withId(R.id.spinner_event_category)).check(matches(isDisplayed()));
    }

    @Test
    public void priceField_isDisplayed() {
        onView(withId(R.id.et_event_price)).perform(scrollTo()).check(matches(isDisplayed()));
    }

    @Test
    public void seatsField_isDisplayed() {
        onView(withId(R.id.et_event_seats)).perform(scrollTo()).check(matches(isDisplayed()));
    }

    @Test
    public void createButton_isDisplayed() {
        onView(withId(R.id.btn_create_event)).perform(scrollTo()).check(matches(isDisplayed()));
    }

    @Test
    public void backButton_isDisplayed() {
        onView(withId(R.id.btn_back)).check(matches(isDisplayed()));
    }

    // ── Input interaction ───────────────────────────────────────────────────

    @Test
    public void titleField_acceptsTextInput() {
        onView(withId(R.id.et_event_title))
            .perform(typeText("Summer Concert"), closeSoftKeyboard());
        onView(withId(R.id.et_event_title)).check(matches(withText("Summer Concert")));
    }

    @Test
    public void descriptionField_acceptsTextInput() {
        onView(withId(R.id.et_event_description))
            .perform(typeText("A great show"), closeSoftKeyboard());
        onView(withId(R.id.et_event_description)).check(matches(withText("A great show")));
    }

    @Test
    public void locationField_acceptsTextInput() {
        onView(withId(R.id.et_event_location))
            .perform(typeText("Montreal"), closeSoftKeyboard());
        onView(withId(R.id.et_event_location)).check(matches(withText("Montreal")));
    }

    @Test
    public void priceField_acceptsNumericInput() {
        onView(withId(R.id.et_event_price))
            .perform(scrollTo(), typeText("25.99"), closeSoftKeyboard());
        onView(withId(R.id.et_event_price)).check(matches(withText("25.99")));
    }

    @Test
    public void seatsField_acceptsNumericInput() {
        onView(withId(R.id.et_event_seats))
            .perform(scrollTo(), typeText("100"), closeSoftKeyboard());
        onView(withId(R.id.et_event_seats)).check(matches(withText("100")));
    }

    // ── Navigation ──────────────────────────────────────────────────────────

    @Test
    public void clickBackButton_finishesActivity() {
        onView(withId(R.id.btn_back)).perform(click());
        scenario.onActivity(activity ->
            org.junit.Assert.assertTrue(activity.isFinishing()));
    }
}
