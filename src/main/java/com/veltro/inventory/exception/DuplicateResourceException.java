package com.veltro.inventory.exception;

/**
 * Thrown when attempting to create a resource that already exists 
 * (e.g., duplicate tax ID, barcode, etc.). Maps to HTTP 409 Conflict.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }

    public DuplicateResourceException(String resourceName, String field, String value) {
        super(resourceName + " with " + field + " '" + value + "' already exists");
    }
}