package com.example.eventpro.scheduler;

import com.example.eventpro.service.ReservationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReservationExpirationScheduler {

    private static final Logger logger =
            LoggerFactory.getLogger(ReservationExpirationScheduler.class);

    private final ReservationService reservationService;

    public ReservationExpirationScheduler(
            ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @Scheduled(
            fixedDelayString =
                    "${eventpro.reservations.expiration-check-ms:10000}"
    )
    public void expireReservations() {

        int expiredCount = reservationService.expireReservations();

        if (expiredCount > 0) {
            logger.info(
                    "{} réservation(s) expirée(s) et places libérées.",
                    expiredCount);
        }
    }
}
