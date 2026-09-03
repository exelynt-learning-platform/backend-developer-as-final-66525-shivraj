package com.resource.bookingsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resource.bookingsystem.dto.LoginRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should successfully authenticate admin and return JWT token")
    void testAdminLoginSuccess() throws Exception {
        LoginRequest request = new LoginRequest("admin@resourcebooking.local", "admin123");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.email").value("admin@resourcebooking.local"))
            .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    @DisplayName("Should successfully authenticate user and return JWT token")
    void testUserLoginSuccess() throws Exception {
        LoginRequest request = new LoginRequest("user@resourcebooking.local", "user123");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.email").value("user@resourcebooking.local"))
            .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @DisplayName("Should reject invalid password with 401 Unauthorized")
    void testLoginWithInvalidPassword() throws Exception {
        LoginRequest request = new LoginRequest("admin@resourcebooking.local", "wrongpassword");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.error").isNotEmpty());
    }

    @Test
    @DisplayName("Should reject request with missing fields with 400 Bad Request")
    void testLoginWithValidationErrors() throws Exception {
        LoginRequest request = new LoginRequest("", "");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Validation failed"));
    }
}
