package com.billingsystem.exceptions;

/**
 * InvalidOrderException - Thrown when order data is invalid
 */
public class InvalidOrderException extends RuntimeException {
    public InvalidOrderException(String message) {
        super(message);
    }

    public InvalidOrderException(String message, Throwable cause) {
        super(message, cause);
    }
}
