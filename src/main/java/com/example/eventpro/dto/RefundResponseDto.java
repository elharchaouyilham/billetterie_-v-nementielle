package com.example.eventpro.dto;

import com.example.eventpro.enums.RefundStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Builder
public class RefundResponseDto {

    private Long id;
    private Long paymentId;
    private BigDecimal amount;
    private String reason;
    private Instant requestDate;
    private RefundStatus status;
}
