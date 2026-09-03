package com.resource.bookingsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resource.bookingsystem.dto.ResourceRequest;
import com.resource.bookingsystem.entity.ResourceType;
import com.resource.bookingsystem.security.JwtService;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ResourceControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() {
        adminToken = "Bearer " + jwtService.generateToken("admin@resourcebooking.local", "ADMIN");
        userToken = "Bearer " + jwtService.generateToken("user@resourcebooking.local", "USER");
    }

    @Test
    @DisplayName("USER should be able to view resources (read-only)")
    void testUserCanViewResources() throws Exception {
        mockMvc.perform(get("/resources")
                .header("Authorization", userToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("USER should be forbidden from creating a resource")
    void testUserCannotCreateResource() throws Exception {
        ResourceRequest request = new ResourceRequest("Test Room", ResourceType.ROOM, "Floor 1", new BigDecimal("50.00"), true);

        mockMvc.perform(post("/resources")
                .header("Authorization", userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ADMIN should be authorized to create a resource")
    void testAdminCanCreateResource() throws Exception {
        ResourceRequest request = new ResourceRequest("Auditorium B", ResourceType.ROOM, "Floor 3", new BigDecimal("100.00"), true);

        mockMvc.perform(post("/resources")
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Auditorium B"))
            .andExpect(jsonPath("$.pricePerHour").value(100.00));
    }

    @Test
    @DisplayName("USER should be forbidden from updating a resource")
    void testUserCannotUpdateResource() throws Exception {
        ResourceRequest request = new ResourceRequest("Updated Room", ResourceType.ROOM, "Floor 1", new BigDecimal("60.00"), true);

        mockMvc.perform(put("/resources/1")
                .header("Authorization", userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ADMIN should be authorized to update a resource")
    void testAdminCanUpdateResource() throws Exception {
        ResourceRequest request = new ResourceRequest("Conference Room A - Updated", ResourceType.ROOM, "Floor 2", new BigDecimal("45.00"), true);

        mockMvc.perform(put("/resources/1")
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Conference Room A - Updated"));
    }

    @Test
    @DisplayName("USER should be forbidden from deleting a resource")
    void testUserCannotDeleteResource() throws Exception {
        mockMvc.perform(delete("/resources/1")
                .header("Authorization", userToken))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Unauthenticated request to /resources should be rejected")
    void testUnauthenticatedAccessRejected() throws Exception {
        mockMvc.perform(get("/resources"))
            .andExpect(status().isForbidden());
    }
}
