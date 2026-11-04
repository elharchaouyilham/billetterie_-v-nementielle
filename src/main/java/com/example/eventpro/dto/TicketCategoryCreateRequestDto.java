package com.example.eventpro.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TicketCategoryCreateRequestDto {

    @NotBlank(message = "Le nom de la catégorie est obligatoire")
    private String name;
}
