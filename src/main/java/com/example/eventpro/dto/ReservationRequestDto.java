package com.example.eventpro.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ReservationRequestDto {

    @NotNull(message = "L'événement est obligatoire")
    @Positive(message = "L'ID de l'événement doit être positif")
    private Long eventId;

    @NotEmpty(
            message = "La réservation doit contenir au moins une ligne"
    )
    @Valid
    private List<ReservationLineRequestDto> lines;

    private String promoCode;

    public Long getClientId() {
    }
}