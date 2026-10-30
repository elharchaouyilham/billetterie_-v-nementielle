package com.example.eventpro.dto;

import com.example.eventpro.enums.TicketStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class TicketResponseDto {

    private Long id;
    private String code;
    private Instant issueDate;
    private Instant scanDate;
    private TicketStatus status;
    private Long reservationLineId;
}