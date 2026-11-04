package com.example.eventpro.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private ApiErrorResponse buildError(
            HttpStatus status,
            String message,
            String path) {

        return buildError(status, message, path, Map.of());
    }

    private ApiErrorResponse buildError(
            HttpStatus status,
            String message,
            String path,
            Map<String, String> fieldErrors) {

        return ApiErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(path)
                .fieldErrors(fieldErrors)
                .build();
    }

    @ExceptionHandler({
            ResourceNotFoundException.class,
            EntityNotFoundException.class
    })
    public ResponseEntity<ApiErrorResponse> handleNotFound(
            RuntimeException ex,
            HttpServletRequest request) {

        ApiErrorResponse response = buildError(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    @ExceptionHandler({
            IdempotencyConflictException.class,
            InsufficientStockException.class,
            ReservationExpiredException.class,
            TicketAlreadyUsedException.class,
            IllegalStateException.class,
            DataIntegrityViolationException.class
    })
    public ResponseEntity<ApiErrorResponse> handleConflict(
            RuntimeException ex,
            HttpServletRequest request) {

        ApiErrorResponse response = buildError(
                HttpStatus.CONFLICT,
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequest(
            IllegalArgumentException ex,
            HttpServletRequest request) {

        ApiErrorResponse response = buildError(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        Map<String, String> fieldErrors = new LinkedHashMap<>();

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.putIfAbsent(
                    error.getField(),
                    error.getDefaultMessage());
        }

        ApiErrorResponse response = buildError(
                HttpStatus.BAD_REQUEST,
                "Les données envoyées sont invalides.",
                request.getRequestURI(),
                fieldErrors
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }
}