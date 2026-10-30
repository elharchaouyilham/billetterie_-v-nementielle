package com.example.eventpro.repository;

import com.example.eventpro.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PromotionRepository
        extends JpaRepository<Promotion, Long> {

    boolean existsByCode(String code);

    Optional<Promotion> findByCode(String code);

    List<Promotion> findByOrganizer_Id(Long organizerId);

    boolean existsByCodeAndIdNot(String code, Long id);
}