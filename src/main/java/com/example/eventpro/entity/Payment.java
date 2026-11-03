package com.example.eventpro.entity;

import com.example.eventpro.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @Column(nullable = false, unique = true, length = 100)
    private String idempotencyKey;

    @Column(nullable = false, unique = true, length = 100)
    private String reference;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    private Instant paymentDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;

    @OneToMany(
            mappedBy = "payment",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Refund> refunds = new ArrayList<>();

    public void markAsSuccessful() {
        this.status = PaymentStatus.SUCCESS;
        this.paymentDate = Instant.now();
    }

    public void markAsFailed() {
        this.status = PaymentStatus.FAILED;
        this.paymentDate = Instant.now();
    }

    public void markAsRefunded() {
        this.status = PaymentStatus.REFUNDED;
    }

    public boolean isPending() {
        return status == PaymentStatus.PENDING;
    }

    public boolean isSuccessful() {
        return status == PaymentStatus.SUCCESS;
    }

    public boolean isFailed() {
        return status == PaymentStatus.FAILED;
    }

    public void addRefund(Refund refund) {
        if (refund == null) {
            return;
        }

        refund.setPayment(this);
        refunds.add(refund);
    }

    public void removeRefund(Refund refund) {
        if (refund == null) {
            return;
        }

        refunds.remove(refund);
        refund.setPayment(null);
    }
}