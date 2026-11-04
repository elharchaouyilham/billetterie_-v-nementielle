package com.example.eventpro.dto;

import com.example.eventpro.enums.DiscountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class PromotionRequestDto {

    @NotBlank(message = "Le code promo est obligatoire")
    private String code;

    @NotNull(message = "Le type de réduction est obligatoire")
    private DiscountType discountType;

    @NotNull(message = "La valeur de la réduction est obligatoire")
    @DecimalMin(value = "0.01",
            message = "La réduction doit être supérieure à zéro")
    private BigDecimal discountValue;

    private LocalDate startDate;

    private LocalDate endDate;

    @Min(value = 0, message = "La limite d'utilisation ne peut pas être négative")
    private int usageLimit;
}
