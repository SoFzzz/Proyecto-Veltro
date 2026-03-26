package com.veltro.inventory.application.audit.service;

import com.veltro.inventory.application.audit.dto.AuditFilterRequest;
import com.veltro.inventory.application.audit.dto.AuditRecordResponse;
import com.veltro.inventory.application.audit.mapper.AuditRecordMapper;
import com.veltro.inventory.domain.audit.model.AuditAction;
import com.veltro.inventory.domain.audit.model.AuditEntityType;
import com.veltro.inventory.domain.audit.model.AuditRecordEntity;
import com.veltro.inventory.domain.audit.ports.AuditRecordRepository;
import com.veltro.inventory.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ForensicAuditService (B3-03).
 */
@ExtendWith(MockitoExtension.class)
class ForensicAuditServiceTest {

    @Mock
    private AuditRecordRepository auditRepository;

    @Mock
    private AuditRecordMapper mapper;

    private ForensicAuditService auditService;

    @BeforeEach
    void setUp() {
        auditService = new ForensicAuditService(auditRepository, mapper);
    }

    @Test
    void shouldFindAllAuditRecordsWithFilters() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        AuditFilterRequest filter = AuditFilterRequest.empty();
        
        AuditRecordEntity entity1 = createAuditEntity(1L, AuditEntityType.SALE, 100L, AuditAction.CONFIRM);
        AuditRecordEntity entity2 = createAuditEntity(2L, AuditEntityType.PURCHASE_ORDER, 200L, AuditAction.RECEIVE);
        Page<AuditRecordEntity> entityPage = new PageImpl<>(List.of(entity1, entity2), pageable, 2);

        AuditRecordResponse response1 = createAuditResponse(1L, AuditEntityType.SALE, 100L, AuditAction.CONFIRM);
        AuditRecordResponse response2 = createAuditResponse(2L, AuditEntityType.PURCHASE_ORDER, 200L, AuditAction.RECEIVE);

        when(auditRepository.findByFilters(null, null, null, null, null, pageable))
                .thenReturn(entityPage);
        when(mapper.toResponse(entity1)).thenReturn(response1);
        when(mapper.toResponse(entity2)).thenReturn(response2);

