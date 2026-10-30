package com.example.eventpro.dto;

import com.example.eventpro.enums.PaymentStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Builder
public class PaymentResponseDto {

    private Long id;
    private String reference;
    private BigDecimal amount;
    private Instant paymentDate;
    private PaymentStatus status;
    private Long reservationId;
}