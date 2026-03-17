package com.example.ticketreservationapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.ticketreservationapp.model.Event;
import com.example.ticketreservationapp.repository.EventRepository;

import java.util.List;

public class MyEventsViewModel extends ViewModel {

    private final EventRepository repository;

    private final MutableLiveData<List<Event>> events = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> deleteSuccess = new MutableLiveData<>();

    public MyEventsViewModel() {
        this.repository = new EventRepository();
    }

    public MyEventsViewModel(EventRepository repository) {
        this.repository = repository;
    }

    public LiveData<List<Event>> getEvents() { return events; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<Boolean> getDeleteSuccess() { return deleteSuccess; }

    public void loadMyEvents(String organizerId) {
        loading.setValue(true);
        repository.getEventsByOrganizer(organizerId, new EventRepository.EventListCallback() {
            @Override
            public void onSuccess(List<Event> eventList) {
                events.setValue(eventList);
                loading.setValue(false);
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
                loading.setValue(false);
            }
        });
    }

    public void deleteEvent(String eventId) {
        loading.setValue(true);
        repository.deleteEvent(eventId, new EventRepository.EventCallback() {
            @Override
            public void onSuccess() {
                deleteSuccess.setValue(true);
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
