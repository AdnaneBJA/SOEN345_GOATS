package com.example.ticketreservationapp;

import androidx.lifecycle.LiveData;

import com.example.ticketreservationapp.model.Event;
import com.example.ticketreservationapp.repository.EventRepository;
import com.example.ticketreservationapp.viewmodel.EventListViewModel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(InstantTaskExecutorExtension.class)
class EventListViewModelTest {

    private static class FakeEventRepository extends EventRepository {
        int loadCalls = 0;
        boolean shouldSucceed = true;
        List<Event> resultList = Collections.emptyList();
        String errorMessage = "boom";
        boolean invokeCallback = true;

        FakeEventRepository() { super(null); }

        @Override
        public void getAllEvents(EventListCallback callback) {
            loadCalls++;
            if (!invokeCallback) return;
            if (shouldSucceed) callback.onSuccess(resultList);
            else callback.onError(errorMessage);
        }

        @Override
        public void getEventsByOrganizer(String organizerId, EventListCallback callback) {}
        @Override
        public void createEvent(Event event, EventCallback callback) {}
        @Override
        public void updateEvent(Event event, EventCallback callback) {}
        @Override
        public void deleteEvent(String eventId, EventCallback callback) {}
    }

    private FakeEventRepository fakeRepo;
    private EventListViewModel viewModel;

    private Event makeEvent(String title, String category, String location, String date) {
        return new Event(null, title, "Description of " + title, date,
                location, category, 25.0, 100, 100, "org1", "Organizer");
    }

    @BeforeEach
    void setUp() {
        fakeRepo = new FakeEventRepository();
        viewModel = new EventListViewModel(fakeRepo);
    }

    private <T> List<T> collectValues(LiveData<T> liveData) {
        List<T> values = new ArrayList<>();
        liveData.observeForever(values::add);
        return values;
    }

    // loadEvents ──────────────────────────────────────────────────────────

    @Test
    void loadEvents_callsRepository() {
        fakeRepo.invokeCallback = false;
        viewModel.loadEvents();
        assertEquals(1, fakeRepo.loadCalls);
    }

    @Test
    void loadEvents_setsLoadingTrueBeforeCallback() {
        fakeRepo.invokeCallback = false;
        List<Boolean> loadingStates = collectValues(viewModel.getLoading());
        viewModel.loadEvents();
        assertTrue(loadingStates.contains(true));
    }

    @Test
    void loadEvents_success_postsFilteredEventsAndStopsLoading() {
        Event e1 = makeEvent("Concert A", "Concerts", "Montreal", "2026-06-01");
        fakeRepo.resultList = Arrays.asList(e1);

        viewModel.loadEvents();

        List<Event> filtered = viewModel.getFilteredEvents().getValue();
        assertNotNull(filtered);
        assertEquals(1, filtered.size());
        assertEquals("Concert A", filtered.get(0).getTitle());
        assertEquals(Boolean.FALSE, viewModel.getLoading().getValue());
    }

    @Test
    void loadEvents_emptySuccess_postsEmptyList() {
        fakeRepo.resultList = Collections.emptyList();
        viewModel.loadEvents();
        assertNotNull(viewModel.getFilteredEvents().getValue());
        assertTrue(viewModel.getFilteredEvents().getValue().isEmpty());
    }

    @Test
    void loadEvents_error_postsErrorAndStopsLoading() {
        fakeRepo.shouldSucceed = false;
        fakeRepo.errorMessage = "network down";

        List<String> errors = collectValues(viewModel.getErrorMessage());
        viewModel.loadEvents();

        assertTrue(errors.contains("network down"));
        assertEquals(Boolean.FALSE, viewModel.getLoading().getValue());
    }

    // Search filter ───────────────────────────────────────────────────────

    @Test
    void setSearchQuery_filtersByTitle() {
        fakeRepo.resultList = Arrays.asList(
            makeEvent("Rock Concert", "Concerts", "Montreal", "2026-06-01"),
            makeEvent("Jazz Night", "Concerts", "Toronto", "2026-06-02")
        );
        viewModel.loadEvents();
        viewModel.setSearchQuery("Rock");

        List<Event> filtered = viewModel.getFilteredEvents().getValue();
        assertNotNull(filtered);
        assertEquals(1, filtered.size());
        assertEquals("Rock Concert", filtered.get(0).getTitle());
    }

