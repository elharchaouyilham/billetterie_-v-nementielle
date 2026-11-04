package com.example.eventpro.repository;

import com.example.eventpro.entity.TicketCategoriesPerEvent;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TicketCategoriesPerEventRepository
        extends JpaRepository<TicketCategoriesPerEvent, Long> {

    List<TicketCategoriesPerEvent> findByEvent_Id(Long eventId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT category
            FROM TicketCategoriesPerEvent category
            WHERE category.id = :categoryId
            """)
    Optional<TicketCategoriesPerEvent> findByIdForUpdate(
            @Param("categoryId") Long categoryId);
}