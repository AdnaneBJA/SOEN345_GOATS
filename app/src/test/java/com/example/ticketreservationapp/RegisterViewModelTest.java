package com.example.ticketreservationapp;

import androidx.lifecycle.LiveData;

import com.example.ticketreservationapp.repository.AuthRepository;
import com.example.ticketreservationapp.viewmodel.RegisterViewModel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(InstantTaskExecutorExtension.class)
class RegisterViewModelTest {

    private static class FakeAuthRepository extends AuthRepository {
        String lastFullName;
        String lastEmail;
        String lastPassword;
        String lastPhone;
        String lastRole;
        int registerCalls = 0;

        boolean shouldSucceed = true;
        String errorMessage = "boom";
        boolean invokeCallback = true;

        FakeAuthRepository() { super(null, null); }

        @Override
        public void registerWithEmail(String fullName, String email, String password,
                                      String phone, String role, AuthCallback callback) {
            registerCalls++;
            lastFullName = fullName;
            lastEmail = email;
            lastPassword = password;
            lastPhone = phone;
            lastRole = role;
            if (!invokeCallback) return;
            if (shouldSucceed) callback.onSuccess();
            else callback.onError(errorMessage);
        }

        @Override
        public void loginWithEmail(String email, String password, AuthCallback callback) {}
        @Override
        public void signInWithPhoneCredential(com.google.firebase.auth.PhoneAuthCredential cred,
                                              String fullName, String phone, String role,
                                              String mode, AuthCallback callback) {}
        @Override
        public void sendPasswordReset(String email, AuthCallback callback) {}
    }

    private FakeAuthRepository fakeRepo;
    private RegisterViewModel viewModel;

    @BeforeEach
    void setUp() {
        fakeRepo = new FakeAuthRepository();
        viewModel = new RegisterViewModel(fakeRepo);
    }

    private <T> List<T> collectValues(LiveData<T> liveData) {
        List<T> values = new ArrayList<>();
        liveData.observeForever(values::add);
        return values;
    }

    // Validation: full name ───────────────────────────────────────────────

    @Test
    void register_emptyFullName_postsError() {
        List<String> errors = collectValues(viewModel.getErrorMessage());
        viewModel.register("", "john@example.com", "password123", "password123", "customer");
        assertEquals("Full name is required", errors.get(0));
    }

    @Test
    void register_emptyFullName_doesNotCallRepository() {
        viewModel.register("", "john@example.com", "password123", "password123", "customer");
        assertEquals(0, fakeRepo.registerCalls);
    }

    // Validation: email/identifier ────────────────────────────────────────

    @Test
    void register_emptyIdentifier_postsError() {
        List<String> errors = collectValues(viewModel.getErrorMessage());
        viewModel.register("John Doe", "", "password123", "password123", "customer");
        assertEquals("Email or phone number is required", errors.get(0));
    }

    @Test
    void register_invalidEmailFormat_postsError() {
        List<String> errors = collectValues(viewModel.getErrorMessage());
        viewModel.register("John Doe", "notvalid@", "password123", "password123", "customer");
        assertEquals("Invalid email format", errors.get(0));
    }

    @Test
    void register_invalidEmail_doesNotCallRepository() {
        viewModel.register("John Doe", "notvalid@", "password123", "password123", "customer");
        assertEquals(0, fakeRepo.registerCalls);
    }

    // Validation: password ────────────────────────────────────────────────

    @Test
    void register_emptyPassword_postsError() {
        List<String> errors = collectValues(viewModel.getErrorMessage());
        viewModel.register("John Doe", "john@example.com", "", "", "customer");
        assertEquals("Password is required", errors.get(0));
    }

    @Test
    void register_shortPassword_postsError() {
        List<String> errors = collectValues(viewModel.getErrorMessage());
        viewModel.register("John Doe", "john@example.com", "abc", "abc", "customer");
        assertEquals("Password must be at least 6 characters", errors.get(0));
    }

    @Test
    void register_passwordMismatch_postsError() {
        List<String> errors = collectValues(viewModel.getErrorMessage());
        viewModel.register("John Doe", "john@example.com", "password123", "different", "customer");
        assertEquals("Passwords do not match", errors.get(0));
    }

    @Test
    void register_passwordMismatch_doesNotCallRepository() {
        viewModel.register("John Doe", "john@example.com", "password123", "different", "customer");
        assertEquals(0, fakeRepo.registerCalls);
    }

    // Valid input ─────────────────────────────────────────────────────────

    @Test
    void register_validInput_callsRepository() {
        viewModel.register("John Doe", "john@example.com", "password123", "password123", "customer");
        assertEquals(1, fakeRepo.registerCalls);
        assertEquals("John Doe", fakeRepo.lastFullName);
        assertEquals("john@example.com", fakeRepo.lastEmail);
        assertEquals("password123", fakeRepo.lastPassword);
        assertEquals("", fakeRepo.lastPhone);
        assertEquals("customer", fakeRepo.lastRole);
    }

    @Test
    void register_validInput_setsLoadingTrue() {
        fakeRepo.invokeCallback = false;
        List<Boolean> loadingStates = collectValues(viewModel.getLoading());
        viewModel.register("John Doe", "john@example.com", "password123", "password123", "customer");
        assertTrue(loadingStates.contains(true));
    }

    // Repository success ──────────────────────────────────────────────────

    @Test
    void register_repositorySuccess_postsRegistrationSuccessTrue() {
        viewModel.register("John Doe", "john@example.com", "password123", "password123", "customer");
        assertEquals(Boolean.TRUE, viewModel.getRegistrationSuccess().getValue());
    }

    @Test
    void register_repositorySuccess_setsLoadingFalse() {
        viewModel.register("John Doe", "john@example.com", "password123", "password123", "customer");
        assertEquals(Boolean.FALSE, viewModel.getLoading().getValue());
    }

    // Repository error ────────────────────────────────────────────────────

    @Test
    void register_repositoryError_postsErrorMessage() {
        fakeRepo.shouldSucceed = false;
        fakeRepo.errorMessage = "Email already in use.";

        List<String> errors = collectValues(viewModel.getErrorMessage());
        viewModel.register("John Doe", "john@example.com", "password123", "password123", "customer");

        assertTrue(errors.contains("Email already in use."));
    }

    @Test
    void register_repositoryError_setsLoadingFalse() {
        fakeRepo.shouldSucceed = false;
        fakeRepo.errorMessage = "Error";

        viewModel.register("John Doe", "john@example.com", "password123", "password123", "customer");

        assertEquals(Boolean.FALSE, viewModel.getLoading().getValue());
    }

    // Role handling ───────────────────────────────────────────────────────

    @Test
    void register_organizerRole_passesRoleToRepository() {
        viewModel.register("Jane Org", "jane@org.com", "password123", "password123", "organizer");
        assertEquals("organizer", fakeRepo.lastRole);
    }
}
