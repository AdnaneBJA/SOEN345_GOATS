package com.example.ticketreservationapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.ticketreservationapp.repository.AuthRepository;

import java.util.regex.Pattern;

public class RegisterViewModel extends ViewModel {

    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}");

    private final AuthRepository authRepository;

    public RegisterViewModel() {
        this.authRepository = new AuthRepository();
    }

    // Visible for unit testing — allows injecting a mock repository
    public RegisterViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> registrationSuccess = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);

    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getRegistrationSuccess() { return registrationSuccess; }
    public LiveData<Boolean> getLoading() { return loading; }

    /**
     * Called only for the email path. The phone path is intercepted by RegisterActivity
     * before this method is invoked.
     *
     * @param fullName        required; stored in the Firestore user profile
     * @param identifier      email address
     * @param password        min 6 characters
     * @param confirmPassword must match password
     * @param role            "customer" or "organizer"
     */
    public void register(String fullName, String identifier,
                         String password, String confirmPassword, String role) {
        if (fullName.isEmpty()) {
            errorMessage.setValue("Full name is required");
            return;
        }

        if (identifier.isEmpty()) {
            errorMessage.setValue("Email or phone number is required");
            return;
        }

        boolean isEmail = identifier.contains("@");
        if (isEmail) {
            if (!EMAIL_PATTERN.matcher(identifier).matches()) {
                errorMessage.setValue("Invalid email format");
                return;
            }
        }

        if (password.isEmpty()) {
            errorMessage.setValue("Password is required");
            return;
        }
        if (password.length() < 6) {
            errorMessage.setValue("Password must be at least 6 characters");
            return;
        }
        if (!password.equals(confirmPassword)) {
            errorMessage.setValue("Passwords do not match");
            return;
        }

        loading.setValue(true);
        authRepository.registerWithEmail(fullName, identifier, password, "", role,
            new AuthRepository.AuthCallback() {
                @Override
                public void onSuccess() {
                    loading.postValue(false);
                    registrationSuccess.postValue(true);
                }

                @Override
                public void onError(String message) {
                    loading.postValue(false);
                    errorMessage.postValue(message);
                }
            });
    }
}
