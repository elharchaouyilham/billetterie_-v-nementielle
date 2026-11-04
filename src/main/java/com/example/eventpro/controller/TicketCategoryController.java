package com.example.eventpro.controller;

import com.example.eventpro.dto.TicketCategoryCreateRequestDto;
import com.example.eventpro.dto.TicketCategoryRequestDto;
import com.example.eventpro.dto.TicketCategoryResponseDto;
import com.example.eventpro.entity.TicketCategoriesPerEvent;
import com.example.eventpro.entity.TicketCategory;
import com.example.eventpro.mapper.TicketCategoryMapper;
import com.example.eventpro.service.TicketCategoryService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class TicketCategoryController {

    private final TicketCategoryService ticketCategoryService;
    private final TicketCategoryMapper ticketCategoryMapper;

    public TicketCategoryController(
            TicketCategoryService ticketCategoryService,
            TicketCategoryMapper ticketCategoryMapper) {

        this.ticketCategoryService = ticketCategoryService;
        this.ticketCategoryMapper = ticketCategoryMapper;
    }

    @PostMapping("/ticket-categories")
    public ResponseEntity<TicketCategory> createCategory(
            @Valid @RequestBody TicketCategoryCreateRequestDto request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ticketCategoryService.createCategory(
                        request.getName()));
    }

    @GetMapping("/ticket-categories")
    public List<TicketCategory> getAllCategories() {
        return ticketCategoryService.getAllCategories();
    }

    @PostMapping("/events/{eventId}/categories")
    public ResponseEntity<TicketCategoryResponseDto> addCategoryToEvent(
            @PathVariable Long eventId,
            @Valid @RequestBody TicketCategoryRequestDto request) {

        TicketCategoriesPerEvent eventCategory =
                ticketCategoryService.addCategoryToEvent(
                        eventId,
                        request.getCategoryId(),
                        request.getUnitPrice(),
                        request.getTotalQuantity(),
                        request.getMaxQuotaPerClient());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ticketCategoryMapper.toResponse(eventCategory));
    }

    @GetMapping("/events/{eventId}/categories")
    public List<TicketCategoryResponseDto> getCategoriesByEvent(
            @PathVariable Long eventId) {

        return ticketCategoryMapper.toResponseList(
                ticketCategoryService.getCategoriesByEvent(eventId));
    }
}
