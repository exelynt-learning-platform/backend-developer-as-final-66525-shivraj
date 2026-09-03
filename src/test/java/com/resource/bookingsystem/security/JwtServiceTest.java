package com.resource.bookingsystem.security;

import com.resource.bookingsystem.config.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("test-secret-key-at-least-32-bytes-long-for-hmac-sha");
        properties.setExpirationMs(3600000L);
        jwtService = new JwtService(properties);
    }

    @Test
    @DisplayName("Should generate token and extract email and role accurately")
    void testTokenGenerationAndExtraction() {
        String token = jwtService.generateToken("test@example.com", "ADMIN");

        assertNotNull(token);
        assertEquals("test@example.com", jwtService.extractEmail(token));
        assertEquals("ADMIN", jwtService.extractRole(token));
        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    @DisplayName("Should return false for invalid token")
    void testInvalidToken() {
        assertFalse(jwtService.isTokenValid("invalid.jwt.token"));
        assertFalse(jwtService.isTokenValid(""));
        assertFalse(jwtService.isTokenValid(null));
    }

    @Test
    @DisplayName("Should return false for expired token")
    void testExpiredToken() {
        JwtProperties expiredProps = new JwtProperties();
        expiredProps.setSecret("test-secret-key-at-least-32-bytes-long-for-hmac-sha");
        expiredProps.setExpirationMs(-1000L); // already expired
        JwtService expiredJwtService = new JwtService(expiredProps);

        String expiredToken = expiredJwtService.generateToken("expired@example.com", "USER");
        assertFalse(expiredJwtService.isTokenValid(expiredToken));
    }
}
