package com.example.ticketreservationapp.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ticketreservationapp.R;
import com.example.ticketreservationapp.model.Reservation;
import com.example.ticketreservationapp.repository.EventRepository;
import com.example.ticketreservationapp.repository.ReservationRepository;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;

public class EventDetailActivity extends AppCompatActivity {

    private String eventId, eventTitle, eventDescription, eventDate,
            eventLocation, eventCategory, eventOrganizerName, eventOrganizerId;
    private double eventPrice;
    private int eventAvailableSeats, eventTotalSeats;

    private final ActivityResultLauncher<Intent> editLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    // Event was updated, close detail so list refreshes
                    setResult(RESULT_OK);
                    finish();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        // Extract intent extras
        eventId = getIntent().getStringExtra("event_id");
        eventTitle = getIntent().getStringExtra("event_title");
        eventDescription = getIntent().getStringExtra("event_description");
        eventDate = getIntent().getStringExtra("event_date");
        eventLocation = getIntent().getStringExtra("event_location");
        eventCategory = getIntent().getStringExtra("event_category");
        eventOrganizerName = getIntent().getStringExtra("event_organizer_name");
        eventOrganizerId = getIntent().getStringExtra("event_organizer_id");
        eventPrice = getIntent().getDoubleExtra("event_price", 0);
        eventAvailableSeats = getIntent().getIntExtra("event_available_seats", 0);
        eventTotalSeats = getIntent().getIntExtra("event_total_seats", 0);

        // Bind views
        TextView tvTitle = findViewById(R.id.tv_detail_title);
        TextView tvDate = findViewById(R.id.tv_detail_date);
        TextView tvLocation = findViewById(R.id.tv_detail_location);
        TextView tvCategory = findViewById(R.id.tv_detail_category);
        TextView tvPrice = findViewById(R.id.tv_detail_price);
        TextView tvDescription = findViewById(R.id.tv_detail_description);
        TextView tvSeats = findViewById(R.id.tv_detail_seats);
        TextView tvOrganizer = findViewById(R.id.tv_detail_organizer);
        MaterialButton btnEdit = findViewById(R.id.btn_edit_event);
        MaterialButton btnCancel = findViewById(R.id.btn_cancel_event);
        MaterialButton btnReserve = findViewById(R.id.btn_reserve_ticket);

        tvTitle.setText(eventTitle);
        tvDate.setText(eventDate);
        tvLocation.setText(eventLocation);
        tvCategory.setText(eventCategory);
        tvDescription.setText(eventDescription);
        tvOrganizer.setText(getString(R.string.organized_by, eventOrganizerName));
        tvPrice.setText(String.format(Locale.US, "$%.2f", eventPrice));
        tvSeats.setText(getString(R.string.seats_available, eventAvailableSeats, eventTotalSeats));

        // Show edit/cancel buttons only if current user is the organizer; otherwise show Reserve.
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        boolean isOrganizer = currentUser != null && eventOrganizerId != null
                && currentUser.getUid().equals(eventOrganizerId);
        if (isOrganizer) {
            btnEdit.setVisibility(View.VISIBLE);
            btnCancel.setVisibility(View.VISIBLE);
        } else if (currentUser != null) {
            btnReserve.setVisibility(View.VISIBLE);
        }

        btnEdit.setOnClickListener(v -> openEditEvent());
        btnCancel.setOnClickListener(v -> confirmCancelEvent());
        btnReserve.setOnClickListener(v -> showReserveDialog());
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    private void showReserveDialog() {
        if (eventAvailableSeats <= 0) {
            Toast.makeText(this, R.string.error_no_seats, Toast.LENGTH_LONG).show();
            return;
        }
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint(R.string.hint_number_of_tickets);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        input.setPadding(padding, padding, padding, padding);

        new AlertDialog.Builder(this)
                .setTitle(R.string.reserve_dialog_title)
                .setMessage(getString(R.string.reserve_dialog_message, eventAvailableSeats))
                .setView(input)
                .setPositiveButton(R.string.action_reserve, (dialog, which) -> {
                    String text = input.getText().toString().trim();
                    int qty;
                    try {
                        qty = Integer.parseInt(text);
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, R.string.error_invalid_ticket_count, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (qty <= 0) {
                        Toast.makeText(this, R.string.error_invalid_ticket_count, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (qty > eventAvailableSeats) {
                        Toast.makeText(this, R.string.error_no_seats, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    submitReservation(qty);
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void submitReservation(int qty) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;
        new ReservationRepository().reserveTicket(currentUser.getUid(), eventId, qty,
                new ReservationRepository.ReservationCallback() {
                    @Override
                    public void onSuccess(Reservation reservation) {
                        eventAvailableSeats -= qty;
                        TextView tvSeats = findViewById(R.id.tv_detail_seats);
                        tvSeats.setText(getString(R.string.seats_available,
                                eventAvailableSeats, eventTotalSeats));
                        showConfirmation(reservation);
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(EventDetailActivity.this, message, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showConfirmation(Reservation r) {
        String message = getString(R.string.reservation_success_message,
                r.getEventTitle(),
                r.getNumberOfTickets(),
                r.getTotalPrice(),
                r.getEventDate(),
                r.getEventLocation(),
                r.getConfirmationCode());
        new AlertDialog.Builder(this)
                .setTitle(R.string.reservation_success_title)
                .setMessage(message)
                .setPositiveButton(R.string.action_ok, null)
                .show();
    }

    private void openEditEvent() {
        Intent intent = new Intent(this, CreateEventActivity.class);
        intent.putExtra("edit_event_id", eventId);
        intent.putExtra("edit_title", eventTitle);
        intent.putExtra("edit_description", eventDescription);
        intent.putExtra("edit_date", eventDate);
        intent.putExtra("edit_location", eventLocation);
        intent.putExtra("edit_category", eventCategory);
        intent.putExtra("edit_price", eventPrice);
        intent.putExtra("edit_total_seats", eventTotalSeats);
        intent.putExtra("edit_organizer_id", eventOrganizerId);
        intent.putExtra("edit_organizer_name", eventOrganizerName);
        editLauncher.launch(intent);
    }

    private void confirmCancelEvent() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.cancel_event_dialog_title)
                .setMessage(getString(R.string.cancel_event_dialog_message, eventTitle))
                .setPositiveButton(R.string.action_yes, (dialog, which) -> deleteEvent())
                .setNegativeButton(R.string.action_no, null)
                .show();
    }

    private void deleteEvent() {
        new EventRepository().deleteEvent(eventId, new EventRepository.EventCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(EventDetailActivity.this,
                        R.string.event_cancelled_success, Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(EventDetailActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
