package com.example.eventpro.service;

import com.example.eventpro.dto.ReservationLineRequestDto;
import com.example.eventpro.dto.ReservationRequestDto;
import com.example.eventpro.dto.ReservationResponseDto;

import com.example.eventpro.entity.Client;
import com.example.eventpro.entity.Event;
import com.example.eventpro.entity.Promotion;
import com.example.eventpro.entity.Reservation;
import com.example.eventpro.entity.ReservationLine;
import com.example.eventpro.entity.TicketCategoriesPerEvent;

import com.example.eventpro.enums.EventStatus;
import com.example.eventpro.enums.ReservationStatus;

import com.example.eventpro.exception.InsufficientStockException;
import com.example.eventpro.exception.ReservationExpiredException;
import com.example.eventpro.exception.ResourceNotFoundException;

import com.example.eventpro.mapper.ReservationMapper;

import com.example.eventpro.repository.ClientRepository;
import com.example.eventpro.repository.EventRepository;
import com.example.eventpro.repository.PromotionRepository;
import com.example.eventpro.repository.ReservationLineRepository;
import com.example.eventpro.repository.ReservationRepository;
import com.example.eventpro.repository.TicketCategoriesPerEventRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationLineRepository reservationLineRepository;
    private final ClientRepository clientRepository;
    private final EventRepository eventRepository;
    private final TicketCategoriesPerEventRepository categoryRepository;
    private final PromotionRepository promotionRepository;
    private final ReservationMapper reservationMapper;

    public ReservationService(
            ReservationRepository reservationRepository,
            ReservationLineRepository reservationLineRepository,
            ClientRepository clientRepository,
            EventRepository eventRepository,
            TicketCategoriesPerEventRepository categoryRepository,
            PromotionRepository promotionRepository,
            ReservationMapper reservationMapper) {

        this.reservationRepository = reservationRepository;
        this.reservationLineRepository = reservationLineRepository;
        this.clientRepository = clientRepository;
        this.eventRepository = eventRepository;
        this.categoryRepository = categoryRepository;
        this.promotionRepository = promotionRepository;
        this.reservationMapper = reservationMapper;
    }


    public ReservationResponseDto createReservation(
            ReservationRequestDto request) {

        if (request == null || request.getLines() == null
                || request.getLines().isEmpty()) {
            throw new IllegalArgumentException(
                    "La réservation doit contenir au moins une ligne.");
        }

        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Client introuvable : "
                                        + request.getClientId()));

        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Événement introuvable : "
                                        + request.getEventId()));

        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new IllegalStateException(
                    "Cet événement n'est pas publié.");
        }

        Instant now = Instant.now();

        if (event.getSalesOpeningDate() != null
                && now.isBefore(event.getSalesOpeningDate())) {
            throw new IllegalStateException(
                    "La vente des billets n'est pas encore ouverte.");
        }

        if (event.getSalesClosingDate() != null
                && now.isAfter(event.getSalesClosingDate())) {
            throw new IllegalStateException(
                    "La vente des billets est terminée.");
        }

        if (event.getStartDate() != null
                && !now.isBefore(event.getStartDate())) {
            throw new IllegalStateException(
                    "L'événement a déjà commencé.");
        }

        Reservation reservation = new Reservation();
        reservation.setReference("RES-" + UUID.randomUUID());
        reservation.setClient(client);
        reservation.setCreationDate(now);
        reservation.setExpirationDate(now.plus(Duration.ofMinutes(10)));
        reservation.setStatus(ReservationStatus.PENDING_PAYMENT);

        Set<Long> processedCategories = new HashSet<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (ReservationLineRequestDto lineRequest
                : request.getLines()) {

            if (lineRequest.getEventCategoryId() == null
                    || lineRequest.getQuantity() == null
                    || lineRequest.getQuantity() <= 0) {
                throw new IllegalArgumentException(
                        "La catégorie et une quantité positive sont obligatoires.");
            }

            Long categoryId = lineRequest.getEventCategoryId();
            int quantity = lineRequest.getQuantity();

            if (!processedCategories.add(categoryId)) {
                throw new IllegalArgumentException(
                        "Une même catégorie ne peut pas apparaître deux fois.");
            }

            TicketCategoriesPerEvent category =
                    categoryRepository.findByIdForUpdate(categoryId)
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Catégorie de billets introuvable : "
                                                    + categoryId));

            if (!category.getEvent().getId().equals(event.getId())) {
                throw new IllegalArgumentException(
                        "Cette catégorie n'appartient pas à l'événement demandé.");
            }

            if (category.getAvailableQuantity() < quantity) {
                throw new InsufficientStockException(
                        "Stock insuffisant pour la catégorie "
                                + category.getCategory().getName());
            }

            Long alreadyReserved =
                    reservationLineRepository.sumActiveQuantityForClient(
                            client.getId(),
                            category.getId(),
                            ReservationStatus.CONFIRMED,
                            ReservationStatus.PENDING_PAYMENT,
                            now);

            long totalForClient =
                    (alreadyReserved == null ? 0 : alreadyReserved)
                            + quantity;

            if (totalForClient > category.getMaxQuotaPerClient()) {
                throw new IllegalArgumentException(
                        "Le quota maximal par client est dépassé pour "
                                + category.getCategory().getName());
            }

            category.setReservedQuantity(
                    category.getReservedQuantity() + quantity);

            ReservationLine line = new ReservationLine();
            line.setReservation(reservation);
            line.setCategory(category);
            line.setQuantity(quantity);
            line.setFixedUnitPrice(category.getUnitPrice());

            BigDecimal lineSubtotal = category.getUnitPrice()
                    .multiply(BigDecimal.valueOf(quantity));

            line.setSubtotal(lineSubtotal);
            reservation.getLines().add(line);

            subtotal = subtotal.add(lineSubtotal);
        }

        if (request.getPromoCode() != null
                && !request.getPromoCode().isBlank()) {

            String code = request.getPromoCode().trim();

            Promotion promotion = promotionRepository.findByCode(
                            code.toUpperCase(java.util.Locale.ROOT))
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Code promotionnel introuvable : "
                                            + code));

            if (!promotion.isValid()) {
                throw new IllegalArgumentException(
                        "Ce code promotionnel n'est plus valide.");
            }

            if (!promotion.getOrganizer().getId()
                    .equals(event.getOrganizer().getId())) {
                throw new IllegalArgumentException(
                        "Cette promotion n'appartient pas à l'organisateur de l'événement.");
            }

            reservation.setPromotion(promotion);

            BigDecimal discount =
                    promotion.calculateDiscount(subtotal);

            reservation.setTotalAmount(subtotal.subtract(discount));
        } else {
            reservation.setTotalAmount(subtotal);
        }

        Reservation saved = reservationRepository.save(reservation);

        return reservationMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ReservationResponseDto getReservationById(Long id) {

        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Réservation introuvable : " + id));

        return reservationMapper.toResponse(reservation);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponseDto> getAllReservations() {

        return reservationMapper.toResponseList(
                reservationRepository.findAll());
    }

    @Transactional(readOnly = true)
    public List<ReservationResponseDto> getReservationsByClient(
            Long clientId) {

        if (!clientRepository.existsById(clientId)) {
            throw new ResourceNotFoundException(
                    "Client introuvable : " + clientId);
        }

        return reservationMapper.toResponseList(
                reservationRepository.findByClient_Id(clientId));
    }

    public ReservationResponseDto cancelReservation(Long id) {

        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Réservation introuvable : " + id));

        if (reservation.getStatus() == ReservationStatus.CANCELLED
                || reservation.getStatus() == ReservationStatus.EXPIRED
                || reservation.getStatus() == ReservationStatus.REFUNDED) {
            throw new IllegalStateException(
                    "Cette réservation ne peut pas être annulée.");
        }

        if (reservation.getStatus() == ReservationStatus.CONFIRMED) {
            throw new IllegalStateException(
                    "Une réservation confirmée doit faire l'objet d'un remboursement.");
        }

        reservation.releaseReservedStock();
        reservation.cancel();

        return reservationMapper.toResponse(
                reservationRepository.save(reservation));
    }

    public int expireReservations() {

        Instant now = Instant.now();

        List<Long> candidates =
                reservationRepository.findExpiredReservationIds(
                        ReservationStatus.PENDING_PAYMENT,
                        now);

        int expiredCount = 0;

        for (Long candidateId : candidates) {

            Reservation reservation = reservationRepository
                    .findByIdForUpdate(candidateId)
                    .orElse(null);

            if (reservation == null
                    || reservation.getStatus()
                    != ReservationStatus.PENDING_PAYMENT) {
                continue;
            }

            if (reservation.getExpirationDate() != null
                    && reservation.getExpirationDate().isAfter(now)) {
                continue;
            }

            reservation.releaseReservedStock();
            reservation.expire();
            expiredCount++;
        }

        return expiredCount;
    }
}