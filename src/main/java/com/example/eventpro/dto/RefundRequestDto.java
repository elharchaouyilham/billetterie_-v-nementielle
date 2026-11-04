package com.example.eventpro.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class RefundRequestDto {

    @NotNull(message = "Le paiement est obligatoire")
    @Positive(message = "L'ID du paiement doit être positif")
    private Long paymentId;

    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "0.01",
            message = "Le montant doit être supérieur à zéro")
    private BigDecimal amount;

    private String reason;
}
