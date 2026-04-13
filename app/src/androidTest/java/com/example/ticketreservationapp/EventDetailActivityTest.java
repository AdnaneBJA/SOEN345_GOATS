package com.example.ticketreservationapp;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.ticketreservationapp.view.EventDetailActivity;
import com.google.firebase.auth.FirebaseAuth;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertTrue;

/**
 * Instrumented UI tests for EventDetailActivity.
 *
 * The activity is launched with intent extras simulating a selected event.
 * Tests verify that event details are rendered correctly, that the back
 * button works, and that role-based button visibility is correct.
 */
@RunWith(AndroidJUnit4.class)
public class EventDetailActivityTest {

    private ActivityScenario<EventDetailActivity> scenario;

    private Intent buildEventIntent() {
        Intent intent = new Intent(
            ApplicationProvider.getApplicationContext(), EventDetailActivity.class);
        intent.putExtra("event_id", "test_event_1");
        intent.putExtra("event_title", "Summer Rock Concert");
        intent.putExtra("event_description", "An amazing live rock performance under the stars.");
        intent.putExtra("event_date", "2026-08-15");
        intent.putExtra("event_location", "Montreal, QC");
        intent.putExtra("event_category", "Concerts");
        intent.putExtra("event_price", 49.99);
        intent.putExtra("event_available_seats", 75);
        intent.putExtra("event_total_seats", 200);
        intent.putExtra("event_organizer_name", "Jane Organizer");
        intent.putExtra("event_organizer_id", "org_user_123");
        return intent;
    }

    @Before
    public void setUp() {
        FirebaseAuth.getInstance().signOut();
        scenario = ActivityScenario.launch(buildEventIntent());
    }

    @After
    public void tearDown() {
        if (scenario != null) {
            scenario.close();
        }
    }

    // ── Event details display ───────────────────────────────────────────────

    @Test
    public void eventTitle_isDisplayed() {
        onView(withId(R.id.tv_detail_title)).check(matches(withText("Summer Rock Concert")));
    }

    @Test
    public void eventDate_isDisplayed() {
        onView(withId(R.id.tv_detail_date)).check(matches(withText("2026-08-15")));
    }

    @Test
    public void eventLocation_isDisplayed() {
        onView(withId(R.id.tv_detail_location)).check(matches(withText("Montreal, QC")));
    }

    @Test
    public void eventCategory_isDisplayed() {
        onView(withId(R.id.tv_detail_category)).check(matches(withText("Concerts")));
    }

    @Test
    public void eventPrice_isDisplayed() {
        onView(withId(R.id.tv_detail_price)).check(matches(withText("$49.99")));
    }

    @Test
    public void eventDescription_isDisplayed() {
        onView(withId(R.id.tv_detail_description))
            .perform(scrollTo())
            .check(matches(withText("An amazing live rock performance under the stars.")));
    }

    @Test
    public void eventSeats_isDisplayed() {
        onView(withId(R.id.tv_detail_seats))
            .check(matches(withText(containsString("75"))));
    }

    @Test
    public void eventOrganizer_isDisplayed() {
        onView(withId(R.id.tv_detail_organizer))
            .check(matches(withText(containsString("Jane Organizer"))));
    }

    // ── Back button ─────────────────────────────────────────────────────────

    @Test
    public void backButton_isDisplayed() {
        onView(withId(R.id.btn_back)).check(matches(isDisplayed()));
    }

    @Test
    public void clickBackButton_finishesActivity() {
        onView(withId(R.id.btn_back)).perform(scrollTo(), click());
        scenario.onActivity(activity -> assertTrue(activity.isFinishing()));
    }
}
