package com.veltro.inventory.domain.inventory.model;

/**
 * Alert severity levels used for ordering and UI badge coloring.
 */
public enum AlertSeverity {
    CRITICAL(3),
    WARNING(2),
    INFO(1);

    private final int priority;

    AlertSeverity(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
}
