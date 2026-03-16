package com.example.ticketreservationapp.model;

public class Event {
    private String id;
    private String title;
    private String description;
    private String date;
    private String location;
    private String category; // "Movies", "Concerts", "Travel", "Sports"
    private double price;
    private int availableSeats;
    private int totalSeats;
    private String organizerId;
    private String organizerName;

    // Required by Firestore deserialization
    public Event() {}

    public Event(String id, String title, String description, String date,
                 String location, String category, double price,
                 int availableSeats, int totalSeats,
                 String organizerId, String organizerName) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.date = date;
        this.location = location;
        this.category = category;
        this.price = price;
        this.availableSeats = availableSeats;
        this.totalSeats = totalSeats;
        this.organizerId = organizerId;
        this.organizerName = organizerName;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(int availableSeats) { this.availableSeats = availableSeats; }

    public int getTotalSeats() { return totalSeats; }
    public void setTotalSeats(int totalSeats) { this.totalSeats = totalSeats; }

    public String getOrganizerId() { return organizerId; }
    public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }

    public String getOrganizerName() { return organizerName; }
    public void setOrganizerName(String organizerName) { this.organizerName = organizerName; }
}
