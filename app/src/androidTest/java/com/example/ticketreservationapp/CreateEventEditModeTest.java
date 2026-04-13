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
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

/**
 * Instrumented UI tests for CreateEventActivity in EDIT mode.
 *
 * When launched with edit_event_id extra, the activity pre-fills
 * all fields and shows "Edit Event" / "Update Event" labels.
 */
@RunWith(AndroidJUnit4.class)
public class CreateEventEditModeTest {

    private ActivityScenario<CreateEventActivity> scenario;

    @Before
    public void setUp() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(
            ApplicationProvider.getApplicationContext(), CreateEventActivity.class);
        intent.putExtra("edit_event_id", "evt_123");
        intent.putExtra("edit_title", "Original Title");
        intent.putExtra("edit_description", "Original Description");
        intent.putExtra("edit_date", "2026-07-01");
        intent.putExtra("edit_location", "Toronto");
        intent.putExtra("edit_category", "Sports");
        intent.putExtra("edit_price", 30.0);
        intent.putExtra("edit_total_seats", 150);
        intent.putExtra("edit_organizer_id", "org1");
        intent.putExtra("edit_organizer_name", "Organizer Name");
        scenario = ActivityScenario.launch(intent);
    }

    @After
    public void tearDown() {
        if (scenario != null) {
            scenario.close();
        }
    }

    // ── Edit mode labels ────────────────────────────────────────────────────

    @Test
    public void editMode_titleShowsEditEvent() {
        onView(withId(R.id.tv_form_title)).check(matches(withText("Edit Event")));
    }

    @Test
    public void editMode_subtitleShowsUpdateDetails() {
        onView(withId(R.id.tv_form_subtitle)).check(matches(withText("Update your event details")));
    }

    @Test
    public void editMode_buttonShowsUpdateEvent() {
        onView(withId(R.id.btn_create_event))
            .perform(scrollTo())
            .check(matches(withText("Update Event")));
    }

    // ── Pre-filled fields ───────────────────────────────────────────────────

    @Test
    public void editMode_titleFieldPreFilled() {
        onView(withId(R.id.et_event_title)).check(matches(withText("Original Title")));
    }

    @Test
    public void editMode_descriptionFieldPreFilled() {
        onView(withId(R.id.et_event_description)).check(matches(withText("Original Description")));
    }

    @Test
    public void editMode_dateFieldPreFilled() {
        onView(withId(R.id.et_event_date)).check(matches(withText("2026-07-01")));
    }

    @Test
    public void editMode_locationFieldPreFilled() {
        onView(withId(R.id.et_event_location)).check(matches(withText("Toronto")));
    }

    @Test
    public void editMode_categoryFieldPreFilled() {
        onView(withId(R.id.spinner_event_category)).check(matches(withText("Sports")));
    }

    @Test
    public void editMode_priceFieldPreFilled() {
        onView(withId(R.id.et_event_price))
            .perform(scrollTo())
            .check(matches(withText("30.0")));
    }

    @Test
    public void editMode_seatsFieldPreFilled() {
        onView(withId(R.id.et_event_seats))
            .perform(scrollTo())
            .check(matches(withText("150")));
    }
}