        // When
        Page<AuditRecordResponse> result = auditService.findAll(filter, pageable);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).id()).isEqualTo(1L);
        assertThat(result.getContent().get(1).id()).isEqualTo(2L);
    }

    @Test
    void shouldFilterByEntityType() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        AuditFilterRequest filter = new AuditFilterRequest(AuditEntityType.SALE, null, null, null, null);

        AuditRecordEntity entity = createAuditEntity(1L, AuditEntityType.SALE, 100L, AuditAction.CONFIRM);
        Page<AuditRecordEntity> entityPage = new PageImpl<>(List.of(entity), pageable, 1);

        AuditRecordResponse response = createAuditResponse(1L, AuditEntityType.SALE, 100L, AuditAction.CONFIRM);

        when(auditRepository.findByFilters(eq(AuditEntityType.SALE), eq(null), eq(null), eq(null), eq(null), eq(pageable)))
                .thenReturn(entityPage);
        when(mapper.toResponse(entity)).thenReturn(response);

        // When
        Page<AuditRecordResponse> result = auditService.findAll(filter, pageable);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).entityType()).isEqualTo(AuditEntityType.SALE);
    }

    @Test
    void shouldFilterByAction() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        AuditFilterRequest filter = new AuditFilterRequest(null, AuditAction.VOID, null, null, null);

        AuditRecordEntity entity = createAuditEntity(1L, AuditEntityType.SALE, 100L, AuditAction.VOID);
        Page<AuditRecordEntity> entityPage = new PageImpl<>(List.of(entity), pageable, 1);

        AuditRecordResponse response = createAuditResponse(1L, AuditEntityType.SALE, 100L, AuditAction.VOID);

        when(auditRepository.findByFilters(eq(null), eq(AuditAction.VOID), eq(null), eq(null), eq(null), eq(pageable)))
                .thenReturn(entityPage);
        when(mapper.toResponse(entity)).thenReturn(response);

        // When
        Page<AuditRecordResponse> result = auditService.findAll(filter, pageable);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).action()).isEqualTo(AuditAction.VOID);
    }

    @Test
    void shouldFilterByUsername() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        AuditFilterRequest filter = new AuditFilterRequest(null, null, "john.doe", null, null);

        AuditRecordEntity entity = createAuditEntity(1L, AuditEntityType.SALE, 100L, AuditAction.CONFIRM);
        entity.setUsername("john.doe");
        Page<AuditRecordEntity> entityPage = new PageImpl<>(List.of(entity), pageable, 1);

        AuditRecordResponse response = createAuditResponse(1L, AuditEntityType.SALE, 100L, AuditAction.CONFIRM);

        when(auditRepository.findByFilters(eq(null), eq(null), eq("john.doe"), eq(null), eq(null), eq(pageable)))
                .thenReturn(entityPage);
        when(mapper.toResponse(entity)).thenReturn(response);

        // When
        Page<AuditRecordResponse> result = auditService.findAll(filter, pageable);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(auditRepository).findByFilters(null, null, "john.doe", null, null, pageable);
    }

    @Test
    void shouldFilterByDateRange() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        Instant from = Instant.parse("2025-01-01T00:00:00Z");
        Instant to = Instant.parse("2025-12-31T23:59:59Z");
        AuditFilterRequest filter = new AuditFilterRequest(null, null, null, from, to);

        AuditRecordEntity entity = createAuditEntity(1L, AuditEntityType.INVENTORY, 50L, AuditAction.ADJUST);
        entity.setCreatedAt(Instant.parse("2025-06-15T12:00:00Z"));
        Page<AuditRecordEntity> entityPage = new PageImpl<>(List.of(entity), pageable, 1);

        AuditRecordResponse response = createAuditResponse(1L, AuditEntityType.INVENTORY, 50L, AuditAction.ADJUST);

        when(auditRepository.findByFilters(eq(null), eq(null), eq(null), eq(from), eq(to), eq(pageable)))
                .thenReturn(entityPage);
        when(mapper.toResponse(entity)).thenReturn(response);

        // When
        Page<AuditRecordResponse> result = auditService.findAll(filter, pageable);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void shouldFilterByMultipleCriteria() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        Instant from = Instant.parse("2025-01-01T00:00:00Z");
        AuditFilterRequest filter = new AuditFilterRequest(
                AuditEntityType.PURCHASE_ORDER,
                AuditAction.RECEIVE,
                "warehouse.user",
                from,
                null
        );

        AuditRecordEntity entity = createAuditEntity(1L, AuditEntityType.PURCHASE_ORDER, 200L, AuditAction.RECEIVE);
        entity.setUsername("warehouse.user");
        entity.setCreatedAt(Instant.parse("2025-03-10T10:30:00Z"));
        Page<AuditRecordEntity> entityPage = new PageImpl<>(List.of(entity), pageable, 1);

        AuditRecordResponse response = createAuditResponse(1L, AuditEntityType.PURCHASE_ORDER, 200L, AuditAction.RECEIVE);

        when(auditRepository.findByFilters(
                eq(AuditEntityType.PURCHASE_ORDER),
                eq(AuditAction.RECEIVE),
                eq("warehouse.user"),
                eq(from),
                eq(null),
                eq(pageable)
        )).thenReturn(entityPage);
        when(mapper.toResponse(entity)).thenReturn(response);

        // When
        Page<AuditRecordResponse> result = auditService.findAll(filter, pageable);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).entityType()).isEqualTo(AuditEntityType.PURCHASE_ORDER);
        assertThat(result.getContent().get(0).entityId()).isEqualTo(200L);
        assertThat(result.getContent().get(0).action()).isEqualTo(AuditAction.RECEIVE);
    }

    @Test
    void shouldReturnEmptyPageWhenNoResults() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        AuditFilterRequest filter = new AuditFilterRequest(AuditEntityType.SALE, null, null, null, null);

        Page<AuditRecordEntity> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(auditRepository.findByFilters(eq(AuditEntityType.SALE), eq(null), eq(null), eq(null), eq(null), eq(pageable)))
                .thenReturn(emptyPage);

        // When
        Page<AuditRecordResponse> result = auditService.findAll(filter, pageable);

        // Then
        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void shouldFindById() {
        // Given
        Long id = 1L;
        AuditRecordEntity entity = createAuditEntity(id, AuditEntityType.SALE, 100L, AuditAction.CONFIRM);
        AuditRecordResponse response = createAuditResponse(id, AuditEntityType.SALE, 100L, AuditAction.CONFIRM);

        when(auditRepository.findById(id)).thenReturn(Optional.of(entity));
        when(mapper.toResponse(entity)).thenReturn(response);

        // When
        AuditRecordResponse result = auditService.findById(id);

        // Then
        assertThat(result.id()).isEqualTo(id);
        assertThat(result.entityType()).isEqualTo(AuditEntityType.SALE);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenIdNotFound() {
        // Given
        Long id = 999L;
        when(auditRepository.findById(id)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> auditService.findById(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Audit record not found with ID: 999");
    }

    @Test
    void shouldFindByEntityTypeAndEntityId() {
        // Given
        AuditEntityType entityType = AuditEntityType.SALE;
        Long entityId = 100L;

        AuditRecordEntity entity1 = createAuditEntity(1L, entityType, entityId, AuditAction.CONFIRM);
        AuditRecordEntity entity2 = createAuditEntity(2L, entityType, entityId, AuditAction.VOID);
        List<AuditRecordEntity> entities = List.of(entity1, entity2);

        AuditRecordResponse response1 = createAuditResponse(1L, entityType, entityId, AuditAction.CONFIRM);
        AuditRecordResponse response2 = createAuditResponse(2L, entityType, entityId, AuditAction.VOID);

        when(auditRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId))
                .thenReturn(entities);
        when(mapper.toResponse(entity1)).thenReturn(response1);
        when(mapper.toResponse(entity2)).thenReturn(response2);

        // When
        List<AuditRecordResponse> result = auditService.findByEntityTypeAndEntityId(entityType, entityId);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).action()).isEqualTo(AuditAction.CONFIRM);
        assertThat(result.get(1).action()).isEqualTo(AuditAction.VOID);
    }

    // Helper methods

    private AuditRecordEntity createAuditEntity(Long id, AuditEntityType entityType, Long entityId, AuditAction action) {
        AuditRecordEntity entity = new AuditRecordEntity();
        entity.setId(id);
        entity.setEntityType(entityType);
        entity.setEntityId(entityId);
        entity.setAction(action);
        entity.setUsername("testuser");
        entity.setIpAddress("192.168.1.1");
        entity.setCreatedAt(Instant.now());
        entity.setPreviousData("{\"status\":\"before\"}");
        entity.setNewData("{\"status\":\"after\"}");
        return entity;
    }

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
