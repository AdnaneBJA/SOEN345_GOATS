package com.example.ticketreservationapp;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.ticketreservationapp.view.EventListActivity;
import com.google.firebase.auth.FirebaseAuth;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

/**
 * Instrumented UI tests for EventListActivity (Browse Events screen).
 *
 * Tests verify that all UI elements are displayed correctly and that
 * search and filter controls are functional. These tests run against
 * live Firestore, so results depend on the current data — element
 * visibility tests are independent of data.
 */
@RunWith(AndroidJUnit4.class)
public class EventListActivityTest {

    private ActivityScenario<EventListActivity> scenario;

    @Before
    public void setUp() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(
            ApplicationProvider.getApplicationContext(), EventListActivity.class);
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
    public void eventListScreen_titleIsDisplayed() {
        onView(withText("Discover Events")).check(matches(isDisplayed()));
    }

    @Test
    public void eventListScreen_subtitleIsDisplayed() {
        onView(withText("Browse movies, concerts, travel & sports"))
            .check(matches(isDisplayed()));
    }

    @Test
    public void searchField_isDisplayed() {
        onView(withId(R.id.et_search)).check(matches(isDisplayed()));
    }

    @Test
    public void categoryDropdown_isDisplayed() {
        onView(withId(R.id.spinner_category)).check(matches(isDisplayed()));
    }

    @Test
    public void dateFilterChip_isDisplayed() {
        onView(withId(R.id.chip_date)).check(matches(isDisplayed()));
    }

    @Test
    public void clearFiltersChip_isDisplayed() {
        onView(withId(R.id.chip_clear_filters)).check(matches(isDisplayed()));
    }

    @Test
    public void backButton_isDisplayed() {
        onView(withId(R.id.btn_back)).check(matches(isDisplayed()));
    }

    @Test
    public void eventRecyclerView_isDisplayed() {
        onView(withId(R.id.rv_events)).check(matches(isDisplayed()));
    }

    // ── Search interaction ──────────────────────────────────────────────────

    @Test
    public void searchField_acceptsTextInput() {
        onView(withId(R.id.et_search))
            .perform(typeText("Concert"), closeSoftKeyboard());
        onView(withId(R.id.et_search)).check(matches(withText("Concert")));
    }

    @Test
    public void clearFilters_clearsSearchField() {
        onView(withId(R.id.et_search))
            .perform(typeText("Rock"), closeSoftKeyboard());
        onView(withId(R.id.chip_clear_filters)).perform(click());
        onView(withId(R.id.et_search)).check(matches(withText("")));
    }

    // ── Navigation ──────────────────────────────────────────────────────────

    @Test
    public void clickBackButton_finishesActivity() {
        onView(withId(R.id.btn_back)).perform(click());
        scenario.onActivity(activity ->
            assertTrue(activity.isFinishing()));
    }

    private void assertTrue(boolean condition) {
        org.junit.Assert.assertTrue(condition);
    }
}
