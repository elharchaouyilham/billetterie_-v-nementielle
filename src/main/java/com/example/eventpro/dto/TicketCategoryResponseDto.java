package com.example.eventpro.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class TicketCategoryResponseDto {

    private Long id;
    private Long eventId;
    private Long categoryId;
    private String categoryName;
    private BigDecimal unitPrice;
    private int totalQuantity;
    private int maxQuotaPerClient;
    private int reservedQuantity;
    private int availableQuantity;
}
