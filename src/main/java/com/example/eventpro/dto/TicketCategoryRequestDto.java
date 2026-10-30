package com.example.eventpro.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TicketCategoryRequestDto {

    @NotNull(message = "L'ID de la catégorie est obligatoire")
    @Positive(message = "L'ID doit être positif")
    private Long categoryId;

    @NotNull(message = "Le prix est obligatoire")
    @DecimalMin(value = "0.01",
            message = "Le prix doit être supérieur à zéro")
    private BigDecimal unitPrice;

    @NotNull(message = "La quantité est obligatoire")
    @Min(value = 1, message = "La quantité doit être positive")
    private Integer totalQuantity;

    @NotNull(message = "Le quota est obligatoire")
    @Min(value = 1, message = "Le quota doit être positif")
    private Integer maxQuotaPerClient;
}