package com.example.ticketreservationapp.view;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ticketreservationapp.R;
import com.example.ticketreservationapp.viewmodel.MyReservationsViewModel;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MyReservationsActivity extends AppCompatActivity {

    private MyReservationsViewModel viewModel;
    private ReservationAdapter adapter;
    private TextView tvNoResults;
    private LinearProgressIndicator progressBar;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_reservations);

        viewModel = new ViewModelProvider(this).get(MyReservationsViewModel.class);

        RecyclerView recyclerView = findViewById(R.id.rv_reservations);
        tvNoResults = findViewById(R.id.tv_no_results);
        progressBar = findViewById(R.id.progress_bar);

        adapter = new ReservationAdapter(this::confirmCancel);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userId = user.getUid();
            observeViewModel();
            viewModel.loadReservations(userId);
        }
    }

    private void confirmCancel(com.example.ticketreservationapp.model.Reservation reservation) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.cancel_reservation_dialog_title)
                .setMessage(getString(R.string.cancel_reservation_dialog_message, reservation.getEventTitle()))
                .setPositiveButton(R.string.action_yes, (dialog, which) ->
                        viewModel.cancelReservation(reservation.getId()))
                .setNegativeButton(R.string.action_no, null)
                .show();
    }

    private void observeViewModel() {
        viewModel.getReservations().observe(this, reservations -> {
            adapter.setReservations(reservations);
            tvNoResults.setVisibility(reservations.isEmpty() ? View.VISIBLE : View.GONE);
        });

        viewModel.getLoading().observe(this, isLoading ->
                progressBar.setVisibility(Boolean.TRUE.equals(isLoading) ? View.VISIBLE : View.GONE));

        viewModel.getErrorMessage().observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getCancelSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(this, R.string.reservation_cancelled_success, Toast.LENGTH_SHORT).show();
                viewModel.loadReservations(userId);
            }
        });
    }
}
