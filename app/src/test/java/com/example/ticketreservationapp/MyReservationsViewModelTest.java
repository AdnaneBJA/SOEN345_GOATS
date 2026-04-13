package com.example.ticketreservationapp;

import androidx.lifecycle.LiveData;

import com.example.ticketreservationapp.model.Reservation;
import com.example.ticketreservationapp.repository.ReservationRepository;
import com.example.ticketreservationapp.viewmodel.MyReservationsViewModel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(InstantTaskExecutorExtension.class)
class MyReservationsViewModelTest {

    /** Hand-rolled fake — sidesteps Mockito/ByteBuddy JDK compatibility issues. */
    private static class FakeReservationRepository extends ReservationRepository {
        String lastUserId;
        String lastCancelledId;
        int loadCalls = 0;
        int cancelCalls = 0;

        // Programmable behaviour
        boolean shouldSucceed = true;
        List<Reservation> resultList = Collections.emptyList();
        String errorMessage = "boom";
        boolean invokeCallback = true;

        FakeReservationRepository() {
            // Pass null Firestore so we don't touch Firebase from JVM tests.
            super(null);
        }

        @Override
        public void getReservationsByUser(String userId, ReservationListCallback callback) {
            loadCalls++;
            lastUserId = userId;
            if (!invokeCallback) return;
            if (shouldSucceed) callback.onSuccess(resultList);
            else callback.onError(errorMessage);
        }

        @Override
        public void cancelReservation(String reservationId, SimpleCallback callback) {
            cancelCalls++;
            lastCancelledId = reservationId;
            if (!invokeCallback) return;
            if (shouldSucceed) callback.onSuccess();
            else callback.onError(errorMessage);
        }
    }

    private FakeReservationRepository fakeRepo;
    private MyReservationsViewModel viewModel;

    @BeforeEach
    void setUp() {
        fakeRepo = new FakeReservationRepository();
        viewModel = new MyReservationsViewModel(fakeRepo);
    }

    private <T> List<T> collectValues(LiveData<T> liveData) {
        List<T> values = new ArrayList<>();
        liveData.observeForever(values::add);
        return values;
    }

    // loadReservations ─────────────────────────────────────────────────────

    @Test
    void loadReservations_callsRepositoryWithUserId() {
        fakeRepo.invokeCallback = false;
        viewModel.loadReservations("user1");
        assertEquals(1, fakeRepo.loadCalls);
        assertEquals("user1", fakeRepo.lastUserId);
    }

    @Test
    void loadReservations_setsLoadingTrueBeforeCallback() {
        fakeRepo.invokeCallback = false;
        List<Boolean> loadingStates = collectValues(viewModel.getLoading());
        viewModel.loadReservations("user1");
        assertTrue(loadingStates.contains(true));
    }

    @Test
    void loadReservations_success_postsReservationsAndStopsLoading() {
        Reservation r = new Reservation(
                "user1", "event1", "Show", "2026-05-01",
                "Montreal", 2, 100.0, 1L, "CODE1");
        fakeRepo.resultList = Arrays.asList(r);

        viewModel.loadReservations("user1");

        assertEquals(fakeRepo.resultList, viewModel.getReservations().getValue());
        assertEquals(Boolean.FALSE, viewModel.getLoading().getValue());
    }

    @Test
    void loadReservations_emptySuccess_postsEmptyList() {
        fakeRepo.resultList = Collections.emptyList();
        viewModel.loadReservations("user1");
        assertNotNull(viewModel.getReservations().getValue());
        assertTrue(viewModel.getReservations().getValue().isEmpty());
    }

    @Test
    void loadReservations_error_postsErrorAndStopsLoading() {
        fakeRepo.shouldSucceed = false;
        fakeRepo.errorMessage = "network down";

        List<String> errors = collectValues(viewModel.getErrorMessage());
        viewModel.loadReservations("user1");

        assertTrue(errors.contains("network down"));
        assertEquals(Boolean.FALSE, viewModel.getLoading().getValue());
    }

    // cancelReservation ────────────────────────────────────────────────────

    @Test
    void cancelReservation_callsRepositoryWithReservationId() {
        fakeRepo.invokeCallback = false;
        viewModel.cancelReservation("res1");
        assertEquals(1, fakeRepo.cancelCalls);
        assertEquals("res1", fakeRepo.lastCancelledId);
    }

    @Test
    void cancelReservation_setsLoadingTrueBeforeCallback() {
        fakeRepo.invokeCallback = false;
        List<Boolean> loadingStates = collectValues(viewModel.getLoading());
        viewModel.cancelReservation("res1");
        assertTrue(loadingStates.contains(true));
    }

    @Test
    void cancelReservation_success_postsCancelSuccessTrue() {
        viewModel.cancelReservation("res1");
        assertEquals(Boolean.TRUE, viewModel.getCancelSuccess().getValue());
        assertEquals(Boolean.FALSE, viewModel.getLoading().getValue());
    }

    @Test
    void cancelReservation_error_postsErrorMessageAndStopsLoading() {
        fakeRepo.shouldSucceed = false;
        fakeRepo.errorMessage = "Reservation not found";

        List<String> errors = collectValues(viewModel.getErrorMessage());
        viewModel.cancelReservation("res1");

        assertTrue(errors.contains("Reservation not found"));
        assertEquals(Boolean.FALSE, viewModel.getLoading().getValue());
    }
}
