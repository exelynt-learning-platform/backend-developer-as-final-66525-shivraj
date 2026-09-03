package com.resource.bookingsystem.service;

import com.resource.bookingsystem.dto.ReservationRequest;
import com.resource.bookingsystem.dto.ReservationResponse;
import com.resource.bookingsystem.entity.Reservation;
import com.resource.bookingsystem.entity.ReservationStatus;
import com.resource.bookingsystem.entity.Resource;
import com.resource.bookingsystem.entity.User;
import com.resource.bookingsystem.repository.ReservationRepository;
import com.resource.bookingsystem.repository.ReservationSpecification;
import com.resource.bookingsystem.repository.ResourceRepository;
import com.resource.bookingsystem.repository.UserRepository;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              ResourceRepository resourceRepository,
                              UserRepository userRepository) {
        this.reservationRepository = reservationRepository;
        this.resourceRepository = resourceRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Page<ReservationResponse> getReservationsForAdmin(Pageable pageable, ReservationStatus status,
                                                            BigDecimal minPrice, BigDecimal maxPrice) {
        Specification<Reservation> spec = ReservationSpecification.filter(null, status, minPrice, maxPrice);
        return reservationRepository.findAll(spec, pageable).map(ReservationResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<ReservationResponse> getReservationsForUser(Long userId, Pageable pageable, ReservationStatus status,
                                                           BigDecimal minPrice, BigDecimal maxPrice) {
        Specification<Reservation> spec = ReservationSpecification.filter(userId, status, minPrice, maxPrice);
        return reservationRepository.findAll(spec, pageable).map(ReservationResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public Reservation findEntityById(Long id) {
        return reservationRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found"));
    }

    @Transactional(readOnly = true)
    public ReservationResponse getReservationById(Long id, Long currentUserId, boolean isAdmin) {
        Reservation reservation = findEntityById(id);
        if (!isAdmin && !reservation.getUser().getId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: You can only view your own reservations");
        }
        return ReservationResponse.fromEntity(reservation);
    }

    public ReservationResponse createReservation(Long currentUserId, ReservationRequest request) {
        validateReservationTimes(request.startTime(), request.endTime());

        Resource resource = resourceRepository.findById(request.resourceId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found"));

        if (!resource.isAvailable()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Resource is currently not available for booking");
        }

        boolean hasOverlap = reservationRepository.existsOverlappingReservation(
            resource.getId(), request.startTime(), request.endTime(), ReservationStatus.CANCELLED, null
        );
        if (hasOverlap) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Resource is already reserved for the selected time range");
        }

        User user = userRepository.findById(currentUserId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        BigDecimal finalPrice = calculatePrice(resource, request.price(), request.startTime(), request.endTime());
        ReservationStatus status = request.status() != null ? request.status() : ReservationStatus.PENDING;

        Reservation reservation = new Reservation(resource, user, status, finalPrice, request.startTime(), request.endTime());
        Reservation saved = reservationRepository.save(reservation);
        return ReservationResponse.fromEntity(saved);
    }

    public ReservationResponse updateReservation(Long id, ReservationRequest request, Long currentUserId, boolean isAdmin) {
        Reservation reservation = findEntityById(id);

        if (!isAdmin && !reservation.getUser().getId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only update your own reservations");
        }

        LocalDateTime newStart = request.startTime() != null ? request.startTime() : reservation.getStartTime();
        LocalDateTime newEnd = request.endTime() != null ? request.endTime() : reservation.getEndTime();
        validateReservationTimes(newStart, newEnd);

        Resource targetResource = reservation.getResource();
        if (request.resourceId() != null && !request.resourceId().equals(targetResource.getId())) {
            targetResource = resourceRepository.findById(request.resourceId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found"));
            if (!targetResource.isAvailable()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Resource is currently not available for booking");
            }
            reservation.setResource(targetResource);
        }

        boolean hasOverlap = reservationRepository.existsOverlappingReservation(
            targetResource.getId(), newStart, newEnd, ReservationStatus.CANCELLED, reservation.getId()
        );
        if (hasOverlap) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Resource is already reserved for the selected time range");
        }

        reservation.setStartTime(newStart);
        reservation.setEndTime(newEnd);

        if (request.status() != null) {
            reservation.setStatus(request.status());
        }

        if (request.price() != null) {
            reservation.setPrice(request.price());
        } else if (request.startTime() != null || request.endTime() != null) {
            reservation.setPrice(calculatePrice(targetResource, null, newStart, newEnd));
        }

        Reservation updated = reservationRepository.save(reservation);
        return ReservationResponse.fromEntity(updated);
    }

    public ReservationResponse cancelReservation(Long id, Long currentUserId, boolean isAdmin) {
        Reservation reservation = findEntityById(id);

        if (!isAdmin && !reservation.getUser().getId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only cancel your own reservations");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        Reservation saved = reservationRepository.save(reservation);
        return ReservationResponse.fromEntity(saved);
    }

    public void deleteReservation(Long id, Long currentUserId, boolean isAdmin) {
        Reservation reservation = findEntityById(id);

        if (!isAdmin && !reservation.getUser().getId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only delete your own reservations");
        }

        reservationRepository.delete(reservation);
    }

    private void validateReservationTimes(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start time and end time are required");
        }
        if (!startTime.isBefore(endTime)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start time must be before end time");
        }
    }

    private BigDecimal calculatePrice(Resource resource, BigDecimal customPrice, LocalDateTime startTime, LocalDateTime endTime) {
        if (customPrice != null) {
            return customPrice;
        }
        long minutes = Math.max(1, Duration.between(startTime, endTime).toMinutes());
        long hours = (minutes + 59) / 60; // ceiling
        return resource.getPricePerHour().multiply(BigDecimal.valueOf(hours));
    }
}