    @Test
    void setSearchQuery_filtersByDescription() {
        Event e = makeEvent("Show", "Concerts", "Montreal", "2026-06-01");
        e.setDescription("A wonderful jazz performance");
        fakeRepo.resultList = Arrays.asList(e,
            makeEvent("Movie Night", "Movies", "Toronto", "2026-06-02"));
        viewModel.loadEvents();
        viewModel.setSearchQuery("jazz");

        List<Event> filtered = viewModel.getFilteredEvents().getValue();
        assertNotNull(filtered);
        assertEquals(1, filtered.size());
        assertEquals("Show", filtered.get(0).getTitle());
    }

    @Test
    void setSearchQuery_filtersByLocation() {
        fakeRepo.resultList = Arrays.asList(
            makeEvent("Event A", "Concerts", "Montreal", "2026-06-01"),
            makeEvent("Event B", "Concerts", "Toronto", "2026-06-02")
        );
        viewModel.loadEvents();
        viewModel.setSearchQuery("toronto");

        List<Event> filtered = viewModel.getFilteredEvents().getValue();
        assertNotNull(filtered);
        assertEquals(1, filtered.size());
        assertEquals("Event B", filtered.get(0).getTitle());
    }

    @Test
    void setSearchQuery_caseInsensitive() {
        fakeRepo.resultList = Arrays.asList(
            makeEvent("ROCK CONCERT", "Concerts", "Montreal", "2026-06-01")
        );
        viewModel.loadEvents();
        viewModel.setSearchQuery("rock");
        assertEquals(1, viewModel.getFilteredEvents().getValue().size());
    }

    @Test
    void setSearchQuery_emptyString_showsAllEvents() {
        fakeRepo.resultList = Arrays.asList(
            makeEvent("Event A", "Concerts", "Montreal", "2026-06-01"),
            makeEvent("Event B", "Movies", "Toronto", "2026-06-02")
        );
        viewModel.loadEvents();
        viewModel.setSearchQuery("Event A");
        assertEquals(1, viewModel.getFilteredEvents().getValue().size());
        viewModel.setSearchQuery("");
        assertEquals(2, viewModel.getFilteredEvents().getValue().size());
    }

    @Test
    void setSearchQuery_null_treatedAsEmpty() {
        fakeRepo.resultList = Arrays.asList(makeEvent("Event A", "Concerts", "Montreal", "2026-06-01"));
        viewModel.loadEvents();
        viewModel.setSearchQuery(null);
        assertEquals(1, viewModel.getFilteredEvents().getValue().size());
    }

    @Test
    void setSearchQuery_noMatchReturnsEmpty() {
        fakeRepo.resultList = Arrays.asList(makeEvent("Concert", "Concerts", "Montreal", "2026-06-01"));
        viewModel.loadEvents();
        viewModel.setSearchQuery("xyz_no_match");
        assertTrue(viewModel.getFilteredEvents().getValue().isEmpty());
    }

    // Category filter ─────────────────────────────────────────────────────

    @Test
    void setCategoryFilter_filtersByCategory() {
        fakeRepo.resultList = Arrays.asList(
            makeEvent("Concert", "Concerts", "Montreal", "2026-06-01"),
            makeEvent("Movie", "Movies", "Toronto", "2026-06-02"),
            makeEvent("Game", "Sports", "Ottawa", "2026-06-03")
        );
        viewModel.loadEvents();
        viewModel.setCategoryFilter("Movies");

        List<Event> filtered = viewModel.getFilteredEvents().getValue();
        assertNotNull(filtered);
        assertEquals(1, filtered.size());
        assertEquals("Movie", filtered.get(0).getTitle());
    }

    @Test
    void setCategoryFilter_caseInsensitive() {
        fakeRepo.resultList = Arrays.asList(makeEvent("Concert", "Concerts", "Montreal", "2026-06-01"));
        viewModel.loadEvents();
        viewModel.setCategoryFilter("concerts");
        assertEquals(1, viewModel.getFilteredEvents().getValue().size());
    }

    @Test
    void setCategoryFilter_emptyString_showsAll() {
        fakeRepo.resultList = Arrays.asList(
            makeEvent("Concert", "Concerts", "Montreal", "2026-06-01"),
            makeEvent("Movie", "Movies", "Toronto", "2026-06-02")
        );
        viewModel.loadEvents();
        viewModel.setCategoryFilter("");
        assertEquals(2, viewModel.getFilteredEvents().getValue().size());
    }

