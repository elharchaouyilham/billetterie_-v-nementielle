package com.example.eventpro.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReservationLineRequestDto {

    @NotNull(message = "La catégorie est obligatoire")
    @Positive(message = "L'ID de la catégorie doit être positif")
    private Long categoryId;

    @NotNull(message = "La quantité est obligatoire")
    @Min(value = 1, message = "La quantité doit être au moins 1")
    private Integer quantity;

    public Long getEventCategoryId() {
        return categoryId;
    }
}