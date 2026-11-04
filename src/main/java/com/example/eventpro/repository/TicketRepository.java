package com.example.eventpro.repository;

import com.example.eventpro.entity.Ticket;
import com.example.eventpro.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TicketRepository
        extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByCode(String code);

    boolean existsByCode(String code);

    List<Ticket> findByReservationLine_Reservation_Id(Long reservationId);

    List<Ticket> findByReservationLine_Reservation_Client_Id(Long clientId);

    Optional<Ticket> findByReservationLine_Id(Long reservationLineId);

    boolean existsByReservationLine_Id(Long reservationLineId);

    List<Ticket> findByStatus(TicketStatus status);

    long countByReservationLine_Reservation_Id(Long reservationId);
}
