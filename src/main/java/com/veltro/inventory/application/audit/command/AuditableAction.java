package com.veltro.inventory.application.audit.command;

/**
 * Functional interface for auditable operations (B3-03).
 * 
 * <p>Part of the Command Pattern functional implementation. This interface replaces
 * concrete command classes (e.g., ConfirmSaleCommand, VoidSaleCommand) with lambdas,
 * avoiding class proliferation while maintaining clean separation of concerns.
 * 
 * <p>The append-only nature of audit logging does not require undo/redo, queuing,
 * or multi-method command objects — the three core reasons GoF Command uses concrete
 * classes. This functional approach keeps integration with services clean.
 * 
 * @param <T> the result type of the operation
 * @see AuditCommandExecutor
 */
@FunctionalInterface
public interface AuditableAction<T> {
    
    /**
     * Executes the auditable operation.
     * 
     * @return the operation result
     * @throws RuntimeException if the operation fails
     */
    T execute();
}
