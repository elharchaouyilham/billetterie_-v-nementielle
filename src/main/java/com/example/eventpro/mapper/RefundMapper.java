package com.example.eventpro.mapper;

import com.example.eventpro.dto.RefundRequestDto;
import com.example.eventpro.dto.RefundResponseDto;
import com.example.eventpro.entity.Refund;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RefundMapper {

    @Mapping(source = "payment.id", target = "paymentId")
    RefundResponseDto toResponse(Refund refund);

    List<RefundResponseDto> toResponseList(List<Refund> refunds);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "payment", ignore = true)
    @Mapping(target = "requestDate", ignore = true)
    @Mapping(target = "status", ignore = true)
    Refund toEntity(RefundRequestDto request);
}
