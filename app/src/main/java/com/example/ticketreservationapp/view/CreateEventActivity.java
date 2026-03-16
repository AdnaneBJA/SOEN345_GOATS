package com.example.ticketreservationapp.view;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.ticketreservationapp.R;
import com.example.ticketreservationapp.viewmodel.CreateEventViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Locale;

public class CreateEventActivity extends AppCompatActivity {

    private CreateEventViewModel viewModel;
    private TextInputEditText etTitle, etDescription, etDate, etLocation, etPrice, etSeats;
    private AutoCompleteTextView spinnerCategory;
    private LinearProgressIndicator progressBar;

    private boolean isEditMode = false;
    private String editEventId;
    private String editOrganizerId;
    private String editOrganizerName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        viewModel = new ViewModelProvider(this).get(CreateEventViewModel.class);

        etTitle = findViewById(R.id.et_event_title);
        etDescription = findViewById(R.id.et_event_description);
        etDate = findViewById(R.id.et_event_date);
        etLocation = findViewById(R.id.et_event_location);
        etPrice = findViewById(R.id.et_event_price);
        etSeats = findViewById(R.id.et_event_seats);
        spinnerCategory = findViewById(R.id.spinner_event_category);
        progressBar = findViewById(R.id.progress_bar);

        TextView tvTitle = findViewById(R.id.tv_form_title);
        TextView tvSubtitle = findViewById(R.id.tv_form_subtitle);
        MaterialButton btnSubmit = findViewById(R.id.btn_create_event);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        setupCategoryDropdown();
        setupDatePicker();

        // Check if we're in edit mode
        if (getIntent().hasExtra("edit_event_id")) {
            isEditMode = true;
            editEventId = getIntent().getStringExtra("edit_event_id");
            editOrganizerId = getIntent().getStringExtra("edit_organizer_id");
            editOrganizerName = getIntent().getStringExtra("edit_organizer_name");

            tvTitle.setText(R.string.edit_event_title);
            tvSubtitle.setText(R.string.edit_event_subtitle);
            btnSubmit.setText(R.string.btn_update_event);

            // Pre-fill fields
            etTitle.setText(getIntent().getStringExtra("edit_title"));
            etDescription.setText(getIntent().getStringExtra("edit_description"));
            etDate.setText(getIntent().getStringExtra("edit_date"));
            etLocation.setText(getIntent().getStringExtra("edit_location"));
            spinnerCategory.setText(getIntent().getStringExtra("edit_category"), false);
            etPrice.setText(String.valueOf(getIntent().getDoubleExtra("edit_price", 0)));
            etSeats.setText(String.valueOf(getIntent().getIntExtra("edit_total_seats", 0)));
        }

        btnSubmit.setOnClickListener(v -> submitEvent());

        observeViewModel();
    }

    private void setupCategoryDropdown() {
        String[] categories = {"Movies", "Concerts", "Travel", "Sports"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, categories);
        spinnerCategory.setAdapter(adapter);
    }

    private void setupDatePicker() {
        etDate.setFocusable(false);
        etDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                String date = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                etDate.setText(date);
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
                    .show();
        });
    }

    private void submitEvent() {
        if (isEditMode) {
            viewModel.updateEvent(
                    editEventId,
                    getText(etTitle), getText(etDescription), getText(etDate),
                    getText(etLocation), spinnerCategory.getText().toString(),
                    getText(etPrice), getText(etSeats),
                    editOrganizerId, editOrganizerName
            );
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "You must be logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        String organizerId = user.getUid();

        FirebaseFirestore.getInstance().collection("users").document(organizerId)
            .get()
            .addOnSuccessListener(doc -> {
                String organizerName = doc.getString("fullName");
                if (organizerName == null) organizerName = "Unknown";

                viewModel.createEvent(
                        getText(etTitle), getText(etDescription), getText(etDate),
                        getText(etLocation), spinnerCategory.getText().toString(),
                        getText(etPrice), getText(etSeats),
                        organizerId, organizerName
                );
            })
            .addOnFailureListener(e ->
                Toast.makeText(this, "Failed to load profile.", Toast.LENGTH_SHORT).show()
            );
    }

    private String getText(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString() : "";
    }

    private void observeViewModel() {
        viewModel.getCreateSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(this, R.string.event_created_success, Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }
        });

        viewModel.getUpdateSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(this, R.string.event_updated_success, Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }
        });

        viewModel.getErrorMessage().observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getLoading().observe(this, isLoading ->
                progressBar.setVisibility(Boolean.TRUE.equals(isLoading) ? View.VISIBLE : View.GONE));
    }
}
