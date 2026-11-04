package com.example.eventpro.controller;

import com.example.eventpro.dto.RefundRequestDto;
import com.example.eventpro.dto.RefundResponseDto;
import com.example.eventpro.service.RefundService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/refunds")
public class RefundController {

    private final RefundService refundService;

    public RefundController(RefundService refundService) {
        this.refundService = refundService;
    }

    @PostMapping
    public ResponseEntity<RefundResponseDto> requestRefund(
            @Valid @RequestBody RefundRequestDto request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(refundService.requestRefund(request));
    }

    @GetMapping
    public List<RefundResponseDto> getAllRefunds() {
        return refundService.getAllRefunds();
    }

    @GetMapping("/{id}")
    public RefundResponseDto getRefundById(@PathVariable Long id) {
        return refundService.getRefundById(id);
    }

    @GetMapping("/payment/{paymentId}")
    public List<RefundResponseDto> getRefundsByPayment(
            @PathVariable Long paymentId) {
        return refundService.getRefundsByPayment(paymentId);
    }

    @PatchMapping("/{id}/approve")
    public RefundResponseDto approveRefund(@PathVariable Long id) {
        return refundService.approveRefund(id);
    }

    @PatchMapping("/{id}/reject")
    public RefundResponseDto rejectRefund(@PathVariable Long id) {
        return refundService.rejectRefund(id);
    }
}
