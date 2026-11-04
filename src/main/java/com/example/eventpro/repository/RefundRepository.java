package com.example.eventpro.repository;

import com.example.eventpro.entity.Refund;
import com.example.eventpro.enums.RefundStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RefundRepository
        extends JpaRepository<Refund, Long> {

    List<Refund> findByPayment_Id(Long paymentId);

    List<Refund> findByStatus(RefundStatus status);
}
