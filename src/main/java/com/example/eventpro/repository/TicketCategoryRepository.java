package com.example.eventpro.repository;

import com.example.eventpro.entity.TicketCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketCategoryRepository
        extends JpaRepository<TicketCategory, Long> {
}