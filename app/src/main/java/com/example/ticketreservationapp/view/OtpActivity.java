package com.example.ticketreservationapp.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.ticketreservationapp.MainActivity;
import com.example.ticketreservationapp.R;
import com.example.ticketreservationapp.viewmodel.OtpViewModel;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class OtpActivity extends AppCompatActivity {

    private OtpViewModel viewModel;
    private TextInputLayout tilOtp;
    private LinearProgressIndicator progressBar;

    private String verificationId;
    private String phone;
    private String mode;
    private String fullName;
    private String role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        verificationId = getIntent().getStringExtra("verificationId");
        phone          = getIntent().getStringExtra("phone");
        mode           = getIntent().getStringExtra("mode");
        fullName       = getIntent().getStringExtra("fullName");
        role           = getIntent().getStringExtra("role");

        tilOtp      = findViewById(R.id.til_otp);
        progressBar = findViewById(R.id.progress_bar);

        TextView tvSubtitle = findViewById(R.id.tv_otp_subtitle);
        tvSubtitle.setText(getString(R.string.otp_subtitle, phone));

        viewModel = new ViewModelProvider(this).get(OtpViewModel.class);

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Snackbar.make(findViewById(android.R.id.content), error, Snackbar.LENGTH_LONG).show();
            }
        });

        viewModel.getVerificationSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        viewModel.getLoading().observe(this, isLoading ->
            progressBar.setVisibility(Boolean.TRUE.equals(isLoading) ? View.VISIBLE : View.GONE)
        );

        findViewById(R.id.btn_verify).setOnClickListener(v -> {
            String otp = getTextFrom(tilOtp);
            viewModel.verifyOtp(verificationId, otp, fullName, phone, role, mode);
        });

        findViewById(R.id.tv_resend).setOnClickListener(v -> resendCode());
    }

    private void resendCode() {
        progressBar.setVisibility(View.VISIBLE);
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                    progressBar.setVisibility(View.GONE);
                    viewModel.signInWithCredential(credential, fullName, phone, role, mode);
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    progressBar.setVisibility(View.GONE);
                    Snackbar.make(findViewById(android.R.id.content),
                        e.getMessage() != null ? e.getMessage() : "Resend failed.",
                        Snackbar.LENGTH_LONG).show();
                }

                @Override
                public void onCodeSent(@NonNull String newVerificationId,
                                       @NonNull PhoneAuthProvider.ForceResendingToken token) {
                    progressBar.setVisibility(View.GONE);
                    verificationId = newVerificationId;
                    Snackbar.make(findViewById(android.R.id.content),
                        "Code resent to " + phone, Snackbar.LENGTH_SHORT).show();
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
