package com.resource.bookingsystem.repository;

import com.resource.bookingsystem.entity.Reservation;
import com.resource.bookingsystem.entity.ReservationStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {

    Page<Reservation> findByUserId(Long userId, Pageable pageable);

    List<Reservation> findByUserId(Long userId);

    @Query("SELECT COUNT(r) > 0 FROM Reservation r " +
           "WHERE r.resource.id = :resourceId " +
           "AND r.status != :cancelledStatus " +
           "AND (r.startTime < :endTime AND r.endTime > :startTime) " +
           "AND (:excludeId IS NULL OR r.id != :excludeId)")
    boolean existsOverlappingReservation(
        @Param("resourceId") Long resourceId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime,
        @Param("cancelledStatus") ReservationStatus cancelledStatus,
        @Param("excludeId") Long excludeId
    );
}
