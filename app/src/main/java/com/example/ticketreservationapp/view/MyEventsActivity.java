package com.example.ticketreservationapp.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ticketreservationapp.R;
import com.example.ticketreservationapp.model.Event;
import com.example.ticketreservationapp.viewmodel.MyEventsViewModel;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MyEventsActivity extends AppCompatActivity {

    private MyEventsViewModel viewModel;
    private EventAdapter adapter;
    private TextView tvNoResults;
    private LinearProgressIndicator progressBar;
    private String organizerId;

    private final ActivityResultLauncher<Intent> editLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    viewModel.loadMyEvents(organizerId);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_events);

        viewModel = new ViewModelProvider(this).get(MyEventsViewModel.class);

        RecyclerView recyclerView = findViewById(R.id.rv_my_events);
        tvNoResults = findViewById(R.id.tv_no_results);
        progressBar = findViewById(R.id.progress_bar);

        adapter = new EventAdapter(this::onEventClick);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            organizerId = user.getUid();
            observeViewModel();
            viewModel.loadMyEvents(organizerId);
        }
    }

    private void onEventClick(Event event) {
        String[] options = {
                getString(R.string.action_view),
                getString(R.string.action_edit),
                getString(R.string.action_cancel_event)
        };

        new AlertDialog.Builder(this)
                .setTitle(event.getTitle())
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            openEventDetail(event);
                            break;
                        case 1:
                            openEditEvent(event);
                            break;
                        case 2:
                            confirmDeleteEvent(event);
                            break;
                    }
                })
                .show();
    }

    private void openEventDetail(Event event) {
        Intent intent = new Intent(this, EventDetailActivity.class);
        intent.putExtra("event_id", event.getId());
        intent.putExtra("event_title", event.getTitle());
        intent.putExtra("event_description", event.getDescription());
        intent.putExtra("event_date", event.getDate());
        intent.putExtra("event_location", event.getLocation());
        intent.putExtra("event_category", event.getCategory());
        intent.putExtra("event_price", event.getPrice());
        intent.putExtra("event_available_seats", event.getAvailableSeats());
        intent.putExtra("event_total_seats", event.getTotalSeats());
        intent.putExtra("event_organizer_name", event.getOrganizerName());
        intent.putExtra("event_organizer_id", event.getOrganizerId());
        startActivity(intent);
    }

    private void openEditEvent(Event event) {
        Intent intent = new Intent(this, CreateEventActivity.class);
        intent.putExtra("edit_event_id", event.getId());
        intent.putExtra("edit_title", event.getTitle());
        intent.putExtra("edit_description", event.getDescription());
        intent.putExtra("edit_date", event.getDate());
        intent.putExtra("edit_location", event.getLocation());
        intent.putExtra("edit_category", event.getCategory());
        intent.putExtra("edit_price", event.getPrice());
        intent.putExtra("edit_total_seats", event.getTotalSeats());
        intent.putExtra("edit_organizer_id", event.getOrganizerId());
        intent.putExtra("edit_organizer_name", event.getOrganizerName());
        editLauncher.launch(intent);
    }

    private void confirmDeleteEvent(Event event) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.cancel_event_dialog_title)
                .setMessage(getString(R.string.cancel_event_dialog_message, event.getTitle()))
                .setPositiveButton(R.string.action_yes, (dialog, which) ->
                        viewModel.deleteEvent(event.getId()))
                .setNegativeButton(R.string.action_no, null)
                .show();
    }

    private void observeViewModel() {
        viewModel.getEvents().observe(this, events -> {
            adapter.setEvents(events);
            tvNoResults.setVisibility(events.isEmpty() ? View.VISIBLE : View.GONE);
        });

        viewModel.getLoading().observe(this, isLoading ->
                progressBar.setVisibility(Boolean.TRUE.equals(isLoading) ? View.VISIBLE : View.GONE));

        viewModel.getErrorMessage().observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getDeleteSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(this, R.string.event_cancelled_success, Toast.LENGTH_SHORT).show();
                viewModel.loadMyEvents(organizerId);
            }
        });
    }
}
