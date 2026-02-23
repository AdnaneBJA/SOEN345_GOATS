package com.example.ticketreservationapp;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.ticketreservationapp.view.LoginActivity;
import com.example.ticketreservationapp.view.RegisterActivity;
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
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

/**
 * Instrumented UI tests for LoginActivity.
 *
 * All tests sign out first so LoginActivity always shows the login form
 * rather than redirecting to MainActivity.
 *
 * Tests that exercise actual Firebase sign-in are NOT included here — those
 * require either a live network connection or the Firebase Auth emulator.
 */
@RunWith(AndroidJUnit4.class)
public class LoginActivityTest {

    private ActivityScenario<LoginActivity> scenario;

    @Before
    public void setUp() {
        // Ensure no active session so LoginActivity shows the form
        FirebaseAuth.getInstance().signOut();
        scenario = ActivityScenario.launch(LoginActivity.class);
    }

    @After
    public void tearDown() {
        if (scenario != null) {
            scenario.close();
        }
    }

    // ── Screen elements ──────────────────────────────────────────────────────

    @Test
    public void loginScreen_titleIsDisplayed() {
        onView(withText("Welcome Back")).check(matches(isDisplayed()));
    }

    @Test
    public void identifierField_isDisplayed() {
        onView(withId(R.id.til_identifier)).check(matches(isDisplayed()));
    }

    @Test
    public void passwordField_isDisplayed() {
        onView(withId(R.id.til_password)).check(matches(isDisplayed()));
    }

    @Test
    public void loginButton_isDisplayed() {
        onView(withId(R.id.btn_login)).check(matches(isDisplayed()));
    }

    @Test
    public void createAccountButton_isDisplayed() {
        onView(withId(R.id.btn_create_account)).check(matches(isDisplayed()));
    }

    @Test
    public void forgotPasswordLink_isDisplayed() {
        onView(withId(R.id.tv_forgot_password)).check(matches(isDisplayed()));
    }

    // ── Input validation errors ──────────────────────────────────────────────

    @Test
    public void login_withEmptyFields_showsRequiredError() {
        onView(withId(R.id.btn_login)).perform(click());
        onView(withText("Email or phone is required")).check(matches(isDisplayed()));
    }

    @Test
    public void login_withInvalidEmailFormat_showsFormatError() {
        onView(withId(R.id.et_identifier))
            .perform(typeText("invalidemail@"), closeSoftKeyboard());
        onView(withId(R.id.et_password))
            .perform(typeText("password123"), closeSoftKeyboard());
        onView(withId(R.id.btn_login)).perform(click());
        onView(withText("Invalid email format")).check(matches(isDisplayed()));
    }

    @Test
    public void login_withValidEmailButNoPassword_showsPasswordError() {
        onView(withId(R.id.et_identifier))
            .perform(typeText("john@example.com"), closeSoftKeyboard());
        onView(withId(R.id.btn_login)).perform(click());
        onView(withText("Password is required")).check(matches(isDisplayed()));
    }

    @Test
    public void login_phoneWithoutCountryCode_showsCountryCodeError() {
        onView(withId(R.id.et_identifier))
            .perform(typeText("4381234567"), closeSoftKeyboard());
        onView(withId(R.id.btn_login)).perform(click());
        onView(withText("Include country code (e.g. +14381234567)"))
            .check(matches(isDisplayed()));
    }

    @Test
    public void forgotPassword_withEmptyField_showsError() {
        // Identifier field is empty → sendPasswordReset should show an error
        onView(withId(R.id.tv_forgot_password)).perform(click());
        onView(withText("Enter your email address in the field above first"))
            .check(matches(isDisplayed()));
    }

    // ── Navigation ───────────────────────────────────────────────────────────

    @Test
    public void clickCreateAccount_opensRegisterActivity() {
        Intents.init();
        try {
            onView(withId(R.id.btn_create_account)).perform(click());
            intended(hasComponent(RegisterActivity.class.getName()));
        } finally {
            Intents.release();
        }
    }
}
