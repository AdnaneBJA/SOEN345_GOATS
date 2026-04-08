package com.example.ticketreservationapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ticketreservationapp.view.CreateEventActivity;
import com.example.ticketreservationapp.view.EventListActivity;
import com.example.ticketreservationapp.view.LoginActivity;
import com.example.ticketreservationapp.view.MyEventsActivity;
import com.example.ticketreservationapp.view.MyReservationsActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        TextView tvWelcome = findViewById(R.id.tv_welcome);
        MaterialButton btnCreateEvent = findViewById(R.id.btn_create_event);
        MaterialButton btnMyEvents = findViewById(R.id.btn_my_events);

        if (user != null) {
            tvWelcome.setText("Welcome, " + user.getEmail());

            // Show organizer-only buttons
            FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    String role = doc.getString("role");
                    if ("organizer".equals(role)) {
                        btnCreateEvent.setVisibility(View.VISIBLE);
                        btnMyEvents.setVisibility(View.VISIBLE);
                    }
                });
        }

        findViewById(R.id.btn_browse_events).setOnClickListener(v ->
                startActivity(new Intent(this, EventListActivity.class)));

        btnCreateEvent.setOnClickListener(v ->
                startActivity(new Intent(this, CreateEventActivity.class)));

        btnMyEvents.setOnClickListener(v ->
                startActivity(new Intent(this, MyEventsActivity.class)));

        findViewById(R.id.btn_my_reservations).setOnClickListener(v ->
                startActivity(new Intent(this, MyReservationsActivity.class)));

        findViewById(R.id.btn_sign_out).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });
    }
}
