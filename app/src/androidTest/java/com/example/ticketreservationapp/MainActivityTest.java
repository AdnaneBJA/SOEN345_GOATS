package com.example.ticketreservationapp;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.ticketreservationapp.view.LoginActivity;
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
import static org.junit.Assert.assertNull;

/**
 * Instrumented UI tests for MainActivity (home screen).
 *
 * MainActivity is not exported, so it is launched via an explicit Intent.
 * Tests run with no signed-in user — the welcome text is empty but the
 * sign-out button is always visible.
 *
 * Sign-out navigation is verified with Espresso-Intents.
 */
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    private ActivityScenario<MainActivity> scenario;

    @Before
    public void setUp() {
        // Ensure clean state — no active Firebase session
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
    public void mainScreen_signOutButtonIsDisplayed() {
        onView(withId(R.id.btn_sign_out)).check(matches(isDisplayed()));
    }

    @Test
    public void mainScreen_welcomeTextViewIsDisplayed() {
        onView(withId(R.id.tv_welcome)).check(matches(isDisplayed()));
    }

    // ── Sign-out flow ────────────────────────────────────────────────────────

    @Test
    public void clickSignOut_navigatesToLoginActivity() {
        Intents.init();
        try {
            onView(withId(R.id.btn_sign_out)).perform(click());
            intended(hasComponent(LoginActivity.class.getName()));
        } finally {
            Intents.release();
        }
    }

    @Test
    public void clickSignOut_clearsFirebaseSession() {
        onView(withId(R.id.btn_sign_out)).perform(click());
        // After sign-out, getCurrentUser() must return null
        assertNull(FirebaseAuth.getInstance().getCurrentUser());
    }
}
