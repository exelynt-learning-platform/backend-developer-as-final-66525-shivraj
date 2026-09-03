package com.resource.bookingsystem.controller;

import com.resource.bookingsystem.dto.ReservationRequest;
import com.resource.bookingsystem.dto.ReservationResponse;
import com.resource.bookingsystem.entity.ReservationStatus;
import com.resource.bookingsystem.entity.Role;
import com.resource.bookingsystem.entity.User;
import com.resource.bookingsystem.service.AuthService;
import com.resource.bookingsystem.service.ReservationService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;
    private final AuthService authService;

    public ReservationController(ReservationService reservationService, AuthService authService) {
        this.reservationService = reservationService;
        this.authService = authService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Page<ReservationResponse>> getReservations(
        @RequestParam(required = false) ReservationStatus status,
        @RequestParam(required = false) BigDecimal minPrice,
        @RequestParam(required = false) BigDecimal maxPrice,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @RequestParam(defaultValue = "desc") String direction
    ) {
        User currentUser = authService.getCurrentUser();
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        PageRequest pageable = PageRequest.of(page, size, sort);

        if (currentUser.getRole() == Role.ADMIN) {
            return ResponseEntity.ok(reservationService.getReservationsForAdmin(pageable, status, minPrice, maxPrice));
        }

        return ResponseEntity.ok(reservationService.getReservationsForUser(currentUser.getId(), pageable, status, minPrice, maxPrice));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<ReservationResponse> getReservationById(@PathVariable Long id) {
        User currentUser = authService.getCurrentUser();
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        return ResponseEntity.ok(reservationService.getReservationById(id, currentUser.getId(), isAdmin));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<ReservationResponse> createReservation(@Valid @RequestBody ReservationRequest request) {
        User currentUser = authService.getCurrentUser();
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(reservationService.createReservation(currentUser.getId(), request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<ReservationResponse> updateReservation(@PathVariable Long id,
                                                                 @Valid @RequestBody ReservationRequest request) {
        User currentUser = authService.getCurrentUser();
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        return ResponseEntity.ok(reservationService.updateReservation(id, request, currentUser.getId(), isAdmin));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<ReservationResponse> cancelReservation(@PathVariable Long id) {
        User currentUser = authService.getCurrentUser();
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        return ResponseEntity.ok(reservationService.cancelReservation(id, currentUser.getId(), isAdmin));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        User currentUser = authService.getCurrentUser();
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        reservationService.deleteReservation(id, currentUser.getId(), isAdmin);
        return ResponseEntity.noContent().build();
    }
}
