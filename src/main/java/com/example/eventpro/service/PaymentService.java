package com.example.eventpro.service;

import com.example.eventpro.dto.PaymentRequestDto;
import com.example.eventpro.dto.PaymentResponseDto;

import com.example.eventpro.entity.Payment;
import com.example.eventpro.entity.Promotion;
import com.example.eventpro.entity.Reservation;

import com.example.eventpro.enums.ReservationStatus;

import com.example.eventpro.exception.IdempotencyConflictException;
import com.example.eventpro.exception.ReservationExpiredException;
import com.example.eventpro.exception.ResourceNotFoundException;

import com.example.eventpro.mapper.PaymentMapper;

import com.example.eventpro.repository.PaymentRepository;
import com.example.eventpro.repository.ReservationRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentMapper paymentMapper;
    private final TicketService ticketService;

    public PaymentService(
            PaymentRepository paymentRepository,
            ReservationRepository reservationRepository,
            PaymentMapper paymentMapper,
            TicketService ticketService) {

        this.paymentRepository = paymentRepository;
        this.reservationRepository = reservationRepository;
        this.paymentMapper = paymentMapper;
        this.ticketService = ticketService;
    }

    @Transactional(noRollbackFor = ReservationExpiredException.class)
    public PaymentResponseDto createPayment(PaymentRequestDto request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Les informations du paiement sont obligatoires.");
        }

        String idempotencyKey = normalizeIdempotencyKey(
                request.getIdempotencyKey());

        Payment existingPayment = paymentRepository
                .findByIdempotencyKey(idempotencyKey)
                .orElse(null);

        if (existingPayment != null) {
            return handleExistingPayment(existingPayment, request);
        }

        Reservation reservation = reservationRepository
                .findByIdForUpdate(request.getReservationId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Réservation introuvable : "
                                        + request.getReservationId()));

        Payment replay = paymentRepository
                .findByIdempotencyKey(idempotencyKey)
                .orElse(null);

        if (replay != null) {
            return handleExistingPayment(replay, request);
        }

        Instant now = Instant.now();

        validateReservationIsPayable(reservation, now);

        BigDecimal amount = normalizeAmount(request.getAmount());

        if (amount.compareTo(reservation.getTotalAmount()) != 0) {
            throw new IllegalArgumentException(
                    "Le montant du paiement ne correspond pas au total de la réservation.");
        }

        Payment payment = paymentMapper.toEntity(request);
        payment.setIdempotencyKey(idempotencyKey);
        payment.setReservation(reservation);
        payment.setAmount(amount);
        payment.setReference("PAY-" + UUID.randomUUID());
        payment.markAsSuccessful();

        confirmReservation(reservation);

        Payment saved;

        try {
            saved = paymentRepository.save(payment);
        } catch (DataIntegrityViolationException ex) {
            throw new IdempotencyConflictException(
                    "Un paiement avec cette clé d'idempotence existe déjà.");
        }

        ticketService.issueTickets(reservation.getId());

        return paymentMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PaymentResponseDto getPaymentById(Long id) {

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Paiement introuvable : " + id));

        return paymentMapper.toResponse(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponseDto getPaymentByReference(String reference) {

        Payment payment = paymentRepository.findByReference(reference)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Paiement introuvable : " + reference));

        return paymentMapper.toResponse(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponseDto> getPaymentsByReservation(
            Long reservationId) {

        if (!reservationRepository.existsById(reservationId)) {
            throw new ResourceNotFoundException(
                    "Réservation introuvable : " + reservationId);
        }

        return paymentMapper.toResponseList(
                paymentRepository.findByReservation_Id(reservationId));
    }

    @Transactional(readOnly = true)
    public List<PaymentResponseDto> getAllPayments() {

        return paymentMapper.toResponseList(paymentRepository.findAll());
    }

    @Transactional(noRollbackFor = ReservationExpiredException.class)
    public PaymentResponseDto confirmPayment(Long id) {

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Paiement introuvable : " + id));

        if (payment.isSuccessful()) {
            return paymentMapper.toResponse(payment);
        }

        if (!payment.isPending()) {
            throw new IllegalStateException(
                    "Seul un paiement en attente peut être confirmé.");
        }

        Reservation reservation = reservationRepository
                .findByIdForUpdate(payment.getReservation().getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Réservation introuvable."));

        validateReservationIsPayable(reservation, Instant.now());

        payment.setReservation(reservation);
        payment.markAsSuccessful();
        confirmReservation(reservation);

        Payment savedPayment = paymentRepository.save(payment);

        ticketService.issueTickets(reservation.getId());

        return paymentMapper.toResponse(savedPayment);
    }

    public PaymentResponseDto failPayment(Long id) {

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Paiement introuvable : " + id));

        if (!payment.isPending()) {
            throw new IllegalStateException(
                    "Seul un paiement en attente peut être refusé.");
        }

        payment.markAsFailed();

        return paymentMapper.toResponse(paymentRepository.save(payment));
    }

    private PaymentResponseDto handleExistingPayment(
            Payment payment,
            PaymentRequestDto request) {

        boolean sameReservation =
                payment.getReservation() != null
                        && payment.getReservation().getId()
                        .equals(request.getReservationId());

        boolean sameAmount =
                payment.getAmount() != null
                        && request.getAmount() != null
                        && payment.getAmount().compareTo(request.getAmount()) == 0;

        if (!sameReservation || !sameAmount) {
            throw new IdempotencyConflictException(
                    "Cette clé d'idempotence a déjà été utilisée avec des paramètres différents.");
        }

        return paymentMapper.toResponse(payment);
    }

    private void validateReservationIsPayable(
            Reservation reservation,
            Instant now) {

        boolean delayElapsed = reservation.getExpirationDate() != null
                && now.isAfter(reservation.getExpirationDate());

        if (reservation.getStatus() == ReservationStatus.PENDING_PAYMENT
                && delayElapsed) {

            reservation.releaseReservedStock();
            reservation.expire();

            throw new ReservationExpiredException(
                    "La réservation a expiré : "
                            + reservation.getReference());
        }

        if (reservation.getStatus() == ReservationStatus.EXPIRED) {
            throw new ReservationExpiredException(
                    "La réservation a expiré : "
                            + reservation.getReference());
        }

        if (reservation.getStatus() == ReservationStatus.CANCELLED
                || reservation.getStatus() == ReservationStatus.REFUNDED) {
            throw new IllegalStateException(
                    "Cette réservation n'est plus payable.");
        }

        if (reservation.getStatus() != ReservationStatus.PENDING_PAYMENT) {
            throw new IllegalStateException(
                    "Cette réservation n'est pas en attente de paiement.");
        }
    }

    private void confirmReservation(Reservation reservation) {

        reservation.setStatus(ReservationStatus.CONFIRMED);

        Promotion promotion = reservation.getPromotion();

        if (promotion != null) {
            promotion.setUsageCount(promotion.getUsageCount() + 1);
        }
    }

    private BigDecimal normalizeAmount(BigDecimal amount) {

        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Le montant doit être supérieur à zéro.");
        }

        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private String normalizeIdempotencyKey(String idempotencyKey) {

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException(
                    "La clé d'idempotence est obligatoire.");
        }

        return idempotencyKey.trim();
    }
}
