package com.example.eventpro.repository;

import com.example.eventpro.entity.ReservationLine;
import com.example.eventpro.enums.ReservationStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface ReservationLineRepository
        extends JpaRepository<ReservationLine, Long> {

    @Query("""
        SELECT COALESCE(SUM(line.quantity), 0)
        FROM ReservationLine line
        WHERE line.reservation.client.id = :clientId
          AND line.category.id = :categoryId
          AND (
              line.reservation.status = :confirmed
              OR (
                  line.reservation.status = :pending
                  AND line.reservation.expirationDate > :now
              )
          )
    """)
    Long sumActiveQuantityForClient(
            @Param("clientId") Long clientId,
            @Param("categoryId") Long categoryId,
            @Param("confirmed") ReservationStatus confirmed,
            @Param("pending") ReservationStatus pending,
            @Param("now") Instant now);
}