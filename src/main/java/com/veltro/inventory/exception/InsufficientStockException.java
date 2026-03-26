package com.veltro.inventory.exception;

/**
 * Thrown when a sale or adjustment would push stock below zero (CA-04).
 * Maps to HTTP 422 Unprocessable Entity.
 *
 * Example: selling 10 units of a product that only has 5 in stock.
 */
public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(String message) {
        super(message);
    }

    public InsufficientStockException(String productName, int available, int requested) {
        super("Insufficient stock for '" + productName + "': available " + available
                + ", requested " + requested + ".");
    }
}
