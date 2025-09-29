package com.mambo.reservationsystem;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record Reservation (
         Long id,
         Long userId,
         Long roomId,
         LocalDate startDate,
         LocalDateTime endDate,
         ReservationStatus reservationStatus
)  {
}