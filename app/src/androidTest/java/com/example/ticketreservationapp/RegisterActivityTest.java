package com.example.ticketreservationapp;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.ticketreservationapp.view.RegisterActivity;
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
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertTrue;


@RunWith(AndroidJUnit4.class)
public class RegisterActivityTest {

    private ActivityScenario<RegisterActivity> scenario;

    @Before
    public void setUp() {
        FirebaseAuth.getInstance().signOut();
        scenario = ActivityScenario.launch(RegisterActivity.class);
    }

    @After
    public void tearDown() {
        if (scenario != null) {
            scenario.close();
        }
    }

    // ── Screen elements ──────────────────────────────────────────────────────

    @Test
    public void registerScreen_titleIsDisplayed() {
        onView(withText("Create Your Account")).check(matches(isDisplayed()));
    }

    @Test
    public void fullNameField_isDisplayed() {
        onView(withId(R.id.til_full_name)).check(matches(isDisplayed()));
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
    public void confirmPasswordField_isDisplayed() {
        onView(withId(R.id.til_confirm_password)).check(matches(isDisplayed()));
    }

    @Test
    public void roleToggle_isDisplayed() {
        onView(withId(R.id.toggle_role)).check(matches(isDisplayed()));
    }

    @Test
    public void registerButton_isDisplayed() {
        onView(withId(R.id.btn_register)).check(matches(isDisplayed()));
    }

    @Test
    public void loginLink_isDisplayed() {
        onView(withId(R.id.tv_login_link)).check(matches(isDisplayed()));
    }

    @Test
    public void roleToggle_defaultIsCustomer() {
        onView(withId(R.id.btn_customer)).check(matches(isChecked()));
    }

    // ── Input validation errors (email path) ────────────────────────────────

    @Test
    public void register_withEmptyName_showsError() {
        onView(withId(R.id.et_identifier))
            .perform(typeText("john@example.com"), closeSoftKeyboard());
        onView(withId(R.id.et_password))
            .perform(typeText("password123"), closeSoftKeyboard());
        onView(withId(R.id.et_confirm_password))
            .perform(typeText("password123"), closeSoftKeyboard());
        onView(withId(R.id.btn_register)).perform(scrollTo(), click());
        onView(withText("Full name is required")).check(matches(isDisplayed()));
    }

    @Test
    public void register_withEmptyIdentifier_showsError() {
        onView(withId(R.id.et_full_name))
            .perform(typeText("John Doe"), closeSoftKeyboard());
        onView(withId(R.id.et_password))
            .perform(typeText("password123"), closeSoftKeyboard());
        onView(withId(R.id.et_confirm_password))
            .perform(typeText("password123"), closeSoftKeyboard());
        onView(withId(R.id.btn_register)).perform(scrollTo(), click());
        onView(withText("Email or phone number is required")).check(matches(isDisplayed()));
    }

    @Test
    public void register_withInvalidEmail_showsError() {
        onView(withId(R.id.et_full_name))
            .perform(typeText("John Doe"), closeSoftKeyboard());
        onView(withId(R.id.et_identifier))
            .perform(typeText("invalidemail@"), closeSoftKeyboard());
        onView(withId(R.id.et_password))
            .perform(typeText("password123"), closeSoftKeyboard());
        onView(withId(R.id.et_confirm_password))
            .perform(typeText("password123"), closeSoftKeyboard());
        onView(withId(R.id.btn_register)).perform(scrollTo(), click());
        onView(withText("Invalid email format")).check(matches(isDisplayed()));
    }

    @Test
    public void register_withShortPassword_showsError() {
        onView(withId(R.id.et_full_name))
            .perform(typeText("John Doe"), closeSoftKeyboard());
        onView(withId(R.id.et_identifier))
            .perform(typeText("john@example.com"), closeSoftKeyboard());
        onView(withId(R.id.et_password))
            .perform(typeText("abc"), closeSoftKeyboard());
        onView(withId(R.id.et_confirm_password))
            .perform(typeText("abc"), closeSoftKeyboard());
        onView(withId(R.id.btn_register)).perform(scrollTo(), click());
        onView(withText("Password must be at least 6 characters")).check(matches(isDisplayed()));
    }

    @Test
    public void register_withMismatchedPasswords_showsError() {
        onView(withId(R.id.et_full_name))
            .perform(typeText("John Doe"), closeSoftKeyboard());
        onView(withId(R.id.et_identifier))
            .perform(typeText("john@example.com"), closeSoftKeyboard());
        onView(withId(R.id.et_password))
            .perform(typeText("password123"), closeSoftKeyboard());
        onView(withId(R.id.et_confirm_password))
            .perform(typeText("different456"), closeSoftKeyboard());
        onView(withId(R.id.btn_register)).perform(scrollTo(), click());
        onView(withText("Passwords do not match")).check(matches(isDisplayed()));
    }

    // ── Phone validation errors ──────────────────────────────────────────────

    @Test
    public void register_phoneWithoutCountryCode_showsError() {
        onView(withId(R.id.et_full_name))
            .perform(typeText("John Doe"), closeSoftKeyboard());
        onView(withId(R.id.et_identifier))
            .perform(typeText("4381234567"), closeSoftKeyboard());
        onView(withId(R.id.btn_register)).perform(scrollTo(), click());
        onView(withText("Include country code (e.g. +14381234567)"))
            .check(matches(isDisplayed()));
    }

    // ── Navigation ───────────────────────────────────────────────────────────

    @Test
    public void clickLoginLink_finishesActivity() {
        onView(withId(R.id.tv_login_link)).perform(click());
        // Activity should finish (back-pressed) — verified by checking the scenario state
        scenario.onActivity(activity -> assertTrue(activity.isFinishing()));
    }
}
