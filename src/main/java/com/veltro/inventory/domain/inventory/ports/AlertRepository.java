package com.veltro.inventory.domain.inventory.ports;

import com.veltro.inventory.domain.inventory.model.AlertEntity;
import com.veltro.inventory.domain.inventory.model.AlertType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AlertRepository {

    AlertEntity save(AlertEntity alert);

    Optional<AlertEntity> findByIdAndActiveTrue(Long id);

    List<AlertEntity> findByProductIdAndResolvedFalse(Long productId);

    boolean existsByProductIdAndResolvedFalseAndType(Long productId, AlertType type);

    Page<AlertEntity> findByResolvedFalseOrderBySeverityDescCreatedAtAsc(Pageable pageable);

    Page<AlertEntity> findByReadFalseAndResolvedFalseOrderBySeverityDescCreatedAtAsc(Pageable pageable);

    long countByReadFalseAndResolvedFalse();
}
