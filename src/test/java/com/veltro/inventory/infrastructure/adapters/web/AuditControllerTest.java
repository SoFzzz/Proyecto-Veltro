package com.veltro.inventory.infrastructure.adapters.web;

import com.veltro.inventory.application.audit.dto.AuditFilterRequest;
import com.veltro.inventory.application.audit.dto.AuditRecordResponse;
import com.veltro.inventory.application.audit.service.ForensicAuditService;
import com.veltro.inventory.domain.audit.model.AuditAction;
import com.veltro.inventory.domain.audit.model.AuditEntityType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AuditController (B3-03).
 */
@ExtendWith(MockitoExtension.class)
class AuditControllerTest {

    @Mock
    private ForensicAuditService auditService;

    private AuditController auditController;

    @BeforeEach
    void setUp() {
        auditController = new AuditController(auditService);
    }

    @Test
    void shouldGetAllAuditRecords() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        AuditRecordResponse response = createAuditResponse(1L, AuditEntityType.SALE, 100L, AuditAction.CONFIRM);
        Page<AuditRecordResponse> page = new PageImpl<>(List.of(response), pageable, 1);

        when(auditService.findAll(any(AuditFilterRequest.class), any(Pageable.class))).thenReturn(page);

        // When
        AuditFilterRequest filter = AuditFilterRequest.empty();
        Page<AuditRecordResponse> result = auditController.findAll(filter, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).id()).isEqualTo(1L);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void shouldFilterByEntityType() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        AuditRecordResponse response = createAuditResponse(1L, AuditEntityType.SALE, 100L, AuditAction.CONFIRM);
        Page<AuditRecordResponse> page = new PageImpl<>(List.of(response), pageable, 1);

        when(auditService.findAll(any(AuditFilterRequest.class), any(Pageable.class))).thenReturn(page);

        // When
        AuditFilterRequest filter = new AuditFilterRequest(AuditEntityType.SALE, null, null, null, null);
        Page<AuditRecordResponse> result = auditController.findAll(filter, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent().get(0).entityType()).isEqualTo(AuditEntityType.SALE);
    }

    @Test
    void shouldFilterByAction() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        AuditRecordResponse response = createAuditResponse(1L, AuditEntityType.SALE, 100L, AuditAction.VOID);
        Page<AuditRecordResponse> page = new PageImpl<>(List.of(response), pageable, 1);

        when(auditService.findAll(any(AuditFilterRequest.class), any(Pageable.class))).thenReturn(page);

        // When
        AuditFilterRequest filter = new AuditFilterRequest(null, AuditAction.VOID, null, null, null);
        Page<AuditRecordResponse> result = auditController.findAll(filter, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent().get(0).action()).isEqualTo(AuditAction.VOID);
    }

    @Test
    void shouldFilterByUsername() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        AuditRecordResponse response = new AuditRecordResponse(
                1L,
                AuditEntityType.SALE,
                100L,
                AuditAction.CONFIRM,
                "{\"before\":\"data\"}",
                "{\"after\":\"data\"}",
                "john.doe",
                "192.168.1.1",
                Instant.now()
        );
        Page<AuditRecordResponse> page = new PageImpl<>(List.of(response), pageable, 1);

        when(auditService.findAll(any(AuditFilterRequest.class), any(Pageable.class))).thenReturn(page);

        // When
        AuditFilterRequest filter = new AuditFilterRequest(null, null, "john.doe", null, null);
        Page<AuditRecordResponse> result = auditController.findAll(filter, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent().get(0).username()).isEqualTo("john.doe");
    }

    @Test
    void shouldGetAuditRecordById() {
        // Given
        AuditRecordResponse response = createAuditResponse(1L, AuditEntityType.SALE, 100L, AuditAction.CONFIRM);

        when(auditService.findById(1L)).thenReturn(response);

        // When
        ResponseEntity<AuditRecordResponse> result = auditController.findById(1L);

        // Then
        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().id()).isEqualTo(1L);
        assertThat(result.getBody().entityType()).isEqualTo(AuditEntityType.SALE);
        assertThat(result.getBody().action()).isEqualTo(AuditAction.CONFIRM);
    }

    @Test
    void shouldGetAuditRecordsForEntity() {
        // Given
        AuditRecordResponse response = createAuditResponse(1L, AuditEntityType.SALE, 100L, AuditAction.CONFIRM);
        List<AuditRecordResponse> list = List.of(response);

        when(auditService.findByEntityTypeAndEntityId(AuditEntityType.SALE, 100L)).thenReturn(list);

        // When
        ResponseEntity<List<AuditRecordResponse>> result = auditController.findByEntity(AuditEntityType.SALE, 100L);

        // Then
        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody()).hasSize(1);
        assertThat(result.getBody().get(0).entityId()).isEqualTo(100L);
    }

    // Helper method
    private AuditRecordResponse createAuditResponse(Long id, AuditEntityType entityType, Long entityId, AuditAction action) {
        return new AuditRecordResponse(
                id,
                entityType,
                entityId,
                action,
                "{\"status\":\"before\"}",
                "{\"status\":\"after\"}",
                "testuser",
                "192.168.1.1",
                Instant.now()
        );
    }
}