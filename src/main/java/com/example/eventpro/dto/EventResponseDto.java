package com.example.eventpro.dto;

import com.example.eventpro.enums.EventStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class EventResponseDto {

    private Long id;
    private String title;
    private String description;
    private String location;

    private Instant startDate;
    private Instant endDate;

    private Instant salesOpeningDate;
    private Instant salesClosingDate;

    private EventStatus status;
    private int capacity;
    private Long organizerId;
}