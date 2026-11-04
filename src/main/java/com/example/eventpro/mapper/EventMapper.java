package com.example.eventpro.mapper;

import com.example.eventpro.dto.EventCreateRequestDto;
import com.example.eventpro.dto.EventResponseDto;
import com.example.eventpro.dto.EventUpdateRequestDto;
import com.example.eventpro.entity.Event;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EventMapper {

    @Mapping(source = "organizer.id", target = "organizerId")
    EventResponseDto toResponse(Event event);

    List<EventResponseDto> toResponseList(List<Event> events);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "organizer", ignore = true)
    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "status", ignore = true)
    Event toEntity(EventCreateRequestDto request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "organizer", ignore = true)
    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "status", ignore = true)
    Event toEntity(EventUpdateRequestDto request);
}