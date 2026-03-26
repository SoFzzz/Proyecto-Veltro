package com.veltro.inventory.application.audit.service;

import com.veltro.inventory.application.audit.dto.AuditFilterRequest;
import com.veltro.inventory.application.audit.dto.AuditRecordResponse;
import com.veltro.inventory.application.audit.mapper.AuditRecordMapper;
import com.veltro.inventory.domain.audit.model.AuditEntityType;
import com.veltro.inventory.domain.audit.ports.AuditRecordRepository;
import com.veltro.inventory.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for querying forensic audit records (B3-03).
 * 
 * <p>Provides filtered and paginated access to the append-only audit trail.
 * Supports filtering by entity type, action, username, and date range.
 * 
 * <p>All query methods are read-only — audit records cannot be updated or deleted.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ForensicAuditService {

    private final AuditRecordRepository auditRepository;
    private final AuditRecordMapper mapper;

    /**
     * Finds all audit records with optional filters and pagination.
     * 
     * <p>Results are ordered by severity (if applicable) and created date DESC,
     * consistent with ADR-003 (event ordering delegated to PostgreSQL).
     * 
     * @param filter filter criteria (entity type, action, username, date range)
     * @param pageable pagination and sorting parameters
     * @return page of audit records
     */
    public Page<AuditRecordResponse> findAll(AuditFilterRequest filter, Pageable pageable) {
        log.debug("Finding audit records with filters: {}", filter);

        Page<AuditRecordResponse> results = auditRepository.findByFilters(
                filter.entityType(),
                filter.action(),
                filter.username(),
                filter.startDate(),
                filter.endDate(),
                pageable
        ).map(mapper::toResponse);

        log.debug("Found {} audit records (page {} of {})",
                results.getNumberOfElements(), results.getNumber(), results.getTotalPages());

        return results;
    }

    /**
     * Finds a single audit record by ID.
     * 
     * @param id the audit record ID
     * @return the audit record
     * @throws NotFoundException if record not found
     */
    public AuditRecordResponse findById(Long id) {
        log.debug("Finding audit record by ID: {}", id);

        return auditRepository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Audit record not found with ID: " + id));
    }

    /**
     * Finds all audit records for a specific entity instance.
     * 
     * <p>Useful for viewing the complete audit trail of a single Sale, PurchaseOrder,
     * or Inventory entity.
     * 
     * @param entityType the type of entity
     * @param entityId the entity's ID
     * @return list of audit records ordered by created date DESC
     */
    public List<AuditRecordResponse> findByEntityTypeAndEntityId(
            AuditEntityType entityType, Long entityId) {

        log.debug("Finding audit records for {} with ID {}", entityType, entityId);

        List<AuditRecordResponse> records = auditRepository
                .findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId)
                .stream()
                .map(mapper::toResponse)
                .toList();

        log.debug("Found {} audit records for {} #{}", records.size(), entityType, entityId);

        return records;
    }
}
