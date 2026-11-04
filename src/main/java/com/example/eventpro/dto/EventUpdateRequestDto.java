package com.example.eventpro.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class EventUpdateRequestDto {

    @NotBlank(message = "Le titre est obligatoire")
    private String title;

    private String description;

    @NotBlank(message = "Le lieu est obligatoire")
    private String location;

    @NotNull(message = "La date de début est obligatoire")
    private Instant startDate;

    @NotNull(message = "La date de fin est obligatoire")
    private Instant endDate;

    @NotNull(message = "La date d'ouverture des ventes est obligatoire")
    private Instant salesOpeningDate;

    @NotNull(message = "La date de clôture des ventes est obligatoire")
    private Instant salesClosingDate;

    @NotNull(message = "La capacité est obligatoire")
    @Min(value = 1, message = "La capacité doit être positive")
    private Integer capacity;
}
