package com.example.ticketreservationapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.ticketreservationapp.repository.AuthRepository;

import java.util.regex.Pattern;

public class LoginViewModel extends ViewModel {

    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}");

    private final AuthRepository authRepository;

    public LoginViewModel() {
        this.authRepository = new AuthRepository();
    }

    // Visible for unit testing — allows injecting a mock repository
    public LoginViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loginSuccess = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> resetEmailSent = new MutableLiveData<>();

    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getLoginSuccess() { return loginSuccess; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getResetEmailSent() { return resetEmailSent; }

    /**
     * Called only for the email path. The phone path is intercepted by LoginActivity
     * before this method is invoked.
     */
    public void login(String identifier, String password) {
        if (identifier.isEmpty()) {
            errorMessage.setValue("Email or phone is required");
            return;
        }

        boolean isEmail = identifier.contains("@");
        if (isEmail && !EMAIL_PATTERN.matcher(identifier).matches()) {
            errorMessage.setValue("Invalid email format");
            return;
        }
        if (password.isEmpty()) {
            errorMessage.setValue("Password is required");
            return;
        }

        loading.setValue(true);
        authRepository.loginWithEmail(identifier, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess() {
                loading.postValue(false);
                loginSuccess.postValue(true);
            }

            @Override
            public void onError(String message) {
                loading.postValue(false);
                errorMessage.postValue(message);
            }
        });
    }

    /**
     * Uses the email already typed in the identifier field — avoids a separate dialog.
     */
    public void sendPasswordReset(String email) {
        if (email.isEmpty() || !EMAIL_PATTERN.matcher(email).matches()) {
            errorMessage.setValue("Enter your email address in the field above first");
            return;
        }
        authRepository.sendPasswordReset(email, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess() {
                resetEmailSent.postValue(email);
            }

            @Override
            public void onError(String message) {
                errorMessage.postValue(message);
            }
        });
    }
}
