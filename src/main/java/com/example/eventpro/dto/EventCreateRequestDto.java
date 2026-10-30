package com.example.eventpro.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class EventCreateRequestDto {

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

    @NotNull(message = "L'organisateur est obligatoire")
    @Positive(message = "L'ID de l'organisateur doit être positif")
    private Long organizerId;

    @NotEmpty(message = "Ajoutez au moins une catégorie de billets")
    @Valid
    private List<TicketCategoryRequestDto> categories;
}