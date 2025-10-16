package com.mambo.reservationsystem;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ReservationService {

    private final Map<Long, Reservation> reservationMap;
    private final AtomicLong idCounter;
    private final ReservationRepository repository;

    public ReservationService(ReservationRepository repository) {
        this.repository = repository;
        this.reservationMap = new HashMap<>();
        this.idCounter = new AtomicLong();
    }

    public Reservation getReservationById(Long id) {
        ReservationEntity entity = repository.findById(id)
               .orElseThrow(() ->
                       new EntityNotFoundException("Not found reservation with id: " + id)
               );

        return convertToReservation(entity);
    }

    public List<Reservation> findAllReservations() {

        List<ReservationEntity> allEntities = repository.findAll();

        return allEntities.stream()
                .map(this::convertToReservation).toList();
    }

    public Reservation createReservation(Reservation reservationToCreate) {
        if (reservationToCreate.id() != null) {
            throw new IllegalArgumentException("ID should be empty");
        }
        if (reservationToCreate.reservationStatus() != null) {
            throw new IllegalArgumentException("Status should be empty");
        }

        var entityToSave = new ReservationEntity(
                null,
                reservationToCreate.userId(),
                reservationToCreate.roomId(),
                reservationToCreate.startDate(),
                reservationToCreate.endDate(),
                ReservationStatus.PENDING
        );

        var savedEntity = repository.save(entityToSave);
        return convertToReservation(savedEntity);
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

    private Reservation convertToReservation(final ReservationEntity entity) {
        return new Reservation(
                entity.getId(),
                entity.getUserId(),
                entity.getRoomId(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getReservationStatus()
        );
    }
}