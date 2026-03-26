package com.veltro.inventory.domain.audit.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for AuditRecordEntity (B3-03).
 * 
 * <p>Tests verify the immutable append-only nature of audit records.
 */
class AuditRecordEntityTest {

    @Test
    void shouldCreateAuditRecordWithAllFields() {
        // Given
        Instant now = Instant.now();
        String beforeJson = "{\"status\":\"PENDING\"}";
        String afterJson = "{\"status\":\"CONFIRMED\"}";

        // When
        AuditRecordEntity audit = new AuditRecordEntity();
        audit.setEntityType(AuditEntityType.SALE);
        audit.setEntityId(123L);
        audit.setAction(AuditAction.CONFIRM);
        audit.setUsername("john.doe");
        audit.setIpAddress("192.168.1.100");
        audit.setCreatedAt(now);
        audit.setPreviousData(beforeJson);
        audit.setNewData(afterJson);

        // Then
        assertThat(audit.getEntityType()).isEqualTo(AuditEntityType.SALE);
        assertThat(audit.getEntityId()).isEqualTo(123L);
        assertThat(audit.getAction()).isEqualTo(AuditAction.CONFIRM);
        assertThat(audit.getUsername()).isEqualTo("john.doe");
        assertThat(audit.getIpAddress()).isEqualTo("192.168.1.100");
        assertThat(audit.getCreatedAt()).isEqualTo(now);
        assertThat(audit.getPreviousData()).isEqualTo(beforeJson);
        assertThat(audit.getNewData()).isEqualTo(afterJson);
    }

    @Test
    void shouldAllowNullPreviousDataForCreations() {
        // Given
        AuditRecordEntity audit = new AuditRecordEntity();
        audit.setEntityType(AuditEntityType.SALE);
        audit.setEntityId(1L);
        audit.setAction(AuditAction.CONFIRM);
        audit.setPreviousData(null);
        audit.setNewData("{\"status\":\"CONFIRMED\"}");

        // Then
        assertThat(audit.getPreviousData()).isNull();
        assertThat(audit.getNewData()).isNotNull();
    }

    @Test
    void shouldAllowNullNewDataForDeletions() {
        // Given
        AuditRecordEntity audit = new AuditRecordEntity();
        audit.setEntityType(AuditEntityType.PURCHASE_ORDER);
        audit.setEntityId(1L);
        audit.setAction(AuditAction.VOID);
        audit.setPreviousData("{\"status\":\"PENDING\"}");
        audit.setNewData(null);

        // Then
        assertThat(audit.getPreviousData()).isNotNull();
        assertThat(audit.getNewData()).isNull();
    }
}
