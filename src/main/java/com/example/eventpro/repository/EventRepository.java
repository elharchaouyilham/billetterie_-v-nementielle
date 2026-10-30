package com.example.eventpro.repository;

import com.example.eventpro.entity.Event;
import com.example.eventpro.enums.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository
        extends JpaRepository<Event, Long> {

    List<Event> findByStatus(EventStatus status);
}