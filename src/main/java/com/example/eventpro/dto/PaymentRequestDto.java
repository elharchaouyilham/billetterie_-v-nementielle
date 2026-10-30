package com.example.eventpro.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PaymentRequestDto {

    @NotNull(message = "La réservation est obligatoire")
    @Positive(message = "L'ID de la réservation doit être positif")
    private Long reservationId;

    @NotBlank(message = "La clé d'idempotence est obligatoire")
    private String idempotencyKey;

    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "0.01",
            message = "Le montant doit être supérieur à zéro")
    private BigDecimal amount;
}