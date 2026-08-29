package com.resource.bookingsystem.dto;

public record AuthResponse(String token, String tokenType, String email, String role) {
}
