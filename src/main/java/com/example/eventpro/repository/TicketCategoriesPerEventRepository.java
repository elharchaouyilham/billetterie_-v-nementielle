package com.example.eventpro.repository;

import com.example.eventpro.entity.TicketCategoriesPerEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketCategoriesPerEventRepository
        extends JpaRepository<TicketCategoriesPerEvent, Long> {

    List<TicketCategoriesPerEvent> findByEvent_Id(Long eventId);
}