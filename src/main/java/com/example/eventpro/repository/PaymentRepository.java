package com.example.eventpro.repository;

import com.example.eventpro.entity.Payment;
import com.example.eventpro.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository
        extends JpaRepository<Payment, Long> {

    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    Optional<Payment> findByReference(String reference);

    List<Payment> findByReservation_Id(Long reservationId);

    List<Payment> findByStatus(PaymentStatus status);

    boolean existsByIdempotencyKey(String idempotencyKey);
}
