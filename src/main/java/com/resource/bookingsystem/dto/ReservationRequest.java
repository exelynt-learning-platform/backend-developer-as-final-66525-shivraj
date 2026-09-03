package com.resource.bookingsystem.dto;

import com.resource.bookingsystem.entity.ReservationStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReservationRequest(
    @NotNull(message = "Resource ID is required")
    Long resourceId,

    ReservationStatus status,

    @Positive(message = "Price must be positive")
    BigDecimal price,

    @NotNull(message = "Start time is required")
    LocalDateTime startTime,

    @NotNull(message = "End time is required")
    LocalDateTime endTime
) {
}
