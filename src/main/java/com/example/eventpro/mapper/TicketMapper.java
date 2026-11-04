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
    @Mapping(
            source = "reservationLine.reservation.id",
            target = "reservationId"
    )
    @Mapping(
            source = "reservationLine.reservation.reference",
            target = "reservationReference"
    )
    @Mapping(
            source = "reservationLine.category.event.id",
            target = "eventId"
    )
    @Mapping(
            source = "reservationLine.category.event.title",
            target = "eventTitle"
    )
    @Mapping(
            source = "reservationLine.category.category.name",
            target = "categoryName"
    )
    @Mapping(
            source = "reservationLine.quantity",
            target = "quantity"
    )
    @Mapping(
            source = "reservationLine.fixedUnitPrice",
            target = "unitPrice"
    )
    @Mapping(
            source = "reservationLine.subtotal",
            target = "subtotal"
    )
    TicketResponseDto toResponse(Ticket ticket);

    List<TicketResponseDto> toResponseList(List<Ticket> tickets);
}