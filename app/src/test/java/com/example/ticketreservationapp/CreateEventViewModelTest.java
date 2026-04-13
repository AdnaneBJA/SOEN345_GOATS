package com.example.ticketreservationapp;

import androidx.lifecycle.LiveData;

import com.example.ticketreservationapp.model.Event;
import com.example.ticketreservationapp.repository.EventRepository;
import com.example.ticketreservationapp.viewmodel.CreateEventViewModel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(InstantTaskExecutorExtension.class)
class CreateEventViewModelTest {

    /** Hand-rolled fake — avoids Mockito/ByteBuddy JDK compatibility issues. */
    private static class FakeEventRepository extends EventRepository {
        int createCalls = 0;
        int updateCalls = 0;
        Event lastCreatedEvent;
        Event lastUpdatedEvent;
        boolean shouldSucceed = true;
        String errorMessage = "boom";
        boolean invokeCallback = true;

        FakeEventRepository() { super(null); }

        @Override
        public void createEvent(Event event, EventCallback callback) {
            createCalls++;
            lastCreatedEvent = event;
            if (!invokeCallback) return;
            if (shouldSucceed) callback.onSuccess();
            else callback.onError(errorMessage);
        }

        @Override
        public void updateEvent(Event event, EventCallback callback) {
            updateCalls++;
            lastUpdatedEvent = event;
            if (!invokeCallback) return;
            if (shouldSucceed) callback.onSuccess();
            else callback.onError(errorMessage);
        }

        @Override
        public void getAllEvents(EventListCallback callback) {}
        @Override
        public void getEventsByOrganizer(String id, EventListCallback callback) {}
        @Override
        public void deleteEvent(String id, EventCallback callback) {}
    }

    private FakeEventRepository fakeRepo;
    private CreateEventViewModel viewModel;

    @BeforeEach
    void setUp() {
        fakeRepo = new FakeEventRepository();
        viewModel = new CreateEventViewModel(fakeRepo);
    }

    private <T> List<T> collectValues(LiveData<T> liveData) {
        List<T> values = new ArrayList<>();
        liveData.observeForever(values::add);
        return values;
    }

    // createEvent validation ──────────────────────────────────────────────

    @Test
    void createEvent_nullTitle_postsError() {
        List<String> errors = collectValues(viewModel.getErrorMessage());
        viewModel.createEvent(null, "desc", "2026-06-01", "Montreal", "Concerts",
                "25.0", "100", "org1", "Organizer");
        assertEquals("Title is required.", errors.get(0));
    }

    @Test
    void createEvent_emptyTitle_postsError() {
        List<String> errors = collectValues(viewModel.getErrorMessage());
        viewModel.createEvent("  ", "desc", "2026-06-01", "Montreal", "Concerts",
                "25.0", "100", "org1", "Organizer");
        assertEquals("Title is required.", errors.get(0));
    }

    @Test
    void createEvent_emptyDescription_postsError() {
        List<String> errors = collectValues(viewModel.getErrorMessage());
        viewModel.createEvent("Title", "", "2026-06-01", "Montreal", "Concerts",
                "25.0", "100", "org1", "Organizer");
        assertEquals("Description is required.", errors.get(0));
    }

    @Test
    void createEvent_emptyDate_postsError() {
        List<String> errors = collectValues(viewModel.getErrorMessage());
        viewModel.createEvent("Title", "Desc", "", "Montreal", "Concerts",
                "25.0", "100", "org1", "Organizer");
        assertEquals("Date is required.", errors.get(0));
    }

    @Test
    void createEvent_emptyLocation_postsError() {
        List<String> errors = collectValues(viewModel.getErrorMessage());
        viewModel.createEvent("Title", "Desc", "2026-06-01", "", "Concerts",
                "25.0", "100", "org1", "Organizer");
        assertEquals("Location is required.", errors.get(0));
    }

    @Test
    void createEvent_emptyCategory_postsError() {
        List<String> errors = collectValues(viewModel.getErrorMessage());
        viewModel.createEvent("Title", "Desc", "2026-06-01", "Montreal", "",
                "25.0", "100", "org1", "Organizer");
        assertEquals("Category is required.", errors.get(0));
    }

    @Test
    void createEvent_invalidPrice_postsError() {
        List<String> errors = collectValues(viewModel.getErrorMessage());
        viewModel.createEvent("Title", "Desc", "2026-06-01", "Montreal", "Concerts",
                "abc", "100", "org1", "Organizer");
        assertEquals("Please enter a valid price.", errors.get(0));
    }

