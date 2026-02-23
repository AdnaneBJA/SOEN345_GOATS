package com.example.ticketreservationapp;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;

import com.example.ticketreservationapp.repository.AuthRepository;
import com.example.ticketreservationapp.viewmodel.OtpViewModel;

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

public class OtpViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private AuthRepository mockRepo;

    private OtpViewModel viewModel;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        viewModel = new OtpViewModel(mockRepo);
    }

    private <T> List<T> collectValues(LiveData<T> liveData) {
        List<T> values = new ArrayList<>();
        liveData.observeForever(values::add);
        return values;
    }

    // ── verifyOtp: validation (does NOT call Firebase) ───────────────────────

    @Test
    public void verifyOtp_nullCode_postsError() {
        List<String> errors = collectValues(viewModel.getErrorMessage());
        viewModel.verifyOtp("verif-id", null, "John", "+15551234567", "customer", "register");
        assertEquals("Please enter the 6-digit code", errors.get(0));
    }

    @Test
    public void verifyOtp_emptyCode_postsError() {
        List<String> errors = collectValues(viewModel.getErrorMessage());
        viewModel.verifyOtp("verif-id", "", "John", "+15551234567", "customer", "register");
        assertEquals("Please enter the 6-digit code", errors.get(0));
    }

    @Test
    public void verifyOtp_fiveDigitCode_postsError() {
        List<String> errors = collectValues(viewModel.getErrorMessage());
        viewModel.verifyOtp("verif-id", "12345", "John", "+15551234567", "customer", "register");
        assertEquals("Please enter the 6-digit code", errors.get(0));
    }

    @Test
    public void verifyOtp_sevenDigitCode_postsError() {
        List<String> errors = collectValues(viewModel.getErrorMessage());
        viewModel.verifyOtp("verif-id", "1234567", "John", "+15551234567", "customer", "register");
        assertEquals("Please enter the 6-digit code", errors.get(0));
    }

    @Test
    public void verifyOtp_invalidCode_doesNotCallRepository() {
        viewModel.verifyOtp("verif-id", "123", "John", "+15551234567", "customer", "register");
        verifyNoInteractions(mockRepo);
    }

    // ── signInWithCredential: repository interaction ─────────────────────────

    @Test
    public void signInWithCredential_callsRepositoryWithCorrectArgs() {
        viewModel.signInWithCredential(null, "John", "+15551234567", "customer", "register");
        verify(mockRepo).signInWithPhoneCredential(
            isNull(), eq("John"), eq("+15551234567"), eq("customer"), eq("register"),
            any(AuthRepository.AuthCallback.class)
        );
    }

    @Test
    public void signInWithCredential_setsLoadingTrue_beforeCallback() {
        doAnswer(inv -> null)
            .when(mockRepo).signInWithPhoneCredential(any(), any(), any(), any(), any(), any());

        List<Boolean> loadingStates = collectValues(viewModel.getLoading());
        viewModel.signInWithCredential(null, "John", "+15551234567", "customer", "register");

        assertTrue("Loading must be true before callback fires", loadingStates.contains(true));
    }

    @Test
    public void signInWithCredential_success_postsVerificationSuccessTrue() {
        doAnswer(inv -> {
            ((AuthRepository.AuthCallback) inv.getArgument(5)).onSuccess();
            return null;
        }).when(mockRepo).signInWithPhoneCredential(any(), any(), any(), any(), any(), any());

        viewModel.signInWithCredential(null, "John", "+15551234567", "customer", "register");

        assertEquals(Boolean.TRUE, viewModel.getVerificationSuccess().getValue());
    }

    @Test
    public void signInWithCredential_success_setsLoadingFalse() {
        doAnswer(inv -> {
            ((AuthRepository.AuthCallback) inv.getArgument(5)).onSuccess();
            return null;
        }).when(mockRepo).signInWithPhoneCredential(any(), any(), any(), any(), any(), any());

        viewModel.signInWithCredential(null, "John", "+15551234567", "customer", "register");

        assertEquals(Boolean.FALSE, viewModel.getLoading().getValue());
    }

    @Test
    public void signInWithCredential_error_postsErrorMessage() {
        doAnswer(inv -> {
            ((AuthRepository.AuthCallback) inv.getArgument(5)).onError("Invalid code.");
            return null;
        }).when(mockRepo).signInWithPhoneCredential(any(), any(), any(), any(), any(), any());

        List<String> errors = collectValues(viewModel.getErrorMessage());
        viewModel.signInWithCredential(null, "John", "+15551234567", "customer", "register");

        assertTrue(errors.contains("Invalid code."));
    }

    @Test
    public void signInWithCredential_error_setsLoadingFalse() {
        doAnswer(inv -> {
            ((AuthRepository.AuthCallback) inv.getArgument(5)).onError("Error");
            return null;
        }).when(mockRepo).signInWithPhoneCredential(any(), any(), any(), any(), any(), any());

        viewModel.signInWithCredential(null, "John", "+15551234567", "customer", "register");

        assertEquals(Boolean.FALSE, viewModel.getLoading().getValue());
    }

    @Test
    public void signInWithCredential_loginMode_passedToRepository() {
        viewModel.signInWithCredential(null, null, "+15551234567", null, "login");
        verify(mockRepo).signInWithPhoneCredential(
            isNull(), isNull(), eq("+15551234567"), isNull(), eq("login"),
            any(AuthRepository.AuthCallback.class)
        );
    }
}
