package com.example.eventpro.repository;

import com.example.eventpro.entity.Reservation;
import com.example.eventpro.enums.ReservationStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface ReservationRepository
        extends JpaRepository<Reservation, Long> {

    // Récupérer les réservations d'un client
    List<Reservation> findByClient_Id(Long clientId);

    // Trouver les réservations dont le délai est dépassé
    List<Reservation> findByStatusAndExpirationDateLessThanEqual(
            ReservationStatus status,
            Instant expirationDate);
}