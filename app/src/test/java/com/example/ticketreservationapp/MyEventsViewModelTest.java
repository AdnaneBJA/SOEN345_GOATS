package com.example.ticketreservationapp;

import androidx.lifecycle.LiveData;

import com.example.ticketreservationapp.model.Event;
import com.example.ticketreservationapp.repository.EventRepository;
import com.example.ticketreservationapp.viewmodel.MyEventsViewModel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(InstantTaskExecutorExtension.class)
class MyEventsViewModelTest {

    /** Hand-rolled fake — avoids Mockito/ByteBuddy JDK compatibility issues. */
    private static class FakeEventRepository extends EventRepository {
        String lastOrganizerId;
        String lastDeletedEventId;
        int loadCalls = 0;
        int deleteCalls = 0;

        boolean shouldSucceed = true;
        List<Event> resultList = Collections.emptyList();
        String errorMessage = "boom";
        boolean invokeCallback = true;

        FakeEventRepository() { super(null); }

        @Override
        public void getEventsByOrganizer(String organizerId, EventListCallback callback) {
            loadCalls++;
            lastOrganizerId = organizerId;
            if (!invokeCallback) return;
            if (shouldSucceed) callback.onSuccess(resultList);
            else callback.onError(errorMessage);
        }

        @Override
        public void deleteEvent(String eventId, EventCallback callback) {
            deleteCalls++;
            lastDeletedEventId = eventId;
            if (!invokeCallback) return;
            if (shouldSucceed) callback.onSuccess();
            else callback.onError(errorMessage);
        }

        @Override
        public void getAllEvents(EventListCallback callback) {}
        @Override
        public void createEvent(Event event, EventCallback callback) {}
        @Override
        public void updateEvent(Event event, EventCallback callback) {}
    }

    private FakeEventRepository fakeRepo;
    private MyEventsViewModel viewModel;

    @BeforeEach
    void setUp() {
        fakeRepo = new FakeEventRepository();
        viewModel = new MyEventsViewModel(fakeRepo);
    }

    private <T> List<T> collectValues(LiveData<T> liveData) {
        List<T> values = new ArrayList<>();
        liveData.observeForever(values::add);
        return values;
    }

    // loadMyEvents ────────────────────────────────────────────────────────

    @Test
    void loadMyEvents_callsRepositoryWithOrganizerId() {
        fakeRepo.invokeCallback = false;
        viewModel.loadMyEvents("org1");
        assertEquals(1, fakeRepo.loadCalls);
        assertEquals("org1", fakeRepo.lastOrganizerId);
    }

    @Test
    void loadMyEvents_setsLoadingTrueBeforeCallback() {
        fakeRepo.invokeCallback = false;
        List<Boolean> loadingStates = collectValues(viewModel.getLoading());
        viewModel.loadMyEvents("org1");
        assertTrue(loadingStates.contains(true));
    }

    @Test
    void loadMyEvents_success_postsEventsAndStopsLoading() {
        Event e = new Event("evt1", "Concert", "A concert", "2026-06-01",
                "Montreal", "Concerts", 25.0, 100, 100, "org1", "Organizer");
        fakeRepo.resultList = Arrays.asList(e);

        viewModel.loadMyEvents("org1");

        List<Event> events = viewModel.getEvents().getValue();
        assertNotNull(events);
        assertEquals(1, events.size());
        assertEquals("Concert", events.get(0).getTitle());
        assertEquals(Boolean.FALSE, viewModel.getLoading().getValue());
    }

    @Test
    void loadMyEvents_emptySuccess_postsEmptyList() {
        fakeRepo.resultList = Collections.emptyList();
        viewModel.loadMyEvents("org1");
        assertNotNull(viewModel.getEvents().getValue());
        assertTrue(viewModel.getEvents().getValue().isEmpty());
    }

    @Test
    void loadMyEvents_error_postsErrorAndStopsLoading() {
        fakeRepo.shouldSucceed = false;
        fakeRepo.errorMessage = "Permission denied";

        List<String> errors = collectValues(viewModel.getErrorMessage());
        viewModel.loadMyEvents("org1");

        assertTrue(errors.contains("Permission denied"));
        assertEquals(Boolean.FALSE, viewModel.getLoading().getValue());
    }

    // deleteEvent ─────────────────────────────────────────────────────────

    @Test
    void deleteEvent_callsRepositoryWithEventId() {
        fakeRepo.invokeCallback = false;
        viewModel.deleteEvent("evt1");
        assertEquals(1, fakeRepo.deleteCalls);
        assertEquals("evt1", fakeRepo.lastDeletedEventId);
    }

    @Test
    void deleteEvent_setsLoadingTrueBeforeCallback() {
        fakeRepo.invokeCallback = false;
        List<Boolean> loadingStates = collectValues(viewModel.getLoading());
        viewModel.deleteEvent("evt1");
        assertTrue(loadingStates.contains(true));
    }

    @Test
    void deleteEvent_success_postsDeleteSuccessTrue() {
        viewModel.deleteEvent("evt1");
        assertEquals(Boolean.TRUE, viewModel.getDeleteSuccess().getValue());
        assertEquals(Boolean.FALSE, viewModel.getLoading().getValue());
    }

    @Test
    void deleteEvent_error_postsErrorAndStopsLoading() {
        fakeRepo.shouldSucceed = false;
        fakeRepo.errorMessage = "Event not found";

        List<String> errors = collectValues(viewModel.getErrorMessage());
        viewModel.deleteEvent("evt1");

        assertTrue(errors.contains("Event not found"));
        assertEquals(Boolean.FALSE, viewModel.getLoading().getValue());
    }
}
