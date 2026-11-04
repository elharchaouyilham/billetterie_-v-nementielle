package com.example.eventpro.controller;

import com.example.eventpro.dto.ReservationRequestDto;
import com.example.eventpro.dto.ReservationResponseDto;
import com.example.eventpro.service.ReservationService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(
            ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponseDto> createReservation(
            @Valid @RequestBody ReservationRequestDto request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(reservationService.createReservation(request));
    }

    @GetMapping
    public List<ReservationResponseDto> getAllReservations() {
        return reservationService.getAllReservations();
    }

    @GetMapping("/{id}")
    public ReservationResponseDto getReservationById(
            @PathVariable Long id) {
        return reservationService.getReservationById(id);
    }

    @GetMapping("/client/{clientId}")
    public List<ReservationResponseDto> getReservationsByClient(
            @PathVariable Long clientId) {
        return reservationService.getReservationsByClient(clientId);
    }

    @PatchMapping("/{id}/cancel")
    public ReservationResponseDto cancelReservation(
            @PathVariable Long id) {
        return reservationService.cancelReservation(id);
    }
}
