package com.example.ticketreservationapp.repository;

import androidx.annotation.NonNull;

import com.example.ticketreservationapp.model.Reservation;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class ReservationRepository {

    public interface ReservationCallback {
        void onSuccess(Reservation reservation);
        void onError(String message);
    }

    public interface SimpleCallback {
        void onSuccess();
        void onError(String message);
    }

    public interface ReservationListCallback {
        void onSuccess(List<Reservation> reservations);
        void onError(String message);
    }

    private final FirebaseFirestore firestore;

    public ReservationRepository() {
        this.firestore = FirebaseFirestore.getInstance();
    }

    public ReservationRepository(FirebaseFirestore firestore) {
        this.firestore = firestore;
    }

    /**
     * Atomically reserves tickets: checks seat availability and creates a reservation.
     */
    public void reserveTicket(String userId, String eventId, int numberOfTickets,
                              ReservationCallback callback) {
        if (numberOfTickets <= 0) {
            callback.onError("Number of tickets must be greater than zero");
            return;
        }
        DocumentReference eventRef = firestore.collection("events").document(eventId);
        DocumentReference reservationRef = firestore.collection("reservations").document();

        firestore.runTransaction(transaction -> {
            DocumentSnapshot eventSnap = transaction.get(eventRef);
            if (!eventSnap.exists()) {
                throw new IllegalStateException("Event no longer exists");
            }
            Long availableSeatsLong = eventSnap.getLong("availableSeats");
            int availableSeats = availableSeatsLong == null ? 0 : availableSeatsLong.intValue();
            if (availableSeats < numberOfTickets) {
                throw new IllegalStateException("Not enough seats available");
            }

            String title = eventSnap.getString("title");
            String date = eventSnap.getString("date");
            String location = eventSnap.getString("location");
            Double price = eventSnap.getDouble("price");
            double unitPrice = price == null ? 0 : price;

            Reservation reservation = new Reservation(
                    userId, eventId,
                    title == null ? "" : title,
                    date == null ? "" : date,
                    location == null ? "" : location,
                    numberOfTickets,
                    unitPrice * numberOfTickets,
                    System.currentTimeMillis(),
                    UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            reservation.setId(reservationRef.getId());

            transaction.update(eventRef, "availableSeats", availableSeats - numberOfTickets);
            transaction.set(reservationRef, reservation);
            return reservation;
        }).addOnSuccessListener(reservation -> {
            sendConfirmation(reservation);
            callback.onSuccess(reservation);
        }).addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /**
     * Queues a confirmation message to be delivered by the Firebase "Trigger Email"
     * and "Send SMS" extensions. Writing to these collections is enough — the
     * extensions watch them and dispatch via SMTP / Twilio respectively.
     */
    private void sendConfirmation(Reservation reservation) {
        com.google.firebase.auth.FirebaseUser user =
                com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String subject = "Reservation confirmed: " + reservation.getEventTitle();
        String text = String.format(Locale.US,
                "Your reservation is confirmed.%n%n" +
                        "Event: %s%nDate: %s%nLocation: %s%n" +
                        "Tickets: %d%nTotal: $%.2f%n%n" +
                        "Confirmation code: %s",
                reservation.getEventTitle(),
                reservation.getEventDate(),
                reservation.getEventLocation(),
                reservation.getNumberOfTickets(),
                reservation.getTotalPrice(),
                reservation.getConfirmationCode());

        // Email path — Firebase "Trigger Email" extension watches `mail/`.
        String email = user.getEmail();
        if (email != null && !email.isEmpty()) {
            Map<String, Object> message = new HashMap<>();
            message.put("subject", subject);
            message.put("text", text);

            Map<String, Object> mail = new HashMap<>();
            mail.put("to", email);
            mail.put("message", message);
            mail.put("reservationId", reservation.getId());
            firestore.collection("mail").add(mail);
        }

        // SMS path — Firebase "Send SMS" (Twilio) extension watches `messages/`.
        String phone = user.getPhoneNumber();
        if (phone != null && !phone.isEmpty()) {
            Map<String, Object> sms = new HashMap<>();
            sms.put("to", phone);
            sms.put("body", text);
            sms.put("reservationId", reservation.getId());
            firestore.collection("messages").add(sms);
        }
    }

    /**
     * Cancels a reservation and restores seat availability atomically.
     */
    public void cancelReservation(@NonNull String reservationId, SimpleCallback callback) {
        DocumentReference reservationRef = firestore.collection("reservations").document(reservationId);

        firestore.runTransaction(transaction -> {
            DocumentSnapshot reservationSnap = transaction.get(reservationRef);
            if (!reservationSnap.exists()) {
                throw new IllegalStateException("Reservation not found");
            }
            String eventId = reservationSnap.getString("eventId");
            Long ticketsLong = reservationSnap.getLong("numberOfTickets");
            int tickets = ticketsLong == null ? 0 : ticketsLong.intValue();

            if (eventId != null) {
                DocumentReference eventRef = firestore.collection("events").document(eventId);
                DocumentSnapshot eventSnap = transaction.get(eventRef);
                if (eventSnap.exists()) {
                    Long availableSeatsLong = eventSnap.getLong("availableSeats");
                    int availableSeats = availableSeatsLong == null ? 0 : availableSeatsLong.intValue();
                    transaction.update(eventRef, "availableSeats", availableSeats + tickets);
                }
            }
            transaction.delete(reservationRef);
            return null;
        }).addOnSuccessListener(aVoid -> callback.onSuccess())
          .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void getReservationsByUser(String userId, ReservationListCallback callback) {
        firestore.collection("reservations")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Reservation> reservations = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Reservation reservation = doc.toObject(Reservation.class);
                        reservation.setId(doc.getId());
                        reservations.add(reservation);
                    }
                    callback.onSuccess(reservations);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
}
