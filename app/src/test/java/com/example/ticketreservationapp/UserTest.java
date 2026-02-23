package com.example.ticketreservationapp;

import com.example.ticketreservationapp.model.User;

import org.junit.Test;

import static org.junit.Assert.*;


public class UserTest {

    @Test
    public void fullConstructor_setsAllFields() {
        User user = new User("uid1", "John Doe", "john@example.com", "+15551234567", "customer");

        assertEquals("uid1", user.getUid());
        assertEquals("John Doe", user.getFullName());
        assertEquals("john@example.com", user.getEmail());
        assertEquals("+15551234567", user.getPhone());
        assertEquals("customer", user.getRole());
    }

    @Test
    public void noArgConstructor_createsUserWithNullFields() {
        User user = new User();

        assertNull(user.getUid());
        assertNull(user.getFullName());
        assertNull(user.getEmail());
        assertNull(user.getPhone());
        assertNull(user.getRole());
    }

    @Test
    public void setUid_updatesValue() {
        User user = new User();
        user.setUid("abc-123");
        assertEquals("abc-123", user.getUid());
    }

    @Test
    public void setFullName_updatesValue() {
        User user = new User();
        user.setFullName("Jane Doe");
        assertEquals("Jane Doe", user.getFullName());
    }

    @Test
    public void setEmail_updatesValue() {
        User user = new User();
        user.setEmail("jane@example.com");
        assertEquals("jane@example.com", user.getEmail());
    }

    @Test
    public void setPhone_updatesValue() {
        User user = new User();
        user.setPhone("+14155552671");
        assertEquals("+14155552671", user.getPhone());
    }

    @Test
    public void setRole_updatesValue() {
        User user = new User();
        user.setRole("organizer");
        assertEquals("organizer", user.getRole());
    }

    @Test
    public void customerRole_isDistinctFromOrganizer() {
        User customer = new User("u1", "Alice", "a@x.com", "", "customer");
        User organizer = new User("u2", "Bob",   "b@x.com", "", "organizer");

        assertNotEquals(customer.getRole(), organizer.getRole());
    }

    @Test
    public void settersOverwriteConstructorValues() {
        User user = new User("old-uid", "Old Name", "old@x.com", "+10000000000", "customer");
        user.setUid("new-uid");
        user.setFullName("New Name");
        user.setEmail("new@x.com");
        user.setPhone("+19999999999");
        user.setRole("organizer");

        assertEquals("new-uid",       user.getUid());
        assertEquals("New Name",      user.getFullName());
        assertEquals("new@x.com",     user.getEmail());
        assertEquals("+19999999999",  user.getPhone());
        assertEquals("organizer",     user.getRole());
    }
}