    @Test
    void setCategoryFilter_null_treatedAsEmpty() {
        fakeRepo.resultList = Arrays.asList(makeEvent("Concert", "Concerts", "Montreal", "2026-06-01"));
        viewModel.loadEvents();
        viewModel.setCategoryFilter(null);
        assertEquals(1, viewModel.getFilteredEvents().getValue().size());
    }

    // Location filter ─────────────────────────────────────────────────────

    @Test
    void setLocationFilter_filtersByLocation() {
        fakeRepo.resultList = Arrays.asList(
            makeEvent("Event A", "Concerts", "Montreal", "2026-06-01"),
            makeEvent("Event B", "Movies", "Toronto", "2026-06-02")
        );
        viewModel.loadEvents();
        viewModel.setLocationFilter("Montreal");

        List<Event> filtered = viewModel.getFilteredEvents().getValue();
        assertNotNull(filtered);
        assertEquals(1, filtered.size());
        assertEquals("Event A", filtered.get(0).getTitle());
    }

    @Test
    void setLocationFilter_partialMatch() {
        fakeRepo.resultList = Arrays.asList(
            makeEvent("Event A", "Concerts", "Montreal, QC", "2026-06-01"),
            makeEvent("Event B", "Movies", "Toronto, ON", "2026-06-02")
        );
        viewModel.loadEvents();
        viewModel.setLocationFilter("montreal");
        assertEquals(1, viewModel.getFilteredEvents().getValue().size());
    }

    @Test
    void setLocationFilter_null_treatedAsEmpty() {
        fakeRepo.resultList = Arrays.asList(makeEvent("Concert", "Concerts", "Montreal", "2026-06-01"));
        viewModel.loadEvents();
        viewModel.setLocationFilter(null);
        assertEquals(1, viewModel.getFilteredEvents().getValue().size());
    }

    // Date filter ─────────────────────────────────────────────────────────

    @Test
    void setDateFilter_filtersByExactDate() {
        fakeRepo.resultList = Arrays.asList(
            makeEvent("Event A", "Concerts", "Montreal", "2026-06-01"),
            makeEvent("Event B", "Movies", "Toronto", "2026-06-02")
        );
        viewModel.loadEvents();
        viewModel.setDateFilter("2026-06-01");

        List<Event> filtered = viewModel.getFilteredEvents().getValue();
        assertNotNull(filtered);
        assertEquals(1, filtered.size());
        assertEquals("Event A", filtered.get(0).getTitle());
    }

    @Test
    void setDateFilter_emptyString_showsAll() {
        fakeRepo.resultList = Arrays.asList(
            makeEvent("Event A", "Concerts", "Montreal", "2026-06-01"),
            makeEvent("Event B", "Movies", "Toronto", "2026-06-02")
        );
        viewModel.loadEvents();
        viewModel.setDateFilter("");
        assertEquals(2, viewModel.getFilteredEvents().getValue().size());
    }

    @Test
    void setDateFilter_null_treatedAsEmpty() {
        fakeRepo.resultList = Arrays.asList(makeEvent("Concert", "Concerts", "Montreal", "2026-06-01"));
        viewModel.loadEvents();
        viewModel.setDateFilter(null);
        assertEquals(1, viewModel.getFilteredEvents().getValue().size());
    }

    // Combined filters ────────────────────────────────────────────────────

    @Test
    void combinedFilters_searchAndCategory() {
        fakeRepo.resultList = Arrays.asList(
            makeEvent("Rock Concert", "Concerts", "Montreal", "2026-06-01"),
            makeEvent("Jazz Concert", "Concerts", "Toronto", "2026-06-02"),
            makeEvent("Rock Movie", "Movies", "Montreal", "2026-06-03")
        );
        viewModel.loadEvents();
        viewModel.setSearchQuery("Rock");
        viewModel.setCategoryFilter("Concerts");

        List<Event> filtered = viewModel.getFilteredEvents().getValue();
        assertNotNull(filtered);
        assertEquals(1, filtered.size());
        assertEquals("Rock Concert", filtered.get(0).getTitle());
    }

