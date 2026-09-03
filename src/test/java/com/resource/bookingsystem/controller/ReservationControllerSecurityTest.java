package com.resource.bookingsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resource.bookingsystem.dto.ReservationRequest;
import com.resource.bookingsystem.entity.Reservation;
import com.resource.bookingsystem.entity.ReservationStatus;
import com.resource.bookingsystem.entity.Resource;
import com.resource.bookingsystem.entity.Role;
import com.resource.bookingsystem.entity.User;
import com.resource.bookingsystem.repository.ReservationRepository;
import com.resource.bookingsystem.repository.ResourceRepository;
import com.resource.bookingsystem.repository.UserRepository;
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
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReservationControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    private String adminToken;
    private String userToken;
    private String otherUserToken;
    private Long otherReservationId;
    private Long userReservationId;
    private Long testResourceId;

    @BeforeEach
    void setUp() {
        adminToken = "Bearer " + jwtService.generateToken("admin@resourcebooking.local", "ADMIN");
        userToken = "Bearer " + jwtService.generateToken("user@resourcebooking.local", "USER");

        User otherUser = userRepository.findByEmail("other@resourcebooking.local")
            .orElseGet(() -> userRepository.save(new User("other@resourcebooking.local", "pass123", Role.USER)));
        otherUserToken = "Bearer " + jwtService.generateToken("other@resourcebooking.local", "USER");

        User mainUser = userRepository.findByEmail("user@resourcebooking.local").orElseThrow();
        Resource resource = resourceRepository.findAll().stream().findFirst().orElseThrow();
        testResourceId = resource.getId();

        LocalDateTime start = LocalDateTime.now().plusDays(20);
        LocalDateTime end = start.plusHours(2);

        Reservation otherRes = reservationRepository.save(new Reservation(
            resource, otherUser, ReservationStatus.CONFIRMED, new BigDecimal("80.00"), start, end
        ));
        otherReservationId = otherRes.getId();

        LocalDateTime userStart = LocalDateTime.now().plusDays(25);
        LocalDateTime userEnd = userStart.plusHours(2);
        Reservation userRes = reservationRepository.save(new Reservation(
            resource, mainUser, ReservationStatus.PENDING, new BigDecimal("80.00"), userStart, userEnd
        ));
        userReservationId = userRes.getId();
    }

    @Test
    @DisplayName("USER can create a reservation with identity taken from JWT")
    void testUserCanCreateReservation() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusDays(30);
        LocalDateTime end = start.plusHours(2);
        ReservationRequest request = new ReservationRequest(testResourceId, ReservationStatus.PENDING, null, start, end);

        mockMvc.perform(post("/reservations")
                .header("Authorization", userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.userEmail").value("user@resourcebooking.local"))
            .andExpect(jsonPath("$.status").value("PENDING"))
            .andExpect(jsonPath("$.price").isNotEmpty());
    }

    @Test
    @DisplayName("USER should only view their own reservations")
    void testUserViewsOnlyOwnReservations() throws Exception {
        mockMvc.perform(get("/reservations")
                .header("Authorization", userToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[*].userEmail", everyItem(is("user@resourcebooking.local"))));
    }

    @Test
    @DisplayName("USER is forbidden from viewing another user's reservation")
    void testUserForbiddenFromViewingOtherReservation() throws Exception {
        mockMvc.perform(get("/reservations/" + otherReservationId)
                .header("Authorization", userToken))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ADMIN can view any reservation including other users'")
    void testAdminCanViewAnyReservation() throws Exception {
        mockMvc.perform(get("/reservations/" + otherReservationId)
                .header("Authorization", adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(otherReservationId))
            .andExpect(jsonPath("$.userEmail").value("other@resourcebooking.local"));
    }

    @Test
    @DisplayName("USER can cancel their own reservation")
    void testUserCanCancelOwnReservation() throws Exception {
        mockMvc.perform(put("/reservations/" + userReservationId + "/cancel")
                .header("Authorization", userToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("USER is forbidden from cancelling another user's reservation")
    void testUserCannotCancelOtherReservation() throws Exception {
        mockMvc.perform(put("/reservations/" + otherReservationId + "/cancel")
                .header("Authorization", userToken))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ADMIN can cancel any reservation")
    void testAdminCanCancelAnyReservation() throws Exception {
        mockMvc.perform(put("/reservations/" + otherReservationId + "/cancel")
                .header("Authorization", adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("USER is forbidden from deleting another user's reservation")
    void testUserCannotDeleteOtherReservation() throws Exception {
        mockMvc.perform(delete("/reservations/" + otherReservationId)
                .header("Authorization", userToken))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ADMIN can delete any reservation")
    void testAdminCanDeleteReservation() throws Exception {
        mockMvc.perform(delete("/reservations/" + otherReservationId)
                .header("Authorization", adminToken))
            .andExpect(status().isNoContent());
    }
}
