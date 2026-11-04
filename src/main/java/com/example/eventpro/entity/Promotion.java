
package com.example.eventpro.entity;

import com.example.eventpro.enums.DiscountType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Entity
@Table(name = "promotions")
@Getter
@Setter
@NoArgsConstructor
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organizer_id", nullable = false)
    private Organizer organizer;

    @Column(nullable = false, unique = true)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountType discountType;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal discountValue;

    private LocalDate startDate;
    private LocalDate endDate;

    private int usageLimit;
    private int usageCount;

    public BigDecimal calculateDiscount(BigDecimal subtotal) {

        if (subtotal == null || subtotal.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Le sous-total doit être positif."
            );
        }

        if (discountValue == null || discountValue.signum() < 0) {
            throw new IllegalStateException(
                    "La valeur du discount est invalide."
            );
        }

        BigDecimal discount;

        if (discountType == DiscountType.PERCENTAGE) {
            discount = subtotal
                    .multiply(discountValue)
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        } else {
            discount = discountValue;
        }

        if (discount.compareTo(subtotal) > 0) {
            discount = subtotal;
        }

        return discount.setScale(2, RoundingMode.HALF_UP);
    }

    public boolean isValid() {

        LocalDate today = LocalDate.now();

        boolean dateValid =
                (startDate == null || !today.isBefore(startDate))
                        && (endDate == null || !today.isAfter(endDate));

        boolean usageValid =
                usageLimit <= 0 || usageCount < usageLimit;

        return dateValid && usageValid;
    }
}
