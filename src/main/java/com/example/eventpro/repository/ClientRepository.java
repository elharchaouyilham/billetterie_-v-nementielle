package com.example.eventpro.repository;

import com.example.eventpro.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository
        extends JpaRepository<Client, Long> {
}