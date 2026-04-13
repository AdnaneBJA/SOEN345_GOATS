package com.example.ticketreservationapp;

import com.example.ticketreservationapp.model.Event;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EventTest {

    @Test
    void noArgConstructor_createsInstanceWithNullFields() {
        Event event = new Event();
        assertNull(event.getId());
        assertNull(event.getTitle());
        assertNull(event.getDescription());
        assertNull(event.getDate());
        assertNull(event.getLocation());
        assertNull(event.getCategory());
        assertEquals(0.0, event.getPrice(), 0.001);
        assertEquals(0, event.getAvailableSeats());
        assertEquals(0, event.getTotalSeats());
        assertNull(event.getOrganizerId());
        assertNull(event.getOrganizerName());
    }

    @Test
    void fullConstructor_setsAllFields() {
        Event event = new Event("e1", "Concert", "Live show", "2026-06-01",
                "Montreal", "Concerts", 49.99, 80, 100, "org1", "Jane Doe");

        assertEquals("e1", event.getId());
        assertEquals("Concert", event.getTitle());
        assertEquals("Live show", event.getDescription());
        assertEquals("2026-06-01", event.getDate());
        assertEquals("Montreal", event.getLocation());
        assertEquals("Concerts", event.getCategory());
        assertEquals(49.99, event.getPrice(), 0.001);
        assertEquals(80, event.getAvailableSeats());
        assertEquals(100, event.getTotalSeats());
        assertEquals("org1", event.getOrganizerId());
        assertEquals("Jane Doe", event.getOrganizerName());
    }

    @Test
    void setters_overrideConstructorValues() {
        Event event = new Event("e1", "Old Title", "Old Desc", "2026-01-01",
                "Old City", "Movies", 10.0, 50, 50, "org1", "Old Org");

        event.setId("e2");
        event.setTitle("New Title");
        event.setDescription("New Desc");
        event.setDate("2026-12-25");
        event.setLocation("New City");
        event.setCategory("Sports");
        event.setPrice(99.99);
        event.setAvailableSeats(200);
        event.setTotalSeats(300);
        event.setOrganizerId("org2");
        event.setOrganizerName("New Org");

        assertEquals("e2", event.getId());
        assertEquals("New Title", event.getTitle());
        assertEquals("New Desc", event.getDescription());
        assertEquals("2026-12-25", event.getDate());
        assertEquals("New City", event.getLocation());
        assertEquals("Sports", event.getCategory());
        assertEquals(99.99, event.getPrice(), 0.001);
        assertEquals(200, event.getAvailableSeats());
        assertEquals(300, event.getTotalSeats());
        assertEquals("org2", event.getOrganizerId());
        assertEquals("New Org", event.getOrganizerName());
    }

    @Test
    void availableSeats_canBeDifferentFromTotalSeats() {
        Event event = new Event("e1", "Show", "desc", "2026-06-01",
                "Montreal", "Concerts", 25.0, 30, 100, "org1", "Org");
        assertEquals(30, event.getAvailableSeats());
        assertEquals(100, event.getTotalSeats());
    }

    @Test
    void price_canBeZero() {
        Event event = new Event("e1", "Free Event", "desc", "2026-06-01",
                "Montreal", "Concerts", 0.0, 100, 100, "org1", "Org");
        assertEquals(0.0, event.getPrice(), 0.001);
    }
}
