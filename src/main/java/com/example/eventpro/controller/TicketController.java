package com.example.eventpro.controller;

import com.example.eventpro.dto.TicketResponseDto;
import com.example.eventpro.service.TicketService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping("/issue/{reservationId}")
    public ResponseEntity<List<TicketResponseDto>> issueTickets(
            @PathVariable Long reservationId) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ticketService.issueTickets(reservationId));
    }

    @GetMapping
    public List<TicketResponseDto> getAllTickets() {
        return ticketService.getAllTickets();
    }

    @GetMapping("/{id}")
    public TicketResponseDto getTicketById(@PathVariable Long id) {
        return ticketService.getTicketById(id);
    }

    @GetMapping("/code/{code}")
    public TicketResponseDto getTicketByCode(@PathVariable String code) {
        return ticketService.getTicketByCode(code);
    }

    @GetMapping("/line/{reservationLineId}")
    public TicketResponseDto getTicketByReservationLine(
            @PathVariable Long reservationLineId) {
        return ticketService.getTicketByReservationLine(reservationLineId);
    }

    @GetMapping("/reservation/{reservationId}")
    public List<TicketResponseDto> getTicketsByReservation(
            @PathVariable Long reservationId) {
        return ticketService.getTicketsByReservation(reservationId);
    }

    @GetMapping("/client/{clientId}")
    public List<TicketResponseDto> getTicketsByClient(
            @PathVariable Long clientId) {
        return ticketService.getTicketsByClient(clientId);
    }

    @PatchMapping("/code/{code}/scan")
    public TicketResponseDto scanTicket(@PathVariable String code) {
        return ticketService.scanTicket(code);
    }

    @PatchMapping("/{id}/cancel")
    public TicketResponseDto cancelTicket(@PathVariable Long id) {
        return ticketService.cancelTicket(id);
    }
}
