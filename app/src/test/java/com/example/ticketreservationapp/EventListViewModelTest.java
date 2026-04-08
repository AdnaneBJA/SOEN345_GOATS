package com.example.ticketreservationapp;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;

import com.example.ticketreservationapp.model.Event;
import com.example.ticketreservationapp.repository.EventRepository;
import com.example.ticketreservationapp.viewmodel.EventListViewModel;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EventListViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private static class FakeEventRepository extends EventRepository {
        List<Event> resultList = Collections.emptyList();
        String errorMessage = "boom";
        boolean shouldSucceed = true;
        boolean invokeCallback = true;
        int loadCalls = 0;

        FakeEventRepository() {
            super(null);
        }

        @Override
        public void getAllEvents(EventListCallback callback) {
            loadCalls++;
            if (!invokeCallback) {
                return;
            }
            if (shouldSucceed) {
                callback.onSuccess(resultList);
            } else {
                callback.onError(errorMessage);
            }
        }
    }

    private FakeEventRepository fakeRepository;
    private EventListViewModel viewModel;

    @Before
    public void setUp() {
        fakeRepository = new FakeEventRepository();
        viewModel = new EventListViewModel(fakeRepository);
    }

    private <T> List<T> collectValues(LiveData<T> liveData) {
        List<T> values = new ArrayList<>();
        liveData.observeForever(values::add);
        return values;
    }

    private Event event(String id, String title, String description, String date,
                        String location, String category) {
        return new Event(id, title, description, date, location, category,
                10.0, 25, 25, "org1", "Organizer");
    }

    @Test
    public void loadEvents_callsRepository() {
        fakeRepository.invokeCallback = false;

        viewModel.loadEvents();

        assertEquals(1, fakeRepository.loadCalls);
    }

    @Test
    public void loadEvents_setsLoadingTrueBeforeCallback() {
        fakeRepository.invokeCallback = false;
        List<Boolean> loadingStates = collectValues(viewModel.getLoading());

        viewModel.loadEvents();

        assertTrue(loadingStates.contains(true));
    }

    @Test
    public void loadEvents_success_postsAllEvents() {
        fakeRepository.resultList = Arrays.asList(
                event("1", "Jazz Night", "Live jazz", "2026-05-01", "Montreal", "Concerts"),
                event("2", "Soccer Final", "Championship", "2026-05-02", "Toronto", "Sports")
        );

        viewModel.loadEvents();

        assertEquals(2, viewModel.getFilteredEvents().getValue().size());
    }

    @Test
    public void setSearchQuery_filtersByTitleDescriptionAndLocation() {
        fakeRepository.resultList = Arrays.asList(
                event("1", "Jazz Night", "Live jazz", "2026-05-01", "Montreal", "Concerts"),
                event("2", "Soccer Final", "Championship", "2026-05-02", "Toronto", "Sports"),
                event("3", "Movie Premiere", "Drama night", "2026-05-03", "Laval", "Movies")
        );
        viewModel.loadEvents();

        viewModel.setSearchQuery("toronto");

        assertEquals(1, viewModel.getFilteredEvents().getValue().size());
        assertEquals("2", viewModel.getFilteredEvents().getValue().get(0).getId());
    }

    @Test
    public void setCategoryFilter_returnsOnlyMatchingCategory() {
        fakeRepository.resultList = Arrays.asList(
                event("1", "Jazz Night", "Live jazz", "2026-05-01", "Montreal", "Concerts"),
                event("2", "Soccer Final", "Championship", "2026-05-02", "Toronto", "Sports")
        );
        viewModel.loadEvents();

        viewModel.setCategoryFilter("Sports");

        assertEquals(1, viewModel.getFilteredEvents().getValue().size());
        assertEquals("Sports", viewModel.getFilteredEvents().getValue().get(0).getCategory());
    }

    @Test
    public void setLocationFilter_returnsOnlyMatchingLocation() {
        fakeRepository.resultList = Arrays.asList(
                event("1", "Jazz Night", "Live jazz", "2026-05-01", "Montreal", "Concerts"),
                event("2", "Soccer Final", "Championship", "2026-05-02", "Toronto", "Sports"),
                event("3", "Movie Premiere", "Drama night", "2026-05-03", "Laval", "Movies")
        );
        viewModel.loadEvents();

        viewModel.setLocationFilter("lav");

        assertEquals(1, viewModel.getFilteredEvents().getValue().size());
        assertEquals("3", viewModel.getFilteredEvents().getValue().get(0).getId());
    }

    @Test
    public void setDateFilter_returnsOnlyMatchingDate() {
        fakeRepository.resultList = Arrays.asList(
                event("1", "Jazz Night", "Live jazz", "2026-05-01", "Montreal", "Concerts"),
                event("2", "Soccer Final", "Championship", "2026-05-02", "Toronto", "Sports")
        );
        viewModel.loadEvents();

        viewModel.setDateFilter("2026-05-01");

        assertEquals(1, viewModel.getFilteredEvents().getValue().size());
        assertEquals("1", viewModel.getFilteredEvents().getValue().get(0).getId());
    }

    @Test
    public void combinedFilters_applyIntersectionOfSearchCategoryLocationAndDate() {
        fakeRepository.resultList = Arrays.asList(
                event("1", "Jazz Night", "Live jazz", "2026-05-01", "Montreal", "Concerts"),
                event("2", "Jazz Festival", "Outdoor event", "2026-05-01", "Montreal", "Concerts"),
                event("3", "Jazz Night", "Live jazz", "2026-05-01", "Toronto", "Concerts")
        );
        viewModel.loadEvents();

        viewModel.setSearchQuery("jazz");
        viewModel.setCategoryFilter("Concerts");
        viewModel.setLocationFilter("montreal");
        viewModel.setDateFilter("2026-05-01");

        assertEquals(2, viewModel.getFilteredEvents().getValue().size());
    }

    @Test
    public void clearFilters_restoresFullList() {
        fakeRepository.resultList = Arrays.asList(
                event("1", "Jazz Night", "Live jazz", "2026-05-01", "Montreal", "Concerts"),
                event("2", "Soccer Final", "Championship", "2026-05-02", "Toronto", "Sports")
        );
        viewModel.loadEvents();
        viewModel.setCategoryFilter("Sports");

        viewModel.clearFilters();

        assertEquals(2, viewModel.getFilteredEvents().getValue().size());
    }

    @Test
    public void loadEvents_error_postsErrorAndStopsLoading() {
        fakeRepository.shouldSucceed = false;
        fakeRepository.errorMessage = "network down";
        List<String> errors = collectValues(viewModel.getErrorMessage());

        viewModel.loadEvents();

        assertTrue(errors.contains("network down"));
        assertEquals(Boolean.FALSE, viewModel.getLoading().getValue());
    }
}
