package com.veltro.inventory.infrastructure.adapters.persistence;

import com.veltro.inventory.domain.inventory.model.AlertEntity;
import com.veltro.inventory.domain.inventory.model.AlertType;
import com.veltro.inventory.domain.inventory.ports.AlertRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlertJpaRepository extends JpaRepository<AlertEntity, Long>, AlertRepository {

    @Override
    Optional<AlertEntity> findByIdAndActiveTrue(Long id);

    @Override
    List<AlertEntity> findByProductIdAndResolvedFalse(Long productId);

    @Override
    boolean existsByProductIdAndResolvedFalseAndType(Long productId, AlertType type);

    @Override
    Page<AlertEntity> findByResolvedFalseOrderBySeverityDescCreatedAtAsc(Pageable pageable);

    @Override
    Page<AlertEntity> findByReadFalseAndResolvedFalseOrderBySeverityDescCreatedAtAsc(Pageable pageable);

    @Override
    long countByReadFalseAndResolvedFalse();
}
