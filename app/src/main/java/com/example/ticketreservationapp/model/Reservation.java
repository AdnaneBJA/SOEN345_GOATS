package com.example.ticketreservationapp.model;

public class Reservation {
    private String id;
    private String userId;
    private String eventId;
    private String eventTitle;
    private String eventDate;
    private String eventLocation;
    private int numberOfTickets;
    private double totalPrice;
    private long createdAt;
    private String confirmationCode;

    public Reservation() {}

    public Reservation(String userId, String eventId, String eventTitle, String eventDate,
                       String eventLocation, int numberOfTickets, double totalPrice,
                       long createdAt, String confirmationCode) {
        this.userId = userId;
        this.eventId = eventId;
        this.eventTitle = eventTitle;
        this.eventDate = eventDate;
        this.eventLocation = eventLocation;
        this.numberOfTickets = numberOfTickets;
        this.totalPrice = totalPrice;
        this.createdAt = createdAt;
        this.confirmationCode = confirmationCode;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public String getEventTitle() { return eventTitle; }
    public void setEventTitle(String eventTitle) { this.eventTitle = eventTitle; }
    public String getEventDate() { return eventDate; }
    public void setEventDate(String eventDate) { this.eventDate = eventDate; }
    public String getEventLocation() { return eventLocation; }
    public void setEventLocation(String eventLocation) { this.eventLocation = eventLocation; }
    public int getNumberOfTickets() { return numberOfTickets; }
    public void setNumberOfTickets(int numberOfTickets) { this.numberOfTickets = numberOfTickets; }
    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public String getConfirmationCode() { return confirmationCode; }
    public void setConfirmationCode(String confirmationCode) { this.confirmationCode = confirmationCode; }
}
