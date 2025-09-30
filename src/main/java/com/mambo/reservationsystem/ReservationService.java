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

    public Reservation getReservationById(
        Long id
    ) {
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
}