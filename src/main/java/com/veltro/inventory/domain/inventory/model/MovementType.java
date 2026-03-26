package com.veltro.inventory.domain.inventory.model;

/**
 * Type of stock movement recorded in {@link InventoryMovementEntity}.
 *
 * ENTRY      — Stock increases: purchase order reception, manual addition.
 * EXIT       — Stock decreases: sale deduction, shrinkage.
 * ADJUSTMENT — Physical count correction; sets stock to an absolute value.
 */
public enum MovementType {
    ENTRY,
    EXIT,
    ADJUSTMENT
}
