package com.example.ticketreservationapp;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.ticketreservationapp.view.OtpActivity;
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
 * Instrumented UI tests for OtpActivity (phone OTP verification screen).
 *
 * OtpActivity requires intent extras to function, so we launch it with
 * a minimal intent. Tests verify UI element visibility and OTP validation.
 */
@RunWith(AndroidJUnit4.class)
public class OtpActivityTest {

    private ActivityScenario<OtpActivity> scenario;

    @Before
    public void setUp() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), OtpActivity.class);
        intent.putExtra("phone", "+15551234567");
        intent.putExtra("mode", "register");
        intent.putExtra("fullName", "John Doe");
        intent.putExtra("role", "customer");
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
    public void otpScreen_titleIsDisplayed() {
        onView(withText("Verify Your Number")).check(matches(isDisplayed()));
    }

    @Test
    public void otpField_isDisplayed() {
        onView(withId(R.id.til_otp)).check(matches(isDisplayed()));
    }

    @Test
    public void verifyButton_isDisplayed() {
        onView(withId(R.id.btn_verify)).check(matches(isDisplayed()));
    }

    @Test
    public void resendLink_isDisplayed() {
        onView(withId(R.id.tv_resend)).check(matches(isDisplayed()));
    }

    // ── OTP validation ──────────────────────────────────────────────────────

    @Test
    public void verify_withEmptyOtp_showsError() {
        onView(withId(R.id.btn_verify)).perform(click());
        onView(withText("Please enter the 6-digit code")).check(matches(isDisplayed()));
    }

    @Test
    public void verify_withShortOtp_showsError() {
        onView(withId(R.id.et_otp))
            .perform(typeText("123"), closeSoftKeyboard());
        onView(withId(R.id.btn_verify)).perform(click());
        onView(withText("Please enter the 6-digit code")).check(matches(isDisplayed()));
    }

    @Test
    public void verify_withFiveDigitOtp_showsError() {
        onView(withId(R.id.et_otp))
            .perform(typeText("12345"), closeSoftKeyboard());
        onView(withId(R.id.btn_verify)).perform(click());
        onView(withText("Please enter the 6-digit code")).check(matches(isDisplayed()));
    }
}
