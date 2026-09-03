package com.resource.bookingsystem.dto;

import com.resource.bookingsystem.entity.Reservation;
import com.resource.bookingsystem.entity.ReservationStatus;
import com.resource.bookingsystem.entity.ResourceType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReservationResponse(
    Long id,
    Long resourceId,
    String resourceName,
    ResourceType resourceType,
    Long userId,
    String userEmail,
    ReservationStatus status,
    BigDecimal price,
    LocalDateTime startTime,
    LocalDateTime endTime,
    LocalDateTime createdAt
) {
    public static ReservationResponse fromEntity(Reservation reservation) {
        return new ReservationResponse(
            reservation.getId(),
            reservation.getResource() != null ? reservation.getResource().getId() : null,
            reservation.getResource() != null ? reservation.getResource().getName() : null,
            reservation.getResource() != null ? reservation.getResource().getType() : null,
            reservation.getUser() != null ? reservation.getUser().getId() : null,
            reservation.getUser() != null ? reservation.getUser().getEmail() : null,
            reservation.getStatus(),
            reservation.getPrice(),
            reservation.getStartTime(),
            reservation.getEndTime(),
            reservation.getCreatedAt()
        );
    }
}
