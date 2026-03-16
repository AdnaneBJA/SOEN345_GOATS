package com.example.ticketreservationapp.repository;

import com.example.ticketreservationapp.model.Event;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventRepository {

    public interface EventListCallback {
        void onSuccess(List<Event> events);
        void onError(String message);
    }

    public interface EventCallback {
        void onSuccess();
        void onError(String message);
    }

    private final FirebaseFirestore firestore;

    public EventRepository() {
        this.firestore = FirebaseFirestore.getInstance();
    }

    public EventRepository(FirebaseFirestore firestore) {
        this.firestore = firestore;
    }

    public void getAllEvents(EventListCallback callback) {
        firestore.collection("events")
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<Event> events = new ArrayList<>();
                for (QueryDocumentSnapshot doc : querySnapshot) {
                    Event event = doc.toObject(Event.class);
                    event.setId(doc.getId());
                    events.add(event);
                }
                callback.onSuccess(events);
            })
            .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void getEventsByOrganizer(String organizerId, EventListCallback callback) {
        firestore.collection("events")
            .whereEqualTo("organizerId", organizerId)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<Event> events = new ArrayList<>();
                for (QueryDocumentSnapshot doc : querySnapshot) {
                    Event event = doc.toObject(Event.class);
                    event.setId(doc.getId());
                    events.add(event);
                }
                callback.onSuccess(events);
            })
            .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void createEvent(Event event, EventCallback callback) {
        firestore.collection("events")
            .add(event)
            .addOnSuccessListener(docRef -> {
                event.setId(docRef.getId());
                callback.onSuccess();
            })
            .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void updateEvent(Event event, EventCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("title", event.getTitle());
        updates.put("description", event.getDescription());
        updates.put("date", event.getDate());
        updates.put("location", event.getLocation());
        updates.put("category", event.getCategory());
        updates.put("price", event.getPrice());
        updates.put("availableSeats", event.getAvailableSeats());
        updates.put("totalSeats", event.getTotalSeats());

        firestore.collection("events").document(event.getId())
            .update(updates)
            .addOnSuccessListener(aVoid -> callback.onSuccess())
            .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void deleteEvent(String eventId, EventCallback callback) {
        firestore.collection("events").document(eventId)
            .delete()
            .addOnSuccessListener(aVoid -> callback.onSuccess())
            .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
}
