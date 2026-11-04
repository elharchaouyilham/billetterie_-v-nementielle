package com.example.eventpro.controller;

import com.example.eventpro.dto.PromotionRequestDto;
import com.example.eventpro.dto.PromotionResponseDto;
import com.example.eventpro.entity.Promotion;
import com.example.eventpro.mapper.PromotionMapper;
import com.example.eventpro.service.PromotionService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/promotions")
public class PromotionController {

    private final PromotionService promotionService;
    private final PromotionMapper promotionMapper;

    public PromotionController(
            PromotionService promotionService,
            PromotionMapper promotionMapper) {
        this.promotionService = promotionService;
        this.promotionMapper = promotionMapper;
    }

    @PostMapping
    public ResponseEntity<PromotionResponseDto> createPromotion(
            @RequestParam Long organizerId,
            @Valid @RequestBody PromotionRequestDto request) {

        Promotion promotion = promotionMapper.toEntity(request);

        Promotion saved = promotionService.createPromotion(
                promotion,
                organizerId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(promotionMapper.toResponse(saved));
    }

    @GetMapping
    public List<PromotionResponseDto> getAllPromotions() {
        return promotionMapper.toResponseList(
                promotionService.getAllPromotions());
    }

    @GetMapping("/{id}")
    public PromotionResponseDto getPromotionById(@PathVariable Long id) {
        return promotionMapper.toResponse(
                promotionService.getPromotionById(id));
    }

    @GetMapping("/organizer/{organizerId}")
    public List<PromotionResponseDto> getPromotionsByOrganizer(
            @PathVariable Long organizerId) {

        return promotionMapper.toResponseList(
                promotionService.getPromotionsByOrganizer(organizerId));
    }

    @GetMapping("/code/{code}")
    public PromotionResponseDto getPromotionByCode(
            @PathVariable String code) {
        return promotionMapper.toResponse(
                promotionService.getPromotionByCode(code));
    }

    @GetMapping("/validate/{code}")
    public boolean isPromotionValid(@PathVariable String code) {
        return promotionService.isPromotionValid(code);
    }

    @GetMapping("/discount")
    public BigDecimal calculateDiscount(
            @RequestParam String code,
            @RequestParam BigDecimal subtotal) {

        return promotionService.calculateDiscount(code, subtotal);
    }

    @PutMapping("/{id}")
    public PromotionResponseDto updatePromotion(
            @PathVariable Long id,
            @Valid @RequestBody PromotionRequestDto request) {

        Promotion updatedPromotion = promotionMapper.toEntity(request);

        return promotionMapper.toResponse(
                promotionService.updatePromotion(id, updatedPromotion));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePromotion(@PathVariable Long id) {

        promotionService.deletePromotion(id);

        return ResponseEntity.noContent().build();
    }
}
