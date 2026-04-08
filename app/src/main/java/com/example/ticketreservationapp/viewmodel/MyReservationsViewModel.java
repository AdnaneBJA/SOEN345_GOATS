package com.example.ticketreservationapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.ticketreservationapp.model.Reservation;
import com.example.ticketreservationapp.repository.ReservationRepository;

import java.util.List;

public class MyReservationsViewModel extends ViewModel {

    private final ReservationRepository repository;

    private final MutableLiveData<List<Reservation>> reservations = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> cancelSuccess = new MutableLiveData<>();

    public MyReservationsViewModel() {
        this.repository = new ReservationRepository();
    }

    public MyReservationsViewModel(ReservationRepository repository) {
        this.repository = repository;
    }

    public LiveData<List<Reservation>> getReservations() { return reservations; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<Boolean> getCancelSuccess() { return cancelSuccess; }

    public void loadReservations(String userId) {
        loading.setValue(true);
        repository.getReservationsByUser(userId, new ReservationRepository.ReservationListCallback() {
            @Override
            public void onSuccess(List<Reservation> list) {
                reservations.setValue(list);
                loading.setValue(false);
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
                loading.setValue(false);
            }
        });
    }

    public void cancelReservation(String reservationId) {
        loading.setValue(true);
        repository.cancelReservation(reservationId, new ReservationRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                cancelSuccess.setValue(true);
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
