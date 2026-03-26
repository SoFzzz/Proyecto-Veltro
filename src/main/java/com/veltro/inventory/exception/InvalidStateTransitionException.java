package com.veltro.inventory.exception;

/**
 * Thrown when a state transition is requested but is not allowed
 * given the entity's current state (ADR-006 — State Pattern).
 * Maps to HTTP 422 Unprocessable Entity.
 *
 * Example: attempting to confirm a sale that is already COMPLETED.
 */
public class InvalidStateTransitionException extends RuntimeException {

    public InvalidStateTransitionException(String message) {
        super(message);
    }

    public InvalidStateTransitionException(String entityName, String currentState, String requestedAction) {
        super(entityName + " is already in state " + currentState
                + ". Action '" + requestedAction + "' is not allowed.");
    }
}
