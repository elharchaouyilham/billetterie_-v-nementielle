package com.example.eventpro.dto;

import com.example.eventpro.enums.ReservationStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Builder
public class ReservationResponseDto {

    private Long id;
    private String reference;
    private Instant creationDate;
    private Instant expirationDate;
    private ReservationStatus status;
    private BigDecimal totalAmount;
    private Long clientId;
}
