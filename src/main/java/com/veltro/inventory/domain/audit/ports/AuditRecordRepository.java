package com.veltro.inventory.domain.audit.ports;

import com.veltro.inventory.domain.audit.model.AuditAction;
import com.veltro.inventory.domain.audit.model.AuditEntityType;
import com.veltro.inventory.domain.audit.model.AuditRecordEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository port for forensic audit records (B3-03).
 * 
 * <p>Hexagonal architecture — domain layer defines the port, infrastructure layer provides
 * the adapter (JPA implementation).
 */
public interface AuditRecordRepository {

    /**
     * Persists a new audit record.
     * 
     * @param record the audit record to save
     * @return the persisted entity with generated ID
     */
    AuditRecordEntity save(AuditRecordEntity record);

    /**
     * Finds an audit record by its ID.
     * 
     * @param id the audit record ID
     * @return the audit record if found
     */
    Optional<AuditRecordEntity> findById(Long id);

    /**
     * Finds all audit records for a specific entity instance.
     * 
     * @param entityType the type of entity
     * @param entityId the entity's ID
     * @return list of audit records ordered by createdAt DESC
     */
    List<AuditRecordEntity> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
            AuditEntityType entityType, Long entityId);

    /**
     * Finds audit records with filters and pagination.
     * 
     * @param entityType optional entity type filter
     * @param action optional action filter
     * @param username optional username filter
     * @param startDate optional start date (inclusive)
     * @param endDate optional end date (inclusive)
     * @param pageable pagination and sorting
     * @return page of audit records
     */
    Page<AuditRecordEntity> findByFilters(
            AuditEntityType entityType,
            AuditAction action,
            String username,
            Instant startDate,
            Instant endDate,
            Pageable pageable);
}
