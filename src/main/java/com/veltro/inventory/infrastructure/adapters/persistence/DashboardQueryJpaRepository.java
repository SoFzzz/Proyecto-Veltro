package com.veltro.inventory.infrastructure.adapters.persistence;

import com.veltro.inventory.application.dashboard.dto.DashboardResponse;
import com.veltro.inventory.application.dashboard.service.DashboardQueryRepository;
import com.veltro.inventory.domain.inventory.model.AlertType;
import com.veltro.inventory.domain.pos.model.SaleStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * JPA implementation of dashboard queries (B3-02).
 *
 * <p>Uses JPQL queries optimized for dashboard KPIs.
 */
@Slf4j
@Repository
public class DashboardQueryJpaRepository implements DashboardQueryRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public BigDecimal sumTodaySales(LocalDateTime startOfDay, LocalDateTime endOfDay) {
        return entityManager.createQuery(
                        "SELECT COALESCE(SUM(s.total), 0) FROM SaleEntity s " +
                                "WHERE s.status = :status " +
                                "AND s.completedAt BETWEEN :start AND :end",
                        BigDecimal.class)
                .setParameter("status", SaleStatus.COMPLETED)
                .setParameter("start", startOfDay)
                .setParameter("end", endOfDay)
                .getSingleResult();
    }

    @Override
    public long countTodaySales(LocalDateTime startOfDay, LocalDateTime endOfDay) {
        return entityManager.createQuery(
                        "SELECT COUNT(s) FROM SaleEntity s " +
                                "WHERE s.status = :status " +
                                "AND s.completedAt BETWEEN :start AND :end",
                        Long.class)
                .setParameter("status", SaleStatus.COMPLETED)
                .setParameter("start", startOfDay)
                .setParameter("end", endOfDay)
                .getSingleResult();
    }

    @Override
    public BigDecimal sumSalesBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return entityManager.createQuery(
                        "SELECT COALESCE(SUM(s.total), 0) FROM SaleEntity s " +
                                "WHERE s.status = :status " +
                                "AND s.completedAt BETWEEN :start AND :end",
                        BigDecimal.class)
                .setParameter("status", SaleStatus.COMPLETED)
                .setParameter("start", startDate)
                .setParameter("end", endDate)
                .getSingleResult();
    }

    @Override
    public List<DashboardResponse.OutOfStockProduct> findOutOfStockProducts() {
        @SuppressWarnings("unchecked")
        List<Object[]> results = entityManager.createQuery(
                        "SELECT p.id, p.name, p.sku FROM InventoryEntity i " +
                                "JOIN i.product p " +
                                "WHERE i.currentStock = 0 AND i.active = true")
                .getResultList();

        return results.stream()
                .map(row -> new DashboardResponse.OutOfStockProduct(
                        (Long) row[0],
                        (String) row[1],
                        (String) row[2]))
                .toList();
    }

    @Override
    public long countActiveAlertsByType(AlertType type) {
        return entityManager.createQuery(
                        "SELECT COUNT(a) FROM AlertEntity a " +
                                "WHERE a.type = :type AND a.resolved = false",
                        Long.class)
                .setParameter("type", type)
                .getSingleResult();
    }

    @Override
    public List<DashboardResponse.RecentSale> findRecentSales(int limit) {
        @SuppressWarnings("unchecked")
        List<Object[]> results = entityManager.createQuery(
                        "SELECT s.id, s.saleNumber, s.total, SIZE(s.details), s.cashierId, s.completedAt " +
                                "FROM SaleEntity s " +
                                "WHERE s.status = :status " +
                                "ORDER BY s.completedAt DESC")
                .setParameter("status", SaleStatus.COMPLETED)
                .setMaxResults(limit)
                .getResultList();

        return results.stream()
                .map(row -> new DashboardResponse.RecentSale(
                        (Long) row[0],
                        (String) row[1],
                        (BigDecimal) row[2],
                        (Integer) row[3],
                        (Long) row[4],
                        row[5] != null ? ((LocalDateTime) row[5]).atOffset(ZoneOffset.UTC) : null))
                .toList();
    }
}