    @Test
    void createEvent_negativePrice_postsError() {
        List<String> errors = collectValues(viewModel.getErrorMessage());
        viewModel.createEvent("Title", "Desc", "2026-06-01", "Montreal", "Concerts",
                "-5.0", "100", "org1", "Organizer");
        assertEquals("Price cannot be negative.", errors.get(0));
    }

    @Test
    void createEvent_invalidSeats_postsError() {
        List<String> errors = collectValues(viewModel.getErrorMessage());
        viewModel.createEvent("Title", "Desc", "2026-06-01", "Montreal", "Concerts",
                "25.0", "abc", "org1", "Organizer");
        assertEquals("Please enter a valid number of seats.", errors.get(0));
    }

    @Test
    void createEvent_zeroSeats_postsError() {
        List<String> errors = collectValues(viewModel.getErrorMessage());
        viewModel.createEvent("Title", "Desc", "2026-06-01", "Montreal", "Concerts",
                "25.0", "0", "org1", "Organizer");
        assertEquals("Number of seats must be greater than zero.", errors.get(0));
    }

    @Test
    void createEvent_negativeSeats_postsError() {
        List<String> errors = collectValues(viewModel.getErrorMessage());
        viewModel.createEvent("Title", "Desc", "2026-06-01", "Montreal", "Concerts",
                "25.0", "-10", "org1", "Organizer");
        assertEquals("Number of seats must be greater than zero.", errors.get(0));
    }

    @Test
    void createEvent_validationFailure_doesNotCallRepository() {
        viewModel.createEvent("", "Desc", "2026-06-01", "Montreal", "Concerts",
                "25.0", "100", "org1", "Organizer");
        assertEquals(0, fakeRepo.createCalls);
    }

    // createEvent success ─────────────────────────────────────────────────

    @Test
    void createEvent_validInput_callsRepository() {
        viewModel.createEvent("Concert", "Great show", "2026-06-01", "Montreal",
                "Concerts", "25.0", "100", "org1", "Organizer");
        assertEquals(1, fakeRepo.createCalls);
        assertNotNull(fakeRepo.lastCreatedEvent);
        assertEquals("Concert", fakeRepo.lastCreatedEvent.getTitle());
    }

    @Test
    void createEvent_validInput_setsLoadingTrue() {
        fakeRepo.invokeCallback = false;
        List<Boolean> loadingStates = collectValues(viewModel.getLoading());
        viewModel.createEvent("Concert", "Great show", "2026-06-01", "Montreal",
                "Concerts", "25.0", "100", "org1", "Organizer");
        assertTrue(loadingStates.contains(true));
    }

    @Test
    void createEvent_success_postsCreateSuccessTrue() {
        viewModel.createEvent("Concert", "Great show", "2026-06-01", "Montreal",
                "Concerts", "25.0", "100", "org1", "Organizer");
        assertEquals(Boolean.TRUE, viewModel.getCreateSuccess().getValue());
        assertEquals(Boolean.FALSE, viewModel.getLoading().getValue());
    }

    @Test
    void createEvent_error_postsErrorMessage() {
        fakeRepo.shouldSucceed = false;
        fakeRepo.errorMessage = "Firestore error";

        List<String> errors = collectValues(viewModel.getErrorMessage());
        viewModel.createEvent("Concert", "Great show", "2026-06-01", "Montreal",
                "Concerts", "25.0", "100", "org1", "Organizer");

        assertTrue(errors.contains("Firestore error"));
        assertEquals(Boolean.FALSE, viewModel.getLoading().getValue());
    }

    @Test
    void createEvent_trimsInputFields() {
        viewModel.createEvent("  Concert  ", "  Great show  ", " 2026-06-01 ",
                " Montreal ", "Concerts", "25.0", "100", "org1", "Organizer");
        assertEquals("Concert", fakeRepo.lastCreatedEvent.getTitle());
        assertEquals("Great show", fakeRepo.lastCreatedEvent.getDescription());
        assertEquals("Montreal", fakeRepo.lastCreatedEvent.getLocation());
    }

    @Test
    void createEvent_zeroPriceAllowed() {
        viewModel.createEvent("Free Event", "desc", "2026-06-01", "Montreal",
                "Concerts", "0", "100", "org1", "Organizer");
        assertEquals(1, fakeRepo.createCalls);
        assertEquals(0.0, fakeRepo.lastCreatedEvent.getPrice(), 0.001);
    }

    // updateEvent validation ──────────────────────────────────────────────

    @Test
    void updateEvent_emptyTitle_postsError() {
        List<String> errors = collectValues(viewModel.getErrorMessage());
        viewModel.updateEvent("evt1", "", "desc", "2026-06-01", "Montreal",
                "Concerts", "25.0", "100", "org1", "Organizer");
        assertEquals("Title is required.", errors.get(0));
    }

