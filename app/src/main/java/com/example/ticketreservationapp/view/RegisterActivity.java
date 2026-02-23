package com.example.ticketreservationapp.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.ticketreservationapp.MainActivity;
import com.example.ticketreservationapp.R;
import com.example.ticketreservationapp.repository.AuthRepository;
import com.example.ticketreservationapp.viewmodel.RegisterViewModel;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class RegisterActivity extends AppCompatActivity {

    private RegisterViewModel viewModel;
    private final AuthRepository authRepository = new AuthRepository();
    private TextInputLayout tilFullName, tilIdentifier, tilPassword, tilConfirmPassword;
    private MaterialButtonToggleGroup toggleRole;
    private LinearProgressIndicator progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        tilFullName        = findViewById(R.id.til_full_name);
        tilIdentifier      = findViewById(R.id.til_identifier);
        tilPassword        = findViewById(R.id.til_password);
        tilConfirmPassword = findViewById(R.id.til_confirm_password);
        toggleRole         = findViewById(R.id.toggle_role);
        progressBar        = findViewById(R.id.progress_bar);

        viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Snackbar.make(findViewById(android.R.id.content), error, Snackbar.LENGTH_LONG).show();
            }
        });

        viewModel.getRegistrationSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(this, R.string.registration_success, Toast.LENGTH_LONG).show();
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        viewModel.getLoading().observe(this, isLoading ->
            progressBar.setVisibility(Boolean.TRUE.equals(isLoading) ? View.VISIBLE : View.GONE)
        );

        findViewById(R.id.btn_register).setOnClickListener(v -> {
            String fullName       = getTextFrom(tilFullName);
            String identifier     = getTextFrom(tilIdentifier);
            String password       = getTextFrom(tilPassword);
            String confirmPassword = getTextFrom(tilConfirmPassword);
            String role = (toggleRole.getCheckedButtonId() == R.id.btn_organizer)
                ? "organizer" : "customer";

            boolean isEmail = identifier.contains("@");
            if (!isEmail) {
                // Phone path: validate and start OTP flow
                if (fullName.isEmpty()) {
                    Snackbar.make(findViewById(android.R.id.content),
                        "Full name is required", Snackbar.LENGTH_LONG).show();
                    return;
                }
                if (identifier.isEmpty()) {
                    Snackbar.make(findViewById(android.R.id.content),
                        "Email or phone number is required", Snackbar.LENGTH_LONG).show();
                    return;
                }
                if (!identifier.startsWith("+") || !identifier.matches("\\+\\d{7,15}")) {
                    Snackbar.make(findViewById(android.R.id.content),
                        "Include country code (e.g. +14381234567)", Snackbar.LENGTH_LONG).show();
                    return;
                }
                startPhoneVerification(identifier, fullName, role);
            } else {
                // Email path: delegate to ViewModel
                viewModel.register(fullName, identifier, password, confirmPassword, role);
            }
        });

        // Navigate back when user taps the login link
        findViewById(R.id.tv_login_link).setOnClickListener(v -> finish());
    }

    private void startPhoneVerification(String phone, String fullName, String role) {
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
                    authRepository.signInWithPhoneCredential(credential, fullName, phone, role,
                        "register", new AuthRepository.AuthCallback() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(RegisterActivity.this,
                                    R.string.registration_success, Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
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
                    Intent intent = new Intent(RegisterActivity.this, OtpActivity.class);
                    intent.putExtra("verificationId", verificationId);
                    intent.putExtra("phone", phone);
                    intent.putExtra("fullName", fullName);
                    intent.putExtra("role", role);
                    intent.putExtra("mode", "register");
                    startActivity(intent);
                }
            })
            .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private String getTextFrom(TextInputLayout til) {
        if (til.getEditText() != null && til.getEditText().getText() != null) {
            return til.getEditText().getText().toString().trim();
        }
        return "";
    }
}
