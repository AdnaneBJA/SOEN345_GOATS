package com.example.ticketreservationapp;

import androidx.lifecycle.LiveData;

import com.example.ticketreservationapp.repository.AuthRepository;
import com.example.ticketreservationapp.viewmodel.LoginViewModel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(InstantTaskExecutorExtension.class)
class LoginViewModelTest {

    private static class FakeAuthRepository extends AuthRepository {
        String lastLoginEmail;
        String lastLoginPassword;
        String lastResetEmail;
        int loginCalls = 0;
        int resetCalls = 0;

        boolean shouldSucceed = true;
        String errorMessage = "boom";
        boolean invokeCallback = true;

        FakeAuthRepository() { super(null, null); }

        @Override
        public void loginWithEmail(String email, String password, AuthCallback callback) {
            loginCalls++;
            lastLoginEmail = email;
            lastLoginPassword = password;
            if (!invokeCallback) return;
            if (shouldSucceed) callback.onSuccess();
            else callback.onError(errorMessage);
        }

        @Override
        public void sendPasswordReset(String email, AuthCallback callback) {
            resetCalls++;
            lastResetEmail = email;
            if (!invokeCallback) return;
            if (shouldSucceed) callback.onSuccess();
            else callback.onError(errorMessage);
        }

        @Override
        public void registerWithEmail(String fullName, String email, String password,
                                      String phone, String role, AuthCallback callback) {}

        @Override
        public void signInWithPhoneCredential(com.google.firebase.auth.PhoneAuthCredential cred,
                                              String fullName, String phone, String role,
                                              String mode, AuthCallback callback) {}
    }

    private FakeAuthRepository fakeRepo;
    private LoginViewModel viewModel;

    @BeforeEach
    void setUp() {
        fakeRepo = new FakeAuthRepository();
        viewModel = new LoginViewModel(fakeRepo);
    }

    private <T> List<T> collectValues(LiveData<T> liveData) {
        List<T> values = new ArrayList<>();
        liveData.observeForever(values::add);
        return values;
    }

    // Validation: login ────────────────────────────────────────────────────

    @Test
    void login_emptyIdentifier_postsError() {
        List<String> errors = collectValues(viewModel.getErrorMessage());
        viewModel.login("", "password123");
        assertEquals("Email or phone is required", errors.get(0));
    }

    @Test
    void login_invalidEmailFormat_postsError() {
        List<String> errors = collectValues(viewModel.getErrorMessage());
        viewModel.login("notvalid@", "password123");
        assertEquals("Invalid email format", errors.get(0));
    }

    @Test
    void login_emptyPassword_postsError() {
        List<String> errors = collectValues(viewModel.getErrorMessage());
        viewModel.login("john@example.com", "");
        assertEquals("Password is required", errors.get(0));
    }

    @Test
    void login_emptyIdentifier_doesNotCallRepository() {
        viewModel.login("", "password123");
        assertEquals(0, fakeRepo.loginCalls);
    }

    @Test
    void login_invalidEmail_doesNotCallRepository() {
        viewModel.login("bad@", "password123");
        assertEquals(0, fakeRepo.loginCalls);
    }

    // Repository interaction ───────────────────────────────────────────────

    @Test
    void login_validEmail_callsRepositoryWithCorrectArgs() {
        viewModel.login("john@example.com", "password123");
        assertEquals(1, fakeRepo.loginCalls);
        assertEquals("john@example.com", fakeRepo.lastLoginEmail);
        assertEquals("password123", fakeRepo.lastLoginPassword);
    }

    @Test
    void login_validEmail_setsLoadingTrueBeforeCallback() {
        fakeRepo.invokeCallback = false;
        List<Boolean> loadingStates = collectValues(viewModel.getLoading());
        viewModel.login("john@example.com", "password123");
        assertTrue(loadingStates.contains(true), "Loading must be set to true before callback");
    }

    // Repository success ───────────────────────────────────────────────────

    @Test
    void login_repositorySuccess_postsLoginSuccessTrue() {
        viewModel.login("john@example.com", "password123");
        assertEquals(Boolean.TRUE, viewModel.getLoginSuccess().getValue());
    }

    @Test
    void login_repositorySuccess_setsLoadingFalse() {
        viewModel.login("john@example.com", "password123");
        assertEquals(Boolean.FALSE, viewModel.getLoading().getValue());
    }

    // Repository error ─────────────────────────────────────────────────────

    @Test
    void login_repositoryError_postsErrorMessage() {
        fakeRepo.shouldSucceed = false;
        fakeRepo.errorMessage = "Invalid credentials.";

        List<String> errors = collectValues(viewModel.getErrorMessage());
        viewModel.login("john@example.com", "wrongpassword");

        assertTrue(errors.contains("Invalid credentials."));
    }

    @Test
    void login_repositoryError_setsLoadingFalse() {
        fakeRepo.shouldSucceed = false;
        fakeRepo.errorMessage = "Error";

        viewModel.login("john@example.com", "password123");

        assertEquals(Boolean.FALSE, viewModel.getLoading().getValue());
    }

    // sendPasswordReset ────────────────────────────────────────────────────

    @Test
    void sendPasswordReset_emptyEmail_postsError() {
        List<String> errors = collectValues(viewModel.getErrorMessage());
        viewModel.sendPasswordReset("");
        assertFalse(errors.isEmpty(), "An error should be posted for empty email");
    }

    @Test
    void sendPasswordReset_invalidEmail_postsError() {
        List<String> errors = collectValues(viewModel.getErrorMessage());
        viewModel.sendPasswordReset("notanemail");
        assertFalse(errors.isEmpty(), "An error should be posted for invalid email");
    }

    @Test
    void sendPasswordReset_emptyEmail_doesNotCallRepository() {
        viewModel.sendPasswordReset("");
        assertEquals(0, fakeRepo.resetCalls);
    }

    @Test
    void sendPasswordReset_validEmail_callsRepository() {
        viewModel.sendPasswordReset("john@example.com");
        assertEquals(1, fakeRepo.resetCalls);
        assertEquals("john@example.com", fakeRepo.lastResetEmail);
    }

    @Test
    void sendPasswordReset_success_postsResetEmailSent() {
        viewModel.sendPasswordReset("john@example.com");
        assertEquals("john@example.com", viewModel.getResetEmailSent().getValue());
    }

    @Test
    void sendPasswordReset_error_postsErrorMessage() {
        fakeRepo.shouldSucceed = false;
        fakeRepo.errorMessage = "Reset failed.";

        List<String> errors = collectValues(viewModel.getErrorMessage());
        viewModel.sendPasswordReset("john@example.com");

        assertTrue(errors.contains("Reset failed."));
    }
}
