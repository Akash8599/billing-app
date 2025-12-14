package com.billingsystem.exceptions;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handle ResourceNotFoundException
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorDetails> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            WebRequest request
    ) {
        log.error("Resource not found: {}", ex.getMessage());

        ErrorDetails errorDetails = ErrorDetails.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .message(ex.getMessage())
                .details(request.getDescription(false))
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle InsufficientStockException
     */
    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorDetails> handleInsufficientStockException(
            InsufficientStockException ex,
            WebRequest request
    ) {
        log.error("Insufficient stock: {}", ex.getMessage());

        ErrorDetails errorDetails = ErrorDetails.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message(ex.getMessage())
                .details(request.getDescription(false))
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDetails> handleValidationException(
            MethodArgumentNotValidException ex,
            WebRequest request
    ) {
        log.error("Validation error: {}", ex.getMessage());

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage())
        );

        ErrorDetails errorDetails = ErrorDetails.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message("Validation failed")
                .details("Field errors: " + fieldErrors)
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle IllegalArgumentException
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDetails> handleIllegalArgumentException(
            IllegalArgumentException ex,
            WebRequest request
    ) {
        log.error("Invalid argument: {}", ex.getMessage());

        ErrorDetails errorDetails = ErrorDetails.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message(ex.getMessage())
                .details(request.getDescription(false))
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle generic exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleGlobalException(
            Exception ex,
            WebRequest request
    ) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);

        ErrorDetails errorDetails = ErrorDetails.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("An unexpected error occurred")
                .details(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Error details DTO
     */
    public static class ErrorDetails {
        private int status;
        private String message;
        private String details;
        private LocalDateTime timestamp;

        public ErrorDetails(int status, String message, String details, LocalDateTime timestamp) {
            this.status = status;
            this.message = message;
            this.details = details;
            this.timestamp = timestamp;
        }

        public static ErrorDetailsBuilder builder() {
            return new ErrorDetailsBuilder();
        }

        // Getters
        public int getStatus() { return status; }
        public String getMessage() { return message; }
        public String getDetails() { return details; }
        public LocalDateTime getTimestamp() { return timestamp; }

        // Builder class
        public static class ErrorDetailsBuilder {
            private int status;
            private String message;
            private String details;
            private LocalDateTime timestamp;

            public ErrorDetailsBuilder status(int status) {
                this.status = status;
                return this;
            }

            public ErrorDetailsBuilder message(String message) {
                this.message = message;
                return this;
            }

            public ErrorDetailsBuilder details(String details) {
                this.details = details;
                return this;
            }

            public ErrorDetailsBuilder timestamp(LocalDateTime timestamp) {
                this.timestamp = timestamp;
                return this;
            }

            public ErrorDetails build() {
                return new ErrorDetails(status, message, details, timestamp);
            }
        }
    }
}