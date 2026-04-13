package com.example.ticketreservationapp;

import androidx.lifecycle.LiveData;

import com.example.ticketreservationapp.repository.AuthRepository;
import com.example.ticketreservationapp.viewmodel.OtpViewModel;
import com.google.firebase.auth.PhoneAuthCredential;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(InstantTaskExecutorExtension.class)
class OtpViewModelTest {

    private static class FakeAuthRepository extends AuthRepository {
        PhoneAuthCredential lastCredential;
        String lastFullName;
        String lastPhone;
        String lastRole;
        String lastMode;
        int signInCalls = 0;

        boolean shouldSucceed = true;
        String errorMessage = "boom";
        boolean invokeCallback = true;

        FakeAuthRepository() { super(null, null); }

        @Override
        public void signInWithPhoneCredential(PhoneAuthCredential credential,
                                              String fullName, String phone, String role,
                                              String mode, AuthCallback callback) {
            signInCalls++;
            lastCredential = credential;
            lastFullName = fullName;
            lastPhone = phone;
            lastRole = role;
            lastMode = mode;
            if (!invokeCallback) return;
            if (shouldSucceed) callback.onSuccess();
            else callback.onError(errorMessage);
        }

        @Override
        public void loginWithEmail(String email, String password, AuthCallback callback) {}
        @Override
        public void registerWithEmail(String fullName, String email, String password,
                                      String phone, String role, AuthCallback callback) {}
        @Override
        public void sendPasswordReset(String email, AuthCallback callback) {}
    }

    private FakeAuthRepository fakeRepo;
    private OtpViewModel viewModel;

    @BeforeEach
    void setUp() {
        fakeRepo = new FakeAuthRepository();
        viewModel = new OtpViewModel(fakeRepo);
    }

    private <T> List<T> collectValues(LiveData<T> liveData) {
        List<T> values = new ArrayList<>();
        liveData.observeForever(values::add);
        return values;
    }

    // ── verifyOtp: validation ───────────────────────────────────────────────

    @Test
    void verifyOtp_nullCode_postsError() {
        List<String> errors = collectValues(viewModel.getErrorMessage());
        viewModel.verifyOtp("verif-id", null, "John", "+15551234567", "customer", "register");
        assertEquals("Please enter the 6-digit code", errors.get(0));
    }

    @Test
    void verifyOtp_emptyCode_postsError() {
        List<String> errors = collectValues(viewModel.getErrorMessage());
        viewModel.verifyOtp("verif-id", "", "John", "+15551234567", "customer", "register");
        assertEquals("Please enter the 6-digit code", errors.get(0));
    }

    @Test
    void verifyOtp_fiveDigitCode_postsError() {
        List<String> errors = collectValues(viewModel.getErrorMessage());
        viewModel.verifyOtp("verif-id", "12345", "John", "+15551234567", "customer", "register");
        assertEquals("Please enter the 6-digit code", errors.get(0));
    }

    @Test
    void verifyOtp_sevenDigitCode_postsError() {
        List<String> errors = collectValues(viewModel.getErrorMessage());
        viewModel.verifyOtp("verif-id", "1234567", "John", "+15551234567", "customer", "register");
        assertEquals("Please enter the 6-digit code", errors.get(0));
    }

    @Test
    void verifyOtp_invalidCode_doesNotCallRepository() {
        viewModel.verifyOtp("verif-id", "123", "John", "+15551234567", "customer", "register");
        assertEquals(0, fakeRepo.signInCalls);
    }

    // ── signInWithCredential: repository interaction ─────────────────────────

    @Test
    void signInWithCredential_callsRepositoryWithCorrectArgs() {
        viewModel.signInWithCredential(null, "John", "+15551234567", "customer", "register");
        assertEquals(1, fakeRepo.signInCalls);
        assertNull(fakeRepo.lastCredential);
        assertEquals("John", fakeRepo.lastFullName);
        assertEquals("+15551234567", fakeRepo.lastPhone);
        assertEquals("customer", fakeRepo.lastRole);
        assertEquals("register", fakeRepo.lastMode);
    }

    @Test
    void signInWithCredential_setsLoadingTrue_beforeCallback() {
        fakeRepo.invokeCallback = false;
        List<Boolean> loadingStates = collectValues(viewModel.getLoading());
        viewModel.signInWithCredential(null, "John", "+15551234567", "customer", "register");
        assertTrue(loadingStates.contains(true), "Loading must be true before callback fires");
    }

    @Test
    void signInWithCredential_success_postsVerificationSuccessTrue() {
        viewModel.signInWithCredential(null, "John", "+15551234567", "customer", "register");
        assertEquals(Boolean.TRUE, viewModel.getVerificationSuccess().getValue());
    }

    @Test
    void signInWithCredential_success_setsLoadingFalse() {
        viewModel.signInWithCredential(null, "John", "+15551234567", "customer", "register");
        assertEquals(Boolean.FALSE, viewModel.getLoading().getValue());
    }

    @Test
    void signInWithCredential_error_postsErrorMessage() {
        fakeRepo.shouldSucceed = false;
        fakeRepo.errorMessage = "Invalid code.";

        List<String> errors = collectValues(viewModel.getErrorMessage());
        viewModel.signInWithCredential(null, "John", "+15551234567", "customer", "register");

        assertTrue(errors.contains("Invalid code."));
    }

    @Test
    void signInWithCredential_error_setsLoadingFalse() {
        fakeRepo.shouldSucceed = false;
        fakeRepo.errorMessage = "Error";

        viewModel.signInWithCredential(null, "John", "+15551234567", "customer", "register");

        assertEquals(Boolean.FALSE, viewModel.getLoading().getValue());
    }

    @Test
    void signInWithCredential_loginMode_passedToRepository() {
        viewModel.signInWithCredential(null, null, "+15551234567", null, "login");
        assertEquals("login", fakeRepo.lastMode);
        assertNull(fakeRepo.lastFullName);
        assertNull(fakeRepo.lastRole);
    }
}
