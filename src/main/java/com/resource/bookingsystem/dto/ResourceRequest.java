package com.resource.bookingsystem.dto;

import com.resource.bookingsystem.entity.ResourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record ResourceRequest(
    @NotBlank(message = "Name is required")
    String name,

    @NotNull(message = "Type is required")
    ResourceType type,

    @NotBlank(message = "Location is required")
    String location,

    @NotNull(message = "Price per hour is required")
    @Positive(message = "Price per hour must be positive")
    BigDecimal pricePerHour,

    boolean available
) {
}
