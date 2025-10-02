package com.mambo.reservationsystem;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ReservationService {

    private final Map<Long, Reservation> reservationMap;
    private final AtomicLong idCounter;

    public ReservationService() {
        this.reservationMap = new HashMap<>();
        this.idCounter = new AtomicLong();
    }

    public Reservation getReservationById(Long id) throws NoSuchElementException {
       if (!reservationMap.containsKey(id)) {
           throw new NoSuchElementException("Not found reservation with id: " + id);
       }

       return reservationMap.get(id);
    }

    public List<Reservation> findAllReservations() {
        return reservationMap.values().stream().toList();
    }

    public Reservation createReservation(Reservation reservationToCreate) {
        if (reservationToCreate.id() != null) {
            throw new IllegalArgumentException("ID should be empty");
        }
        if (reservationToCreate.reservationStatus() != null) {
            throw new IllegalArgumentException("Status should be empty");
        }

        var newReservation = new Reservation(
                idCounter.incrementAndGet(),
                reservationToCreate.userId(),
                reservationToCreate.roomId(),
                reservationToCreate.startDate(),
                reservationToCreate.endDate(),
                ReservationStatus.PENDING
        );

        reservationMap.put(newReservation.id(), newReservation);
        return newReservation;
    }

    public Reservation updateReservationById(Long id, Reservation reservationToUpdate) {
        if (!reservationMap.containsKey(id)) {
            throw new NoSuchElementException("Not found reservation with id: " + id);
        }

        var reservation = reservationMap.get(id);

        if (reservation.reservationStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("Cannot modify reservation status=" + reservation.reservationStatus());
        }

        var updatedReservation = new Reservation(
                reservation.id(),
                reservationToUpdate.userId(),
                reservationToUpdate.roomId(),
                reservationToUpdate.startDate(),
                reservationToUpdate.endDate(),
                ReservationStatus.PENDING
        );

        reservationMap.put(reservation.id(), updatedReservation);
        return updatedReservation;
    }

    public void deleteReservationById(Long id) {
        if (!reservationMap.containsKey(id)) {
            throw new NoSuchElementException("Not found reservation with id: " + id);
        }

         reservationMap.remove(id);
    }

    public Reservation approveReservation(Long id) {
        if (!reservationMap.containsKey(id)) {
            throw new NoSuchElementException("Not found reservation with id: " + id);
        }

        var reservation = reservationMap.get(id);

        if (reservation.reservationStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("Cannot modify reservation status=" + reservation.reservationStatus());
        }
        if (isReservationConflict(reservation)) {
            throw new IllegalStateException("Cannot approve reservation due to a conflict");
        }

        var approvedReservation = new Reservation(
                reservation.id(),
                reservation.userId(),
                reservation.roomId(),
                reservation.startDate(),
                reservation.endDate(),
                ReservationStatus.APPROVED
        );

        reservationMap.put(reservation.id(), approvedReservation);
        return approvedReservation;
    }

    private boolean isReservationConflict(
            Reservation reservation
    ) {
         return reservationMap.values().stream()
                 .filter(res -> !reservation.id().equals(res.id()))
                 .filter(res -> !reservation.roomId().equals(res.roomId()))
                 .filter(res -> !reservation.reservationStatus().equals(ReservationStatus.APPROVED))
                 .anyMatch(res -> reservation.startDate().isBefore(res.startDate())
                         && res.startDate().isBefore(reservation.endDate()));
    }
}