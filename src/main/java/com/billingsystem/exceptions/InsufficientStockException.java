package com.billingsystem.exceptions;

/**
 * InsufficientStockException - Thrown when there's not enough stock
 */
public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String message) {
        super(message);
    }

    public InsufficientStockException(String message, Throwable cause) {
        super(message, cause);
    }
}