    @Test
    void combinedFilters_categoryAndDate() {
        fakeRepo.resultList = Arrays.asList(
            makeEvent("Concert A", "Concerts", "Montreal", "2026-06-01"),
            makeEvent("Concert B", "Concerts", "Toronto", "2026-06-02"),
            makeEvent("Movie A", "Movies", "Montreal", "2026-06-01")
        );
        viewModel.loadEvents();
        viewModel.setCategoryFilter("Concerts");
        viewModel.setDateFilter("2026-06-01");

        List<Event> filtered = viewModel.getFilteredEvents().getValue();
        assertNotNull(filtered);
        assertEquals(1, filtered.size());
        assertEquals("Concert A", filtered.get(0).getTitle());
    }

    @Test
    void combinedFilters_allFilters() {
        fakeRepo.resultList = Arrays.asList(
            makeEvent("Rock Concert", "Concerts", "Montreal", "2026-06-01"),
            makeEvent("Rock Concert", "Concerts", "Toronto", "2026-06-01"),
            makeEvent("Jazz Concert", "Concerts", "Montreal", "2026-06-01"),
            makeEvent("Rock Movie", "Movies", "Montreal", "2026-06-01")
        );
        viewModel.loadEvents();
        viewModel.setSearchQuery("Rock");
        viewModel.setCategoryFilter("Concerts");
        viewModel.setLocationFilter("Montreal");
        viewModel.setDateFilter("2026-06-01");

        List<Event> filtered = viewModel.getFilteredEvents().getValue();
        assertNotNull(filtered);
        assertEquals(1, filtered.size());
        assertEquals("Rock Concert", filtered.get(0).getTitle());
        assertEquals("Montreal", filtered.get(0).getLocation());
    }

    @Test
    void combinedFilters_noMatch_returnsEmpty() {
        fakeRepo.resultList = Arrays.asList(makeEvent("Concert", "Concerts", "Montreal", "2026-06-01"));
        viewModel.loadEvents();
        viewModel.setCategoryFilter("Movies");
        viewModel.setDateFilter("2026-12-25");

        List<Event> filtered = viewModel.getFilteredEvents().getValue();
        assertNotNull(filtered);
        assertTrue(filtered.isEmpty());
    }

    // Clear filters ───────────────────────────────────────────────────────

    @Test
    void clearFilters_resetsAllFiltersAndShowsAllEvents() {
        fakeRepo.resultList = Arrays.asList(
            makeEvent("Concert", "Concerts", "Montreal", "2026-06-01"),
            makeEvent("Movie", "Movies", "Toronto", "2026-06-02")
        );
        viewModel.loadEvents();
        viewModel.setSearchQuery("Concert");
        viewModel.setCategoryFilter("Concerts");
        viewModel.setLocationFilter("Montreal");
        viewModel.setDateFilter("2026-06-01");
        assertEquals(1, viewModel.getFilteredEvents().getValue().size());

        viewModel.clearFilters();
        assertEquals(2, viewModel.getFilteredEvents().getValue().size());
    }

    // Edge cases ──────────────────────────────────────────────────────────

    @Test
    void applyFilters_beforeLoadEvents_returnsEmptyList() {
        viewModel.setSearchQuery("something");
        List<Event> filtered = viewModel.getFilteredEvents().getValue();
        assertNotNull(filtered);
        assertTrue(filtered.isEmpty());
    }

    @Test
    void filter_eventWithNullTitle_doesNotCrash() {
        Event e = new Event();
        e.setCategory("Concerts");
        e.setLocation("Montreal");
        e.setDate("2026-06-01");
        fakeRepo.resultList = Arrays.asList(e);
        viewModel.loadEvents();
        viewModel.setSearchQuery("test");

        List<Event> filtered = viewModel.getFilteredEvents().getValue();
        assertNotNull(filtered);
        assertTrue(filtered.isEmpty());
    }

    @Test
    void filter_eventWithNullLocation_doesNotCrash() {
        Event e = new Event();
        e.setTitle("Concert");
        e.setCategory("Concerts");
        e.setDate("2026-06-01");
        fakeRepo.resultList = Arrays.asList(e);
        viewModel.loadEvents();
        viewModel.setLocationFilter("Montreal");

        List<Event> filtered = viewModel.getFilteredEvents().getValue();
        assertNotNull(filtered);
        assertTrue(filtered.isEmpty());
    }
}
