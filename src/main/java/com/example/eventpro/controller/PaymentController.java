package com.example.eventpro.controller;

import com.example.eventpro.dto.PaymentRequestDto;
import com.example.eventpro.dto.PaymentResponseDto;
import com.example.eventpro.service.PaymentService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponseDto> createPayment(
            @Valid @RequestBody PaymentRequestDto request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(paymentService.createPayment(request));
    }

    @GetMapping
    public List<PaymentResponseDto> getAllPayments() {
        return paymentService.getAllPayments();
    }

    @GetMapping("/{id}")
    public PaymentResponseDto getPaymentById(@PathVariable Long id) {
        return paymentService.getPaymentById(id);
    }

    @GetMapping("/reference/{reference}")
    public PaymentResponseDto getPaymentByReference(
            @PathVariable String reference) {
        return paymentService.getPaymentByReference(reference);
    }

    @GetMapping("/reservation/{reservationId}")
    public List<PaymentResponseDto> getPaymentsByReservation(
            @PathVariable Long reservationId) {
        return paymentService.getPaymentsByReservation(reservationId);
    }

    @PatchMapping("/{id}/confirm")
    public PaymentResponseDto confirmPayment(@PathVariable Long id) {
        return paymentService.confirmPayment(id);
    }

    @PatchMapping("/{id}/fail")
    public PaymentResponseDto failPayment(@PathVariable Long id) {
        return paymentService.failPayment(id);
    }
}
