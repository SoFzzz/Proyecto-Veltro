package com.veltro.inventory.exception;

/**
 * Thrown when a requested resource does not exist or is not visible
 * (e.g. soft-deleted). Maps to HTTP 404 Not Found.
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String resourceName, Long id) {
        super(resourceName + " not found with id: " + id);
    }

    public NotFoundException(String resourceName, String field, String value) {
        super(resourceName + " not found with " + field + ": " + value);
    }
}
