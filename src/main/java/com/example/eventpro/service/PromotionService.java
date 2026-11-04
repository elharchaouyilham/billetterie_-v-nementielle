
package com.example.eventpro.service;

import com.example.eventpro.entity.Organizer;
import com.example.eventpro.entity.Promotion;
import com.example.eventpro.enums.DiscountType;
import com.example.eventpro.exception.ResourceNotFoundException;
import com.example.eventpro.repository.OrganizerRepository;
import com.example.eventpro.repository.PromotionRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final OrganizerRepository organizerRepository;

    public PromotionService(
            PromotionRepository promotionRepository,
            OrganizerRepository organizerRepository) {

        this.promotionRepository = promotionRepository;
        this.organizerRepository = organizerRepository;
    }

    public Promotion createPromotion(
            Promotion promotion,
            Long organizerId) {

        Organizer organizer = organizerRepository
                .findById(organizerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Organisateur introuvable : " + organizerId));

        validatePromotion(promotion);

        String code = normalizeCode(promotion.getCode());

        if (promotionRepository.existsByCode(code)) {
            throw new IllegalArgumentException(
                    "Ce code promo existe déjà.");
        }

        promotion.setId(null);
        promotion.setCode(code);
        promotion.setOrganizer(organizer);
        promotion.setUsageCount(0);

        return promotionRepository.save(promotion);
    }


    @Transactional(readOnly = true)
    public List<Promotion> getAllPromotions() {
        return promotionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Promotion getPromotionById(Long id) {

        return promotionRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Promotion introuvable : " + id));
    }

    @Transactional(readOnly = true)
    public List<Promotion> getPromotionsByOrganizer(
            Long organizerId) {

        if (!organizerRepository.existsById(organizerId)) {
            throw new ResourceNotFoundException(
                    "Organisateur introuvable : " + organizerId);
        }

        return promotionRepository.findByOrganizer_Id(organizerId);
    }

    @Transactional(readOnly = true)
    public Promotion getPromotionByCode(String code) {

        String normalizedCode = normalizeCode(code);

        return promotionRepository.findByCode(normalizedCode)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Code promo introuvable : " + code));
    }


    public Promotion updatePromotion(
            Long id,
            Promotion updatedPromotion) {

        Promotion existing = getPromotionById(id);

        validatePromotion(updatedPromotion);

        String code = normalizeCode(updatedPromotion.getCode());

        if (promotionRepository.existsByCodeAndIdNot(code, id)) {
            throw new IllegalArgumentException(
                    "Ce code promo est déjà utilisé.");
        }

        int usageLimit = updatedPromotion.getUsageLimit();
        int usageCount = existing.getUsageCount();

        if (usageLimit > 0 && usageLimit < usageCount) {
            throw new IllegalArgumentException(
                    "La limite ne peut pas être inférieure au nombre d'utilisations déjà enregistré.");
        }

        existing.setCode(code);
        existing.setDiscountType(updatedPromotion.getDiscountType());
        existing.setDiscountValue(updatedPromotion.getDiscountValue());
        existing.setStartDate(updatedPromotion.getStartDate());
        existing.setEndDate(updatedPromotion.getEndDate());
        existing.setUsageLimit(usageLimit);


        return promotionRepository.save(existing);
    }


    public void deletePromotion(Long id) {

        Promotion promotion = getPromotionById(id);

        promotionRepository.delete(promotion);
    }

    @Transactional(readOnly = true)
    public boolean isPromotionValid(String code) {

        Promotion promotion = getPromotionByCode(code);

        return promotion.isValid();
    }


    @Transactional(readOnly = true)
    public BigDecimal calculateDiscount(
            String code,
            BigDecimal subtotal) {

        if (subtotal == null || subtotal.signum() < 0) {
            throw new IllegalArgumentException(
                    "Le sous-total ne peut pas être négatif.");
        }

        Promotion promotion = getPromotionByCode(code);

        if (!promotion.isValid()) {
            throw new IllegalArgumentException(
                    "Ce code promo est expiré ou n'est plus utilisable.");
        }

        return promotion.calculateDiscount(subtotal);
    }


    private void validatePromotion(Promotion promotion) {

        if (promotion == null) {
            throw new IllegalArgumentException(
                    "Les informations de la promotion sont obligatoires.");
        }

        if (promotion.getCode() == null
                || promotion.getCode().isBlank()) {
            throw new IllegalArgumentException(
                    "Le code promo est obligatoire.");
        }

        if (promotion.getDiscountType() == null) {
            throw new IllegalArgumentException(
                    "Le type de réduction est obligatoire.");
        }

        BigDecimal value = promotion.getDiscountValue();

        if (value == null || value.signum() <= 0) {
            throw new IllegalArgumentException(
                    "La réduction doit être supérieure à zéro.");
        }

        if (promotion.getDiscountType() == DiscountType.PERCENTAGE
                && value.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException(
                    "Le pourcentage ne peut pas dépasser 100.");
        }

        LocalDate start = promotion.getStartDate();
        LocalDate end = promotion.getEndDate();

        if (start != null && end != null && end.isBefore(start)) {
            throw new IllegalArgumentException(
                    "La date de fin doit être postérieure ou égale à la date de début.");
        }

        if (promotion.getUsageLimit() < 0) {
            throw new IllegalArgumentException(
                    "La limite d'utilisation ne peut pas être négative.");
        }
    }


    private String normalizeCode(String code) {

        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException(
                    "Le code promo est obligatoire.");
        }

        return code.trim().toUpperCase(Locale.ROOT);
    }


}
