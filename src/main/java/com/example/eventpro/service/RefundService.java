package com.example.eventpro.service;

import com.example.eventpro.dto.RefundRequestDto;
import com.example.eventpro.dto.RefundResponseDto;

import com.example.eventpro.entity.Payment;
import com.example.eventpro.entity.Promotion;
import com.example.eventpro.entity.Refund;
import com.example.eventpro.entity.Reservation;
import com.example.eventpro.entity.Ticket;

import com.example.eventpro.enums.PaymentStatus;
import com.example.eventpro.enums.RefundStatus;
import com.example.eventpro.enums.ReservationStatus;
import com.example.eventpro.enums.TicketStatus;

import com.example.eventpro.exception.ResourceNotFoundException;
import com.example.eventpro.exception.TicketAlreadyUsedException;

import com.example.eventpro.mapper.RefundMapper;

import com.example.eventpro.repository.PaymentRepository;
import com.example.eventpro.repository.RefundRepository;
import com.example.eventpro.repository.ReservationRepository;
import com.example.eventpro.repository.TicketRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class RefundService {

    private final RefundRepository refundRepository;
    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final TicketRepository ticketRepository;
    private final RefundMapper refundMapper;

    public RefundService(
            RefundRepository refundRepository,
            PaymentRepository paymentRepository,
            ReservationRepository reservationRepository,
            TicketRepository ticketRepository,
            RefundMapper refundMapper) {

        this.refundRepository = refundRepository;
        this.paymentRepository = paymentRepository;
        this.reservationRepository = reservationRepository;
        this.ticketRepository = ticketRepository;
        this.refundMapper = refundMapper;
    }

    public RefundResponseDto requestRefund(RefundRequestDto request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Les informations du remboursement sont obligatoires.");
        }

        Payment payment = paymentRepository
                .findById(request.getPaymentId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Paiement introuvable : "
                                        + request.getPaymentId()));

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new IllegalStateException(
                    "Seul un paiement réussi peut être remboursé.");
        }

        ensureNoUsedTicket(payment);

        BigDecimal amount = normalizeAmount(request.getAmount());
        BigDecimal alreadyRequested = calculateRefundedAmount(
                payment.getId(),
                RefundStatus.PENDING,
                RefundStatus.APPROVED);

        BigDecimal refundableAmount = payment.getAmount()
                .subtract(alreadyRequested);

        if (refundableAmount.signum() <= 0) {
            throw new IllegalStateException(
                    "Ce paiement a déjà été entièrement remboursé.");
        }

        if (amount.compareTo(refundableAmount) > 0) {
            throw new IllegalArgumentException(
                    "Le montant du remboursement dépasse le montant remboursable : "
                            + refundableAmount);
        }

        Refund refund = refundMapper.toEntity(request);
        refund.setPayment(payment);
        refund.setAmount(amount);
        refund.setReason(request.getReason());
        refund.setRequestDate(Instant.now());
        refund.setStatus(RefundStatus.PENDING);

        return refundMapper.toResponse(refundRepository.save(refund));
    }

    public RefundResponseDto approveRefund(Long id) {

        Refund refund = getRefundEntity(id);

        if (refund.getStatus() != RefundStatus.PENDING) {
            throw new IllegalStateException(
                    "Seul un remboursement en attente peut être approuvé.");
        }

        Payment payment = refund.getPayment();

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new IllegalStateException(
                    "Seul un paiement réussi peut être remboursé.");
        }

        BigDecimal approvedAmount = calculateRefundedAmount(
                payment.getId(),
                RefundStatus.APPROVED);

        boolean fullRefund = approvedAmount
                .add(refund.getAmount())
                .compareTo(payment.getAmount()) >= 0;

        if (fullRefund) {
            ensureNoUsedTicket(payment);
        }

        refund.setStatus(RefundStatus.APPROVED);
        Refund saved = refundRepository.save(refund);

        if (fullRefund) {
            cancelTicketsAndReleaseStock(payment);
        }

        return refundMapper.toResponse(saved);
    }

    public RefundResponseDto rejectRefund(Long id) {

        Refund refund = getRefundEntity(id);

        if (refund.getStatus() != RefundStatus.PENDING) {
            throw new IllegalStateException(
                    "Seul un remboursement en attente peut être refusé.");
        }

        refund.setStatus(RefundStatus.REJECTED);

        return refundMapper.toResponse(refundRepository.save(refund));
    }

    @Transactional(readOnly = true)
    public RefundResponseDto getRefundById(Long id) {
        return refundMapper.toResponse(getRefundEntity(id));
    }

    @Transactional(readOnly = true)
    public List<RefundResponseDto> getAllRefunds() {
        return refundMapper.toResponseList(refundRepository.findAll());
    }

    @Transactional(readOnly = true)
    public List<RefundResponseDto> getRefundsByPayment(Long paymentId) {

        if (!paymentRepository.existsById(paymentId)) {
            throw new ResourceNotFoundException(
                    "Paiement introuvable : " + paymentId);
        }

        return refundMapper.toResponseList(
                refundRepository.findByPayment_Id(paymentId));
    }

    private Refund getRefundEntity(Long id) {

        return refundRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Remboursement introuvable : " + id));
    }

    private BigDecimal calculateRefundedAmount(
            Long paymentId,
            RefundStatus... statuses) {

        BigDecimal total = BigDecimal.ZERO;

        for (Refund refund : refundRepository.findByPayment_Id(paymentId)) {
            for (RefundStatus status : statuses) {
                if (refund.getStatus() == status) {
                    total = total.add(refund.getAmount());
                    break;
                }
            }
        }

        return total;
    }

    private void ensureNoUsedTicket(Payment payment) {

        Reservation reservation = payment.getReservation();

        List<Ticket> tickets =
                ticketRepository.findByReservationLine_Reservation_Id(
                        reservation.getId());

        boolean usedTicket = tickets.stream()
                .anyMatch(ticket ->
                        ticket.getStatus() == TicketStatus.USED);

        if (usedTicket) {
            throw new TicketAlreadyUsedException(
                    "Un billet déjà utilisé empêche le remboursement.");
        }
    }

    private void cancelTicketsAndReleaseStock(Payment payment) {

        Reservation reservation = payment.getReservation();

        List<Ticket> tickets =
                ticketRepository.findByReservationLine_Reservation_Id(
                        reservation.getId());

        for (Ticket ticket : tickets) {
            if (ticket.getStatus() == TicketStatus.VALID) {
                ticket.setStatus(TicketStatus.CANCELLED);
            }
        }

        ticketRepository.saveAll(tickets);

        reservation.releaseReservedStock();

        Promotion promotion = reservation.getPromotion();
        if (promotion != null && promotion.getUsageCount() > 0) {
            promotion.setUsageCount(promotion.getUsageCount() - 1);
        }

        reservation.setStatus(ReservationStatus.REFUNDED);

        reservationRepository.save(reservation);

        payment.markAsRefunded();
        paymentRepository.save(payment);
    }

    private BigDecimal normalizeAmount(BigDecimal amount) {

        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Le montant du remboursement doit être supérieur à zéro.");
        }

        return amount.setScale(2, RoundingMode.HALF_UP);
    }
}
