package com.example.ticketreservationapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.ticketreservationapp.model.Event;
import com.example.ticketreservationapp.repository.EventRepository;

public class CreateEventViewModel extends ViewModel {

    private final EventRepository repository;

    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> createSuccess = new MutableLiveData<>();
    private final MutableLiveData<Boolean> updateSuccess = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);

    public CreateEventViewModel() {
        this.repository = new EventRepository();
    }

    public CreateEventViewModel(EventRepository repository) {
        this.repository = repository;
    }

    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getCreateSuccess() { return createSuccess; }
    public LiveData<Boolean> getUpdateSuccess() { return updateSuccess; }
    public LiveData<Boolean> getLoading() { return loading; }

    public void createEvent(String title, String description, String date,
                            String location, String category, String priceStr,
                            String seatsStr, String organizerId, String organizerName) {

        if (title == null || title.trim().isEmpty()) {
            errorMessage.setValue("Title is required.");
            return;
        }
        if (description == null || description.trim().isEmpty()) {
            errorMessage.setValue("Description is required.");
            return;
        }
        if (date == null || date.trim().isEmpty()) {
            errorMessage.setValue("Date is required.");
            return;
        }
        if (location == null || location.trim().isEmpty()) {
            errorMessage.setValue("Location is required.");
            return;
        }
        if (category == null || category.trim().isEmpty()) {
            errorMessage.setValue("Category is required.");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
            if (price < 0) {
                errorMessage.setValue("Price cannot be negative.");
                return;
            }
        } catch (NumberFormatException e) {
            errorMessage.setValue("Please enter a valid price.");
            return;
        }

        int totalSeats;
        try {
            totalSeats = Integer.parseInt(seatsStr);
            if (totalSeats <= 0) {
                errorMessage.setValue("Number of seats must be greater than zero.");
                return;
            }
        } catch (NumberFormatException e) {
            errorMessage.setValue("Please enter a valid number of seats.");
            return;
        }

        Event event = new Event(null, title.trim(), description.trim(), date.trim(),
                location.trim(), category, price, totalSeats, totalSeats,
                organizerId, organizerName);

        loading.setValue(true);
        repository.createEvent(event, new EventRepository.EventCallback() {
            @Override
            public void onSuccess() {
                createSuccess.setValue(true);
                loading.setValue(false);
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
                loading.setValue(false);
            }
        });
    }

    public void updateEvent(String eventId, String title, String description, String date,
                            String location, String category, String priceStr,
                            String seatsStr, String organizerId, String organizerName) {

        if (title == null || title.trim().isEmpty()) {
            errorMessage.setValue("Title is required.");
            return;
        }
        if (description == null || description.trim().isEmpty()) {
            errorMessage.setValue("Description is required.");
            return;
        }
        if (date == null || date.trim().isEmpty()) {
            errorMessage.setValue("Date is required.");
            return;
        }
        if (location == null || location.trim().isEmpty()) {
            errorMessage.setValue("Location is required.");
            return;
        }
        if (category == null || category.trim().isEmpty()) {
            errorMessage.setValue("Category is required.");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
            if (price < 0) {
                errorMessage.setValue("Price cannot be negative.");
                return;
            }
        } catch (NumberFormatException e) {
            errorMessage.setValue("Please enter a valid price.");
            return;
        }

        int totalSeats;
        try {
            totalSeats = Integer.parseInt(seatsStr);
            if (totalSeats <= 0) {
                errorMessage.setValue("Number of seats must be greater than zero.");
                return;
            }
        } catch (NumberFormatException e) {
            errorMessage.setValue("Please enter a valid number of seats.");
            return;
        }

        Event event = new Event(eventId, title.trim(), description.trim(), date.trim(),
                location.trim(), category, price, totalSeats, totalSeats,
                organizerId, organizerName);

        loading.setValue(true);
        repository.updateEvent(event, new EventRepository.EventCallback() {
            @Override
            public void onSuccess() {
                updateSuccess.setValue(true);
                loading.setValue(false);
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
                loading.setValue(false);
            }
        });
    }
}
