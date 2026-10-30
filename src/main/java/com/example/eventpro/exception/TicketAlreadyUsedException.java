package com.example.eventpro.exception;

public class TicketAlreadyUsedException extends RuntimeException {

    public TicketAlreadyUsedException(String message) {
        super(message);
    }
}