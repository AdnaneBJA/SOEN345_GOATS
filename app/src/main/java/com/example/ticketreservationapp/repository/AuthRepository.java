package com.example.ticketreservationapp.repository;

import com.example.ticketreservationapp.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class AuthRepository {
    public interface AuthCallback {
        void onSuccess();
        void onError(String message);
    }

    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public void registerWithEmail(String fullName, String email, String password,
                                  String phone, String role, AuthCallback callback) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && firebaseAuth.getCurrentUser() != null) {
                    String uid = firebaseAuth.getCurrentUser().getUid();
                    User user = new User(uid, fullName, email, phone, role);
                    firestore.collection("users").document(uid)
                        .set(user)
                        .addOnSuccessListener(aVoid -> callback.onSuccess())
                        .addOnFailureListener(e -> callback.onError(e.getMessage()));
                } else {
                    String msg = task.getException() != null
                        ? task.getException().getMessage()
                        : "Registration failed. Please try again.";
                    callback.onError(msg);
                }
            });
    }



    public void loginWithEmail(String email, String password, AuthCallback callback) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    String msg = task.getException() != null
                        ? task.getException().getMessage()
                        : "Login failed. Please try again.";
                    callback.onError(msg);
                }
            });
    }

    public void signInWithPhoneCredential(com.google.firebase.auth.PhoneAuthCredential credential,
            String fullName, String phone, String role, String mode, AuthCallback callback) {
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful() && firebaseAuth.getCurrentUser() != null) {
                if ("register".equals(mode)) {
                    String uid = firebaseAuth.getCurrentUser().getUid();
                    User user = new User(uid, fullName, "", phone, role);
                    firestore.collection("users").document(uid).set(user)
                        .addOnSuccessListener(v -> callback.onSuccess())
                        .addOnFailureListener(e -> callback.onError(e.getMessage()));
                } else {
                    callback.onSuccess();
                }
            } else {
                String msg = task.getException() != null
                    ? task.getException().getMessage()
                    : "Verification failed.";
                callback.onError(msg);
            }
        });
    }

    public void sendPasswordReset(String email, AuthCallback callback) {
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    String msg = task.getException() != null
                        ? task.getException().getMessage()
                        : "Failed to send reset email.";
                    callback.onError(msg);
                }
            });
    }
}
