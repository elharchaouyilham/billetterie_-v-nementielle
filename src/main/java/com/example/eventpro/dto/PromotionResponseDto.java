package com.example.eventpro.dto;

import com.example.eventpro.enums.DiscountType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class PromotionResponseDto {

    private Long id;
    private Long organizerId;
    private String code;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private LocalDate startDate;
    private LocalDate endDate;
    private int usageLimit;
    private int usageCount;
}