    @Test
    void updateEvent_emptyDescription_postsError() {
        List<String> errors = collectValues(viewModel.getErrorMessage());
        viewModel.updateEvent("evt1", "Title", "", "2026-06-01", "Montreal",
                "Concerts", "25.0", "100", "org1", "Organizer");
        assertEquals("Description is required.", errors.get(0));
    }

    @Test
    void updateEvent_emptyDate_postsError() {
        List<String> errors = collectValues(viewModel.getErrorMessage());
        viewModel.updateEvent("evt1", "Title", "desc", "", "Montreal",
                "Concerts", "25.0", "100", "org1", "Organizer");
        assertEquals("Date is required.", errors.get(0));
    }

    @Test
    void updateEvent_emptyLocation_postsError() {
        List<String> errors = collectValues(viewModel.getErrorMessage());
        viewModel.updateEvent("evt1", "Title", "desc", "2026-06-01", "",
                "Concerts", "25.0", "100", "org1", "Organizer");
        assertEquals("Location is required.", errors.get(0));
    }

    @Test
    void updateEvent_emptyCategory_postsError() {
        List<String> errors = collectValues(viewModel.getErrorMessage());
        viewModel.updateEvent("evt1", "Title", "desc", "2026-06-01", "Montreal",
                "", "25.0", "100", "org1", "Organizer");
        assertEquals("Category is required.", errors.get(0));
    }

    @Test
    void updateEvent_negativePrice_postsError() {
        List<String> errors = collectValues(viewModel.getErrorMessage());
        viewModel.updateEvent("evt1", "Title", "desc", "2026-06-01", "Montreal",
                "Concerts", "-5.0", "100", "org1", "Organizer");
        assertEquals("Price cannot be negative.", errors.get(0));
    }

    @Test
    void updateEvent_invalidPrice_postsError() {
        List<String> errors = collectValues(viewModel.getErrorMessage());
        viewModel.updateEvent("evt1", "Title", "desc", "2026-06-01", "Montreal",
                "Concerts", "xyz", "100", "org1", "Organizer");
        assertEquals("Please enter a valid price.", errors.get(0));
    }

    @Test
    void updateEvent_invalidSeats_postsError() {
        List<String> errors = collectValues(viewModel.getErrorMessage());
        viewModel.updateEvent("evt1", "Title", "desc", "2026-06-01", "Montreal",
                "Concerts", "25.0", "abc", "org1", "Organizer");
        assertEquals("Please enter a valid number of seats.", errors.get(0));
    }

    @Test
    void updateEvent_zeroSeats_postsError() {
        List<String> errors = collectValues(viewModel.getErrorMessage());
        viewModel.updateEvent("evt1", "Title", "desc", "2026-06-01", "Montreal",
                "Concerts", "25.0", "0", "org1", "Organizer");
        assertEquals("Number of seats must be greater than zero.", errors.get(0));
    }

    @Test
    void updateEvent_validationFailure_doesNotCallRepository() {
        viewModel.updateEvent("evt1", "", "desc", "2026-06-01", "Montreal",
                "Concerts", "25.0", "100", "org1", "Organizer");
        assertEquals(0, fakeRepo.updateCalls);
    }

    // updateEvent success ─────────────────────────────────────────────────

    @Test
    void updateEvent_validInput_callsRepository() {
        viewModel.updateEvent("evt1", "Updated Title", "Updated desc", "2026-07-01",
                "Toronto", "Movies", "30.0", "200", "org1", "Organizer");
        assertEquals(1, fakeRepo.updateCalls);
        assertNotNull(fakeRepo.lastUpdatedEvent);
        assertEquals("evt1", fakeRepo.lastUpdatedEvent.getId());
        assertEquals("Updated Title", fakeRepo.lastUpdatedEvent.getTitle());
    }

    @Test
    void updateEvent_success_postsUpdateSuccessTrue() {
        viewModel.updateEvent("evt1", "Title", "desc", "2026-06-01", "Montreal",
                "Concerts", "25.0", "100", "org1", "Organizer");
        assertEquals(Boolean.TRUE, viewModel.getUpdateSuccess().getValue());
        assertEquals(Boolean.FALSE, viewModel.getLoading().getValue());
    }

    @Test
    void updateEvent_error_postsErrorMessage() {
        fakeRepo.shouldSucceed = false;
        fakeRepo.errorMessage = "Update failed";

        List<String> errors = collectValues(viewModel.getErrorMessage());
        viewModel.updateEvent("evt1", "Title", "desc", "2026-06-01", "Montreal",
                "Concerts", "25.0", "100", "org1", "Organizer");

        assertTrue(errors.contains("Update failed"));
        assertEquals(Boolean.FALSE, viewModel.getLoading().getValue());
    }
}
