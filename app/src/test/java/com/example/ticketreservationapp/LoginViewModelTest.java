package com.example.ticketreservationapp;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;

import com.example.ticketreservationapp.repository.AuthRepository;
import com.example.ticketreservationapp.viewmodel.LoginViewModel;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


public class LoginViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private AuthRepository mockRepo;

    private LoginViewModel viewModel;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        viewModel = new LoginViewModel(mockRepo);
    }

    private <T> List<T> collectValues(LiveData<T> liveData) {
        List<T> values = new ArrayList<>();
        liveData.observeForever(values::add);
        return values;
    }

    // Validation: login ────────────────────────────────────────────────────

    @Test
    public void login_emptyIdentifier_postsError() {
        List<String> errors = collectValues(viewModel.getErrorMessage());
        viewModel.login("", "password123");
        assertEquals("Email or phone is required", errors.get(0));
    }

    @Test
    public void login_invalidEmailFormat_postsError() {
        List<String> errors = collectValues(viewModel.getErrorMessage());
        viewModel.login("notvalid@", "password123");
        assertEquals("Invalid email format", errors.get(0));
    }

    @Test
    public void login_emptyPassword_postsError() {
        List<String> errors = collectValues(viewModel.getErrorMessage());
        viewModel.login("john@example.com", "");
        assertEquals("Password is required", errors.get(0));
    }

    @Test
    public void login_emptyIdentifier_doesNotCallRepository() {
        viewModel.login("", "password123");
        verifyNoInteractions(mockRepo);
    }

    @Test
    public void login_invalidEmail_doesNotCallRepository() {
        viewModel.login("bad@", "password123");
        verifyNoInteractions(mockRepo);
    }

    // Repository interaction ───────────────────────────────────────────────

    @Test
    public void login_validEmail_callsRepositoryWithCorrectArgs() {
        viewModel.login("john@example.com", "password123");
        verify(mockRepo).loginWithEmail(
            eq("john@example.com"), eq("password123"), any(AuthRepository.AuthCallback.class)
        );
    }

    @Test
    public void login_validEmail_setsLoadingTrueBeforeCallback() {
        doAnswer(inv -> null).when(mockRepo).loginWithEmail(any(), any(), any());

        List<Boolean> loadingStates = collectValues(viewModel.getLoading());
        viewModel.login("john@example.com", "password123");

        assertTrue("Loading must be set to true before callback", loadingStates.contains(true));
    }

    // Repository success ───────────────────────────────────────────────────

    @Test
    public void login_repositorySuccess_postsLoginSuccessTrue() {
        doAnswer(inv -> {
            ((AuthRepository.AuthCallback) inv.getArgument(2)).onSuccess();
            return null;
        }).when(mockRepo).loginWithEmail(any(), any(), any());

        viewModel.login("john@example.com", "password123");

        assertEquals(Boolean.TRUE, viewModel.getLoginSuccess().getValue());
    }

    @Test
    public void login_repositorySuccess_setsLoadingFalse() {
        doAnswer(inv -> {
            ((AuthRepository.AuthCallback) inv.getArgument(2)).onSuccess();
            return null;
        }).when(mockRepo).loginWithEmail(any(), any(), any());

        viewModel.login("john@example.com", "password123");

        assertEquals(Boolean.FALSE, viewModel.getLoading().getValue());
    }

    // Repository error ─────────────────────────────────────────────────────

    @Test
    public void login_repositoryError_postsErrorMessage() {
        doAnswer(inv -> {
            ((AuthRepository.AuthCallback) inv.getArgument(2)).onError("Invalid credentials.");
            return null;
        }).when(mockRepo).loginWithEmail(any(), any(), any());

        List<String> errors = collectValues(viewModel.getErrorMessage());
        viewModel.login("john@example.com", "wrongpassword");

        assertTrue(errors.contains("Invalid credentials."));
    }

    @Test
    public void login_repositoryError_setsLoadingFalse() {
        doAnswer(inv -> {
            ((AuthRepository.AuthCallback) inv.getArgument(2)).onError("Error");
            return null;
        }).when(mockRepo).loginWithEmail(any(), any(), any());

        viewModel.login("john@example.com", "password123");

        assertEquals(Boolean.FALSE, viewModel.getLoading().getValue());
    }

    // sendPasswordReset ────────────────────────────────────────────────────

    @Test
    public void sendPasswordReset_emptyEmail_postsError() {
        List<String> errors = collectValues(viewModel.getErrorMessage());
        viewModel.sendPasswordReset("");
        assertFalse("An error should be posted for empty email", errors.isEmpty());
    }

    @Test
    public void sendPasswordReset_invalidEmail_postsError() {
        List<String> errors = collectValues(viewModel.getErrorMessage());
        viewModel.sendPasswordReset("notanemail");
        assertFalse("An error should be posted for invalid email", errors.isEmpty());
    }

    @Test
    public void sendPasswordReset_emptyEmail_doesNotCallRepository() {
        viewModel.sendPasswordReset("");
        verifyNoInteractions(mockRepo);
    }

    @Test
    public void sendPasswordReset_validEmail_callsRepository() {
        viewModel.sendPasswordReset("john@example.com");
        verify(mockRepo).sendPasswordReset(
            eq("john@example.com"), any(AuthRepository.AuthCallback.class)
        );
    }

    @Test
    public void sendPasswordReset_success_postsResetEmailSent() {
        doAnswer(inv -> {
            ((AuthRepository.AuthCallback) inv.getArgument(1)).onSuccess();
            return null;
        }).when(mockRepo).sendPasswordReset(any(), any());

        viewModel.sendPasswordReset("john@example.com");

        assertEquals("john@example.com", viewModel.getResetEmailSent().getValue());
    }

    @Test
    public void sendPasswordReset_error_postsErrorMessage() {
        doAnswer(inv -> {
            ((AuthRepository.AuthCallback) inv.getArgument(1)).onError("Reset failed.");
            return null;
        }).when(mockRepo).sendPasswordReset(any(), any());

        List<String> errors = collectValues(viewModel.getErrorMessage());
        viewModel.sendPasswordReset("john@example.com");

        assertTrue(errors.contains("Reset failed."));
    }
}
