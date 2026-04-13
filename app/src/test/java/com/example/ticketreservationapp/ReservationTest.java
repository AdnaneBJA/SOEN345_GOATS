package com.example.ticketreservationapp;

import com.example.ticketreservationapp.model.Reservation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReservationTest {

    @Test
    void defaultConstructor_createsEmptyInstance() {
        Reservation r = new Reservation();
        assertNull(r.getId());
        assertNull(r.getUserId());
        assertNull(r.getEventId());
        assertEquals(0, r.getNumberOfTickets());
        assertEquals(0.0, r.getTotalPrice(), 0.0001);
    }

    @Test
    void parameterizedConstructor_setsAllFields() {
        Reservation r = new Reservation(
                "user1", "event1", "Concert", "2026-05-01",
                "Montreal", 3, 150.0, 1234567890L, "ABC123");

        assertEquals("user1", r.getUserId());
        assertEquals("event1", r.getEventId());
        assertEquals("Concert", r.getEventTitle());
        assertEquals("2026-05-01", r.getEventDate());
        assertEquals("Montreal", r.getEventLocation());
        assertEquals(3, r.getNumberOfTickets());
        assertEquals(150.0, r.getTotalPrice(), 0.0001);
        assertEquals(1234567890L, r.getCreatedAt());
        assertEquals("ABC123", r.getConfirmationCode());
    }

    @Test
    void setters_updateFields() {
        Reservation r = new Reservation();
        r.setId("res1");
        r.setUserId("u1");
        r.setEventId("e1");
        r.setEventTitle("Movie");
        r.setEventDate("2026-06-15");
        r.setEventLocation("Laval");
        r.setNumberOfTickets(2);
        r.setTotalPrice(40.0);
        r.setCreatedAt(999L);
        r.setConfirmationCode("XYZ789");

        assertEquals("res1", r.getId());
        assertEquals("u1", r.getUserId());
        assertEquals("e1", r.getEventId());
        assertEquals("Movie", r.getEventTitle());
        assertEquals("2026-06-15", r.getEventDate());
        assertEquals("Laval", r.getEventLocation());
        assertEquals(2, r.getNumberOfTickets());
        assertEquals(40.0, r.getTotalPrice(), 0.0001);
        assertEquals(999L, r.getCreatedAt());
        assertEquals("XYZ789", r.getConfirmationCode());
    }
}
