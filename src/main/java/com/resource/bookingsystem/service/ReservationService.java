package com.resource.bookingsystem.service;

import com.resource.bookingsystem.dto.ReservationRequest;
import com.resource.bookingsystem.entity.Reservation;
import com.resource.bookingsystem.entity.ReservationStatus;
import com.resource.bookingsystem.entity.Resource;
import com.resource.bookingsystem.entity.User;
import com.resource.bookingsystem.repository.ReservationRepository;
import com.resource.bookingsystem.repository.ResourceRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ResourceRepository resourceRepository;

    public ReservationService(ReservationRepository reservationRepository, ResourceRepository resourceRepository) {
        this.reservationRepository = reservationRepository;
        this.resourceRepository = resourceRepository;
    }

    public Page<Reservation> getReservationsForAdmin(Pageable pageable, ReservationStatus status, BigDecimal minPrice, BigDecimal maxPrice) {
        List<Reservation> all = reservationRepository.findAll();
        return filterReservations(all, pageable, status, minPrice, maxPrice);
    }

    public Page<Reservation> getReservationsForUser(Long userId, Pageable pageable, ReservationStatus status, BigDecimal minPrice, BigDecimal maxPrice) {
        List<Reservation> byUser = reservationRepository.findByUserId(userId);
        return filterReservations(byUser, pageable, status, minPrice, maxPrice);
    }

    public Reservation findById(Long id) {
        return reservationRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found"));
    }

    public Reservation createReservation(Long userId, ReservationRequest request) {
        Resource resource = resourceRepository.findById(request.resourceId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found"));

        BigDecimal requestedPrice = request.price() != null ? request.price() : resource.getPricePerHour();
        ReservationStatus status = request.status() != null ? request.status() : ReservationStatus.PENDING;

        Reservation reservation = new Reservation(resource.getId(), userId, status, requestedPrice);
        return reservationRepository.save(reservation);
    }

    public Reservation updateReservation(Long id, ReservationRequest request, Long currentUserId, boolean isAdmin) {
        Reservation reservation = findById(id);

        if (!isAdmin && !reservation.getUserId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only update your own reservations");
        }

        if (request.resourceId() != null) {
            Resource resource = resourceRepository.findById(request.resourceId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found"));
            reservation.setResourceId(resource.getId());
        }

        if (request.status() != null) {
            reservation.setStatus(request.status());
        }

        if (request.price() != null) {
            reservation.setPrice(request.price());
        }

        return reservationRepository.save(reservation);
    }

    public void deleteReservation(Long id, Long currentUserId, boolean isAdmin) {
        Reservation reservation = findById(id);

        if (!isAdmin && !reservation.getUserId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only delete your own reservations");
        }

        reservationRepository.delete(reservation);
    }

    private Page<Reservation> filterReservations(List<Reservation> reservations, Pageable pageable,
                                               ReservationStatus status, BigDecimal minPrice, BigDecimal maxPrice) {
        List<Reservation> filtered = reservations.stream()
            .filter(r -> status == null || r.getStatus() == status)
            .filter(r -> minPrice == null || r.getPrice().compareTo(minPrice) >= 0)
            .filter(r -> maxPrice == null || r.getPrice().compareTo(maxPrice) <= 0)
            .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filtered.size());
        if (start >= filtered.size()) {
            return new org.springframework.data.domain.PageImpl<>(List.of(), pageable, filtered.size());
        }

        return new org.springframework.data.domain.PageImpl<>(filtered.subList(start, end), pageable, filtered.size());
    }
}
