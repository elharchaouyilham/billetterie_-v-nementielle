package com.example.eventpro.repository;

import com.example.eventpro.entity.Reservation;
import com.example.eventpro.enums.ReservationStatus;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository
        extends JpaRepository<Reservation, Long> {

    List<Reservation> findByClient_Id(Long clientId);

    @Query("""
            SELECT r.id
            FROM Reservation r
            WHERE r.status = :status
              AND r.expirationDate <= :expirationDate
            """)
    List<Long> findExpiredReservationIds(
            @Param("status") ReservationStatus status,
            @Param("expirationDate") Instant expirationDate);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Reservation r WHERE r.id = :id")
    Optional<Reservation> findByIdForUpdate(@Param("id") Long id);
}