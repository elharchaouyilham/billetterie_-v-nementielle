package com.example.eventpro.mapper;

import com.example.eventpro.dto.PromotionRequestDto;
import com.example.eventpro.dto.PromotionResponseDto;
import com.example.eventpro.entity.Promotion;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PromotionMapper {

    @Mapping(source = "organizer.id", target = "organizerId")
    PromotionResponseDto toResponse(Promotion promotion);

    List<PromotionResponseDto> toResponseList(List<Promotion> promotions);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "organizer", ignore = true)
    @Mapping(target = "usageCount", ignore = true)
    Promotion toEntity(PromotionRequestDto request);
}
