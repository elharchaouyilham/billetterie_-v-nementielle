package com.example.eventpro.mapper;

import com.example.eventpro.dto.TicketCategoryResponseDto;
import com.example.eventpro.entity.TicketCategoriesPerEvent;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TicketCategoryMapper {

    @Mapping(source = "event.id", target = "eventId")
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    TicketCategoryResponseDto toResponse(
            TicketCategoriesPerEvent eventCategory);

    List<TicketCategoryResponseDto> toResponseList(
            List<TicketCategoriesPerEvent> eventCategories);
}
