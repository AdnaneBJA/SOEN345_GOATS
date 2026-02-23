package com.example.ticketreservationapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.ticketreservationapp.repository.AuthRepository;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

public class OtpViewModel extends ViewModel {

    private final AuthRepository authRepository;

    public OtpViewModel() {
        this.authRepository = new AuthRepository();
    }

    // Visible for unit testing — allows injecting a mock repository
    public OtpViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> verificationSuccess = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);

    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getVerificationSuccess() { return verificationSuccess; }
    public LiveData<Boolean> getLoading() { return loading; }

    /**
     * Build a credential from verificationId + otp string and sign in.
     * Note: PhoneAuthProvider.getCredential() is only called for valid (6-digit) codes,
     * keeping the validation path fully testable without Firebase.
     */
    public void verifyOtp(String verificationId, String otp,
                          String fullName, String phone, String role, String mode) {
        if (otp == null || otp.length() != 6) {
            errorMessage.setValue("Please enter the 6-digit code");
            return;
        }
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
        signInWithCredential(credential, fullName, phone, role, mode);
    }

    /**
     * Sign in with a ready-made credential (e.g. from auto-verification on resend).
     */
    public void signInWithCredential(PhoneAuthCredential credential,
                                     String fullName, String phone, String role, String mode) {
        loading.setValue(true);
        authRepository.signInWithPhoneCredential(credential, fullName, phone, role, mode,
            new AuthRepository.AuthCallback() {
                @Override
                public void onSuccess() {
                    loading.postValue(false);
                    verificationSuccess.postValue(true);
                }

                @Override
                public void onError(String message) {
                    loading.postValue(false);
                    errorMessage.postValue(message);
                }
            });
    }
}
