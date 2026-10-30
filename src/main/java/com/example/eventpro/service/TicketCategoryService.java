package com.example.eventpro.service;

import com.example.eventpro.entity.Event;
import com.example.eventpro.entity.TicketCategory;
import com.example.eventpro.entity.TicketCategoriesPerEvent;
import com.example.eventpro.enums.EventStatus;

import com.example.eventpro.repository.EventRepository;
import com.example.eventpro.repository.TicketCategoryRepository;
import com.example.eventpro.repository.TicketCategoriesPerEventRepository;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class TicketCategoryService {

    private final TicketCategoryRepository categoryRepository;
    private final TicketCategoriesPerEventRepository eventCategoryRepository;
    private final EventRepository eventRepository;

    public TicketCategoryService(
            TicketCategoryRepository categoryRepository,
            TicketCategoriesPerEventRepository eventCategoryRepository,
            EventRepository eventRepository) {

        this.categoryRepository = categoryRepository;
        this.eventCategoryRepository = eventCategoryRepository;
        this.eventRepository = eventRepository;
    }


    public TicketCategory createCategory(String name) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Le nom de la catégorie est obligatoire.");
        }

        TicketCategory category = new TicketCategory();
        category.setName(name.trim());

        return categoryRepository.save(category);
    }


    @Transactional(readOnly = true)
    public List<TicketCategory> getAllCategories() {
        return categoryRepository.findAll();
    }

    public TicketCategoriesPerEvent addCategoryToEvent(
            Long eventId,
            Long categoryId,
            BigDecimal unitPrice,
            int totalQuantity,
            int maxQuotaPerClient) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Événement introuvable : " + eventId));

        TicketCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Catégorie introuvable : " + categoryId));

        if (event.getStatus() != EventStatus.DRAFT) {
            throw new IllegalStateException(
                    "Les catégories peuvent être ajoutées uniquement aux événements en brouillon.");
        }

        if (unitPrice == null || unitPrice.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Le prix doit être supérieur à zéro.");
        }

        if (totalQuantity <= 0) {
            throw new IllegalArgumentException(
                    "La quantité totale doit être supérieure à zéro.");
        }

        if (maxQuotaPerClient <= 0
                || maxQuotaPerClient > totalQuantity) {
            throw new IllegalArgumentException(
                    "Le quota par client doit être positif et ne pas dépasser la quantité totale.");
        }

        TicketCategoriesPerEvent eventCategory =
                new TicketCategoriesPerEvent();

        eventCategory.setEvent(event);
        eventCategory.setCategory(category);
        eventCategory.setUnitPrice(unitPrice);
        eventCategory.setTotalQuantity(totalQuantity);
        eventCategory.setMaxQuotaPerClient(maxQuotaPerClient);

        event.getCategories().add(eventCategory);

        return eventCategoryRepository.save(eventCategory);
    }


    @Transactional(readOnly = true)
    public List<TicketCategoriesPerEvent> getCategoriesByEvent(
            Long eventId) {

        if (!eventRepository.existsById(eventId)) {
            throw new EntityNotFoundException(
                    "Événement introuvable : " + eventId);
        }

        return eventCategoryRepository.findByEvent_Id(eventId);
    }
}