package com.example.eventpro.mapper;

import com.example.eventpro.dto.TicketResponseDto;
import com.example.eventpro.entity.Ticket;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TicketMapper {

    @Mapping(
            source = "reservationLine.id",
            target = "reservationLineId"
    )
    TicketResponseDto toResponse(Ticket ticket);

    List<TicketResponseDto> toResponseList(List<Ticket> tickets);
}