package com.example.ticketreservationapp.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.ticketreservationapp.MainActivity;
import com.example.ticketreservationapp.R;
import com.example.ticketreservationapp.repository.AuthRepository;
import com.example.ticketreservationapp.viewmodel.LoginViewModel;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private LoginViewModel viewModel;
    private final AuthRepository authRepository = new AuthRepository();
    private TextInputLayout tilIdentifier, tilPassword;
    private LinearProgressIndicator progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Skip login if user session already exists
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            goToHome();
            return;
        }

        setContentView(R.layout.activity_login);

        tilIdentifier = findViewById(R.id.til_identifier);
        tilPassword   = findViewById(R.id.til_password);
        progressBar   = findViewById(R.id.progress_bar);

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Snackbar.make(findViewById(android.R.id.content), error, Snackbar.LENGTH_LONG).show();
            }
        });

        viewModel.getLoginSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                goToHome();
            }
        });

        viewModel.getResetEmailSent().observe(this, email -> {
            if (email != null) {
                Snackbar.make(findViewById(android.R.id.content),
                    "Password reset email sent to " + email,
                    Snackbar.LENGTH_LONG).show();
            }
        });

        viewModel.getLoading().observe(this, isLoading ->
            progressBar.setVisibility(Boolean.TRUE.equals(isLoading) ? View.VISIBLE : View.GONE)
        );

        findViewById(R.id.btn_login).setOnClickListener(v -> {
            String identifier = getTextFrom(tilIdentifier);
            String password   = getTextFrom(tilPassword);

            boolean isEmail = identifier.contains("@");
            if (!isEmail) {
                // Phone path: validate and start OTP flow
                if (identifier.isEmpty()) {
                    Snackbar.make(findViewById(android.R.id.content),
                        "Email or phone is required", Snackbar.LENGTH_LONG).show();
                    return;
                }
                if (!identifier.startsWith("+") || !identifier.matches("\\+\\d{7,15}")) {
                    Snackbar.make(findViewById(android.R.id.content),
                        "Include country code (e.g. +14381234567)", Snackbar.LENGTH_LONG).show();
                    return;
                }
                startPhoneVerification(identifier);
            } else {
                // Email path: delegate to ViewModel
                viewModel.login(identifier, password);
            }
        });

        // Sends a reset email using whatever is typed in the identifier field
        findViewById(R.id.tv_forgot_password).setOnClickListener(v ->
            viewModel.sendPasswordReset(getTextFrom(tilIdentifier))
        );

        findViewById(R.id.btn_create_account).setOnClickListener(v ->
            startActivity(new Intent(this, RegisterActivity.class))
        );
    }

    private void startPhoneVerification(String phone) {
        progressBar.setVisibility(View.VISIBLE);
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                    // Instant verification — skip OTP screen
                    progressBar.setVisibility(View.GONE);
                    authRepository.signInWithPhoneCredential(credential, null, phone, null,
                        "login", new AuthRepository.AuthCallback() {
                            @Override
                            public void onSuccess() {
                                goToHome();
                            }

                            @Override
                            public void onError(String message) {
                                Snackbar.make(findViewById(android.R.id.content),
                                    message, Snackbar.LENGTH_LONG).show();
                            }
                        });
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    progressBar.setVisibility(View.GONE);
                    Snackbar.make(findViewById(android.R.id.content),
                        e.getMessage() != null ? e.getMessage() : "Verification failed.",
                        Snackbar.LENGTH_LONG).show();
                }

                @Override
                public void onCodeSent(@NonNull String verificationId,
                                       @NonNull PhoneAuthProvider.ForceResendingToken token) {
                    progressBar.setVisibility(View.GONE);
                    Intent intent = new Intent(LoginActivity.this, OtpActivity.class);
                    intent.putExtra("verificationId", verificationId);
                    intent.putExtra("phone", phone);
                    intent.putExtra("mode", "login");
                    startActivity(intent);
                }
            })
            .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void goToHome() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private String getTextFrom(TextInputLayout til) {
        if (til.getEditText() != null && til.getEditText().getText() != null) {
            return til.getEditText().getText().toString().trim();
        }
        return "";
    }
}
