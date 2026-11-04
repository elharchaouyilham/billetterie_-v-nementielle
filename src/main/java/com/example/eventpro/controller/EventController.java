package com.example.eventpro.controller;

import com.example.eventpro.dto.EventCreateRequestDto;
import com.example.eventpro.dto.EventResponseDto;
import com.example.eventpro.dto.EventUpdateRequestDto;
import com.example.eventpro.entity.Event;
import com.example.eventpro.mapper.EventMapper;
import com.example.eventpro.service.EventService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;
    private final EventMapper eventMapper;

    public EventController(
            EventService eventService,
            EventMapper eventMapper) {
        this.eventService = eventService;
        this.eventMapper = eventMapper;
    }

    @PostMapping
    public ResponseEntity<EventResponseDto> createEvent(
            @Valid @RequestBody EventCreateRequestDto request) {

        Event event = eventMapper.toEntity(request);

        Event saved = eventService.createEvent(
                event,
                request.getOrganizerId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(eventMapper.toResponse(saved));
    }

    @GetMapping
    public List<EventResponseDto> getAllEvents() {
        return eventMapper.toResponseList(eventService.getAllEvents());
    }

    @GetMapping("/published")
    public List<EventResponseDto> getPublishedEvents() {
        return eventMapper.toResponseList(
                eventService.getPublishedEvents());
    }

    @GetMapping("/{id}")
    public EventResponseDto getEventById(@PathVariable Long id) {
        return eventMapper.toResponse(eventService.getEventById(id));
    }

    @PutMapping("/{id}")
    public EventResponseDto updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventUpdateRequestDto request) {

        Event updatedEvent = eventMapper.toEntity(request);

        return eventMapper.toResponse(
                eventService.updateEvent(id, updatedEvent));
    }

    @PatchMapping("/{id}/publish")
    public EventResponseDto publishEvent(@PathVariable Long id) {
        return eventMapper.toResponse(eventService.publishEvent(id));
    }

    @PatchMapping("/{id}/cancel")
    public EventResponseDto cancelEvent(@PathVariable Long id) {
        return eventMapper.toResponse(eventService.cancelEvent(id));
    }
}
