package com.example.ticketreservationapp.view;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ticketreservationapp.R;
import com.example.ticketreservationapp.model.Event;
import com.example.ticketreservationapp.viewmodel.EventListViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.Locale;

public class EventListActivity extends AppCompatActivity {

    private EventListViewModel viewModel;
    private EventAdapter adapter;
    private TextView tvNoResults;
    private LinearProgressIndicator progressBar;
    private TextInputEditText etSearch;
    private AutoCompleteTextView spinnerCategory;
    private Chip chipDate;
    private Chip chipClearFilters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);

        viewModel = new ViewModelProvider(this).get(EventListViewModel.class);

        RecyclerView recyclerView = findViewById(R.id.rv_events);
        tvNoResults = findViewById(R.id.tv_no_results);
        progressBar = findViewById(R.id.progress_bar);
        etSearch = findViewById(R.id.et_search);
        spinnerCategory = findViewById(R.id.spinner_category);
        chipDate = findViewById(R.id.chip_date);
        chipClearFilters = findViewById(R.id.chip_clear_filters);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        adapter = new EventAdapter(this::openEventDetail);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        setupCategoryDropdown();
        setupSearchListener();
        setupDateFilter();
        setupClearFilters();
        observeViewModel();

        viewModel.loadEvents();
    }

    private void setupCategoryDropdown() {
        String[] categories = getResources().getStringArray(R.array.event_categories);
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, categories);
        spinnerCategory.setAdapter(catAdapter);
        spinnerCategory.setOnItemClickListener((parent, view, position, id) -> {
            String selected = categories[position];
            if ("All Categories".equals(selected)) {
                viewModel.setCategoryFilter("");
            } else {
                viewModel.setCategoryFilter(selected);
            }
        });
    }

    private void setupSearchListener() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setSearchQuery(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupDateFilter() {
        chipDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                String date = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                chipDate.setText(date);
                viewModel.setDateFilter(date);
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
                    .show();
        });
    }

    private void setupClearFilters() {
        chipClearFilters.setOnClickListener(v -> {
            etSearch.setText("");
            spinnerCategory.setText("", false);
            chipDate.setText(getString(R.string.filter_date));
            viewModel.clearFilters();
        });
    }

    private void observeViewModel() {
        viewModel.getFilteredEvents().observe(this, events -> {
            adapter.setEvents(events);
            tvNoResults.setVisibility(events.isEmpty() ? View.VISIBLE : View.GONE);
        });

        viewModel.getLoading().observe(this, isLoading ->
                progressBar.setVisibility(Boolean.TRUE.equals(isLoading) ? View.VISIBLE : View.GONE));

        viewModel.getErrorMessage().observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                tvNoResults.setText(msg);
                tvNoResults.setVisibility(View.VISIBLE);
            }
        });
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
}
