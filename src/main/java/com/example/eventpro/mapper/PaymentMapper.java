package com.example.eventpro.mapper;

import com.example.eventpro.dto.PaymentRequestDto;
import com.example.eventpro.dto.PaymentResponseDto;
import com.example.eventpro.entity.Payment;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    // Entity vers Response DTO
    @Mapping(source = "reservation.id", target = "reservationId")
    PaymentResponseDto toResponse(Payment payment);

    // Request DTO vers Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "reservation", ignore = true)
    @Mapping(target = "reference", ignore = true)
    @Mapping(target = "paymentDate", ignore = true)
    @Mapping(target = "status", ignore = true)
    Payment toEntity(PaymentRequestDto request);
}