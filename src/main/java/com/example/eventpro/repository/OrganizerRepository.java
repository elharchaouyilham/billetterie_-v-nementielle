package com.example.eventpro.repository;

import com.example.eventpro.entity.Organizer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizerRepository
        extends JpaRepository<Organizer, Long> {
}