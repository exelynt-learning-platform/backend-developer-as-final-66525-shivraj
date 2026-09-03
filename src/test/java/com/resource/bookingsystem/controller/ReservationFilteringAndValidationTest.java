package com.resource.bookingsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resource.bookingsystem.dto.ReservationRequest;
import com.resource.bookingsystem.entity.ReservationStatus;
import com.resource.bookingsystem.entity.Resource;
import com.resource.bookingsystem.repository.ResourceRepository;
import com.resource.bookingsystem.security.JwtService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReservationFilteringAndValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ResourceRepository resourceRepository;

    private String adminToken;
    private Long resourceId;

    @BeforeEach
    void setUp() {
        adminToken = "Bearer " + jwtService.generateToken("admin@resourcebooking.local", "ADMIN");
        Resource resource = resourceRepository.findAll().stream().findFirst().orElseThrow();
        resourceId = resource.getId();
    }

    @Test
    @DisplayName("Should reject reservation when start time is after end time")
    void testStartTimeAfterEndTimeRejected() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusDays(5).withHour(15).withMinute(0);
        LocalDateTime end = LocalDateTime.now().plusDays(5).withHour(14).withMinute(0); // Before start!

        ReservationRequest request = new ReservationRequest(resourceId, ReservationStatus.PENDING, new BigDecimal("50.00"), start, end);

        mockMvc.perform(post("/reservations")
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Start time must be before end time"));
    }

    @Test
    @DisplayName("Should reject overlapping reservation with 409 Conflict")
    void testOverlappingReservationRejected() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusDays(10).withHour(10).withMinute(0);
        LocalDateTime end = LocalDateTime.now().plusDays(10).withHour(12).withMinute(0);

        ReservationRequest first = new ReservationRequest(resourceId, ReservationStatus.CONFIRMED, new BigDecimal("80.00"), start, end);

        mockMvc.perform(post("/reservations")
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(first)))
            .andExpect(status().isCreated());

        // Attempt overlapping reservation
        LocalDateTime overlappingStart = LocalDateTime.now().plusDays(10).withHour(11).withMinute(0);
        LocalDateTime overlappingEnd = LocalDateTime.now().plusDays(10).withHour(13).withMinute(0);

        ReservationRequest second = new ReservationRequest(resourceId, ReservationStatus.CONFIRMED, new BigDecimal("80.00"), overlappingStart, overlappingEnd);

        mockMvc.perform(post("/reservations")
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(second)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error").value("Resource is already reserved for the selected time range"));
    }

    @Test
    @DisplayName("Should filter reservations by status")
    void testFilterByStatus() throws Exception {
        mockMvc.perform(get("/reservations?status=CONFIRMED")
                .header("Authorization", adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[*].status", everyItem(is("CONFIRMED"))));
    }

    @Test
    @DisplayName("Should filter reservations by minimum price")
    void testFilterByMinPrice() throws Exception {
        mockMvc.perform(get("/reservations?minPrice=50.00")
                .header("Authorization", adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[*].price", everyItem(greaterThanOrEqualTo(50.0))));
    }

    @Test
    @DisplayName("Should filter reservations by maximum price")
    void testFilterByMaxPrice() throws Exception {
        mockMvc.perform(get("/reservations?maxPrice=150.00")
                .header("Authorization", adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[*].price", everyItem(lessThanOrEqualTo(150.0))));
    }

    @Test
    @DisplayName("Should support pagination parameters page and size")
    void testPagination() throws Exception {
        mockMvc.perform(get("/reservations?page=0&size=2")
                .header("Authorization", adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size").value(2))
            .andExpect(jsonPath("$.number").value(0));
    }

    @Test
    @DisplayName("Should support sorting by price descending")
    void testSorting() throws Exception {
        mockMvc.perform(get("/reservations?sortBy=price&direction=desc")
                .header("Authorization", adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray());
    }
}
