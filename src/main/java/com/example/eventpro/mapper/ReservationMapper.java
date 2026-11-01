package com.example.eventpro.mapper;

import com.example.eventpro.dto.ReservationResponseDto;
import com.example.eventpro.entity.Reservation;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReservationMapper {

    @Mapping(source = "client.id", target = "clientId")
    ReservationResponseDto toResponse(Reservation reservation);

    List<ReservationResponseDto> toResponseList(
            List<Reservation> reservations);
}