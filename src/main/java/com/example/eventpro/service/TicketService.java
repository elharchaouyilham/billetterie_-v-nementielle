package com.example.eventpro.service;

import com.example.eventpro.dto.TicketResponseDto;

import com.example.eventpro.entity.Reservation;
import com.example.eventpro.entity.ReservationLine;
import com.example.eventpro.entity.Ticket;

import com.example.eventpro.enums.ReservationStatus;
import com.example.eventpro.enums.TicketStatus;

import com.example.eventpro.exception.ResourceNotFoundException;
import com.example.eventpro.exception.TicketAlreadyUsedException;

import com.example.eventpro.mapper.TicketMapper;

import com.example.eventpro.repository.ReservationRepository;
import com.example.eventpro.repository.TicketRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class TicketService {

    private final TicketRepository ticketRepository;
    private final ReservationRepository reservationRepository;
    private final TicketMapper ticketMapper;

    public TicketService(
            TicketRepository ticketRepository,
            ReservationRepository reservationRepository,
            TicketMapper ticketMapper) {

        this.ticketRepository = ticketRepository;
        this.reservationRepository = reservationRepository;
        this.ticketMapper = ticketMapper;
    }

    public List<TicketResponseDto> issueTickets(Long reservationId) {

        Reservation reservation = reservationRepository
                .findByIdForUpdate(reservationId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Réservation introuvable : "
                                        + reservationId));

        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new IllegalStateException(
                    "Les billets ne peuvent être générés que pour une réservation confirmée.");
        }

        List<ReservationLine> lines = reservation.getLines();

        if (lines == null || lines.isEmpty()) {
            throw new IllegalStateException(
                    "La réservation ne contient aucune ligne.");
        }

        Map<Long, Ticket> ticketsByLineId = new LinkedHashMap<>();

        List<Ticket> existingTickets =
                ticketRepository.findByReservationLine_Reservation_Id(
                        reservationId);

        for (Ticket ticket : existingTickets) {
            if (ticket.getReservationLine() != null
                    && ticket.getReservationLine().getId() != null) {
                ticketsByLineId.putIfAbsent(
                        ticket.getReservationLine().getId(),
                        ticket);
            }
        }

        Instant now = Instant.now();
        List<Ticket> createdTickets = new ArrayList<>();

        for (ReservationLine line : lines) {

            if (line.getId() == null
                    || ticketsByLineId.containsKey(line.getId())) {
                continue;
            }

            Ticket ticket = new Ticket();
            ticket.setCode("TCK-" + UUID.randomUUID());
            ticket.setReservationLine(line);
            ticket.setIssueDate(now);
            ticket.setStatus(TicketStatus.VALID);

            createdTickets.add(ticket);
            ticketsByLineId.put(line.getId(), ticket);
        }

        if (!createdTickets.isEmpty()) {
            ticketRepository.saveAll(createdTickets);
        }

        List<Ticket> tickets = new ArrayList<>(ticketsByLineId.values());
        tickets.sort(Comparator.comparing(
                ticket -> ticket.getReservationLine().getId()));

        return ticketMapper.toResponseList(tickets);
    }

    @Transactional(readOnly = true)
    public List<TicketResponseDto> getAllTickets() {
        return ticketMapper.toResponseList(ticketRepository.findAll());
    }

    @Transactional(readOnly = true)
    public TicketResponseDto getTicketById(Long id) {

        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Billet introuvable : " + id));

        return ticketMapper.toResponse(ticket);
    }

    @Transactional(readOnly = true)
    public TicketResponseDto getTicketByCode(String code) {

        Ticket ticket = ticketRepository.findByCode(code)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Billet introuvable : " + code));

        return ticketMapper.toResponse(ticket);
    }

    @Transactional(readOnly = true)
    public TicketResponseDto getTicketByReservationLine(
            Long reservationLineId) {

        Ticket ticket = ticketRepository
                .findByReservationLine_Id(reservationLineId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Billet introuvable pour la ligne de réservation : "
                                        + reservationLineId));

        return ticketMapper.toResponse(ticket);
    }

    @Transactional(readOnly = true)
    public List<TicketResponseDto> getTicketsByReservation(
            Long reservationId) {

        if (!reservationRepository.existsById(reservationId)) {
            throw new ResourceNotFoundException(
                    "Réservation introuvable : " + reservationId);
        }

        return ticketMapper.toResponseList(
                ticketRepository.findByReservationLine_Reservation_Id(
                        reservationId));
    }

    @Transactional(readOnly = true)
    public List<TicketResponseDto> getTicketsByClient(Long clientId) {

        return ticketMapper.toResponseList(
                ticketRepository
                        .findByReservationLine_Reservation_Client_Id(
                                clientId));
    }

    public TicketResponseDto scanTicket(String code) {

        Ticket ticket = ticketRepository.findByCode(code)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Billet introuvable : " + code));

        if (ticket.getStatus() == TicketStatus.USED) {
            throw new TicketAlreadyUsedException(
                    "Ce billet a déjà été utilisé.");
        }

        if (ticket.getStatus() == TicketStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Ce billet a été annulé.");
        }

        ticket.setStatus(TicketStatus.USED);
        ticket.setScanDate(Instant.now());

        return ticketMapper.toResponse(ticketRepository.save(ticket));
    }

    public TicketResponseDto cancelTicket(Long id) {

        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Billet introuvable : " + id));

        if (ticket.getStatus() == TicketStatus.USED) {
            throw new TicketAlreadyUsedException(
                    "Un billet déjà utilisé ne peut pas être annulé.");
        }

        ticket.setStatus(TicketStatus.CANCELLED);

        return ticketMapper.toResponse(ticketRepository.save(ticket));
    }
}
