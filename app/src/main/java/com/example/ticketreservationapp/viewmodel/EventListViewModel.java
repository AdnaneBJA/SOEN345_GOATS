package com.example.ticketreservationapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.ticketreservationapp.model.Event;
import com.example.ticketreservationapp.repository.EventRepository;

import java.util.ArrayList;
import java.util.List;

public class EventListViewModel extends ViewModel {

    private final EventRepository repository;

    private final MutableLiveData<List<Event>> events = new MutableLiveData<>();
    private final MutableLiveData<List<Event>> filteredEvents = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);

    private String currentSearchQuery = "";
    private String currentCategoryFilter = "";
    private String currentLocationFilter = "";
    private String currentDateFilter = "";

    public EventListViewModel() {
        this.repository = new EventRepository();
    }

    public EventListViewModel(EventRepository repository) {
        this.repository = repository;
    }

    public LiveData<List<Event>> getFilteredEvents() { return filteredEvents; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getLoading() { return loading; }

    public void loadEvents() {
        loading.setValue(true);
        repository.getAllEvents(new EventRepository.EventListCallback() {
            @Override
            public void onSuccess(List<Event> eventList) {
                events.setValue(eventList);
                applyFilters();
                loading.setValue(false);
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
                loading.setValue(false);
            }
        });
    }

    public void setSearchQuery(String query) {
        this.currentSearchQuery = query != null ? query.trim().toLowerCase() : "";
        applyFilters();
    }

    public void setCategoryFilter(String category) {
        this.currentCategoryFilter = category != null ? category : "";
        applyFilters();
    }

    public void setLocationFilter(String location) {
        this.currentLocationFilter = location != null ? location.trim().toLowerCase() : "";
        applyFilters();
    }

    public void setDateFilter(String date) {
        this.currentDateFilter = date != null ? date : "";
        applyFilters();
    }

    public void clearFilters() {
        this.currentSearchQuery = "";
        this.currentCategoryFilter = "";
        this.currentLocationFilter = "";
        this.currentDateFilter = "";
        applyFilters();
    }

    private void applyFilters() {
        List<Event> allEvents = events.getValue();
        if (allEvents == null) {
            filteredEvents.setValue(new ArrayList<>());
            return;
        }

        List<Event> result = new ArrayList<>();
        for (Event event : allEvents) {
            if (matchesSearch(event) && matchesCategory(event)
                    && matchesLocation(event) && matchesDate(event)) {
                result.add(event);
            }
        }
        filteredEvents.setValue(result);
    }

    private boolean matchesSearch(Event event) {
        if (currentSearchQuery.isEmpty()) return true;
        String title = event.getTitle() != null ? event.getTitle().toLowerCase() : "";
        String desc = event.getDescription() != null ? event.getDescription().toLowerCase() : "";
        String location = event.getLocation() != null ? event.getLocation().toLowerCase() : "";
        return title.contains(currentSearchQuery)
                || desc.contains(currentSearchQuery)
                || location.contains(currentSearchQuery);
    }

    private boolean matchesCategory(Event event) {
        if (currentCategoryFilter.isEmpty()) return true;
        return currentCategoryFilter.equalsIgnoreCase(event.getCategory());
    }

    private boolean matchesLocation(Event event) {
        if (currentLocationFilter.isEmpty()) return true;
        String location = event.getLocation() != null ? event.getLocation().toLowerCase() : "";
        return location.contains(currentLocationFilter);
    }

    private boolean matchesDate(Event event) {
        if (currentDateFilter.isEmpty()) return true;
        return currentDateFilter.equals(event.getDate());
    }
}
