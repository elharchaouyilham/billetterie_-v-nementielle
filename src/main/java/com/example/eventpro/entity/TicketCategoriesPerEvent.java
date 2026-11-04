package com.example.eventpro.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "ticket_categories_per_event")
@Getter
@Setter
@NoArgsConstructor
public class TicketCategoriesPerEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private TicketCategory category;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private int totalQuantity;

    @Column(nullable = false)
    private int maxQuotaPerClient;

    @Column(nullable = false)
    private int reservedQuantity;

    public int getAvailableQuantity() {
        return totalQuantity - reservedQuantity;
    }
}