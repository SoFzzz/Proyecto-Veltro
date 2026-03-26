package com.veltro.inventory.application.report.service;

import com.veltro.inventory.application.report.dto.ProfitabilityReport;
import com.veltro.inventory.application.report.dto.ReportType;
import com.veltro.inventory.application.report.exporter.ReportExporter;
import com.veltro.inventory.domain.pos.model.SaleStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Report service with Factory Method pattern for export (B3-02).
 *
 * <p>Generates profitability reports and uses the Factory Method pattern
 * to delegate export to the appropriate exporter (PDF, Excel).
 */
@Slf4j
@Service
public class ReportService {

    @PersistenceContext
    private EntityManager entityManager;

    private final Map<ReportType, ReportExporter> exporters;

    /**
     * Constructor injection of all exporters (Factory Method Pattern).
     *
     * @param exporterList list of available exporters
     */
    public ReportService(List<ReportExporter> exporterList) {
        this.exporters = exporterList.stream()
                .collect(Collectors.toMap(ReportExporter::getType, Function.identity()));
        log.info("ReportService initialized with {} exporters: {}",
                exporters.size(), exporters.keySet());
    }

    /**
     * Generates a profitability report for a date range.
     *
     * @param startDate start of the report period
     * @param endDate end of the report period
     * @return the profitability report
     */
    @Transactional(readOnly = true)
    public ProfitabilityReport generateProfitabilityReport(LocalDate startDate, LocalDate endDate) {
        log.info("Generating profitability report for {} to {}", startDate, endDate);

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        // Get total sales
        BigDecimal totalSales = entityManager.createQuery(
                        "SELECT COALESCE(SUM(s.total), 0) FROM SaleEntity s " +
                                "WHERE s.status = :status AND s.completedAt BETWEEN :start AND :end",
                        BigDecimal.class)
                .setParameter("status", SaleStatus.COMPLETED)
                .setParameter("start", start)
                .setParameter("end", end)
                .getSingleResult();

        // Count sales and items
        long salesCount = entityManager.createQuery(
                        "SELECT COUNT(s) FROM SaleEntity s " +
                                "WHERE s.status = :status AND s.completedAt BETWEEN :start AND :end",
                        Long.class)
                .setParameter("status", SaleStatus.COMPLETED)
                .setParameter("start", start)
                .setParameter("end", end)
                .getSingleResult();

        Long itemsSold = entityManager.createQuery(
                        "SELECT COALESCE(SUM(d.quantity), 0) FROM SaleDetailEntity d " +
                                "JOIN d.sale s " +
                                "WHERE s.status = :status AND s.completedAt BETWEEN :start AND :end",
                        Long.class)
                .setParameter("status", SaleStatus.COMPLETED)
                .setParameter("start", start)
                .setParameter("end", end)
                .getSingleResult();

        // Get product-level breakdown
        @SuppressWarnings("unchecked")
        List<Object[]> productResults = entityManager.createQuery(
                        "SELECT p.id, p.name, p.sku, SUM(d.quantity), SUM(d.subtotal), p.costPrice " +
                                "FROM SaleDetailEntity d " +
                                "JOIN d.sale s " +
                                "JOIN d.product p " +
                                "WHERE s.status = :status AND s.completedAt BETWEEN :start AND :end " +
                                "GROUP BY p.id, p.name, p.sku, p.costPrice " +
                                "ORDER BY SUM(d.subtotal) DESC")
                .setParameter("status", SaleStatus.COMPLETED)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();

        BigDecimal totalCost = BigDecimal.ZERO;
        List<ProfitabilityReport.ProductProfitability> productBreakdown = new java.util.ArrayList<>();

        for (Object[] row : productResults) {
            Long productId = (Long) row[0];
            String productName = (String) row[1];
            String sku = (String) row[2];
            int quantitySold = ((Number) row[3]).intValue();
            BigDecimal revenue = (BigDecimal) row[4];
            BigDecimal costPrice = row[5] != null ? (BigDecimal) row[5] : BigDecimal.ZERO;

            BigDecimal cost = costPrice.multiply(BigDecimal.valueOf(quantitySold));
            BigDecimal profit = revenue.subtract(cost);
            BigDecimal profitMargin = revenue.compareTo(BigDecimal.ZERO) > 0
                    ? profit.multiply(BigDecimal.valueOf(100)).divide(revenue, 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            totalCost = totalCost.add(cost);

            productBreakdown.add(new ProfitabilityReport.ProductProfitability(
                    productId, productName, sku, quantitySold,
                    revenue.setScale(2, RoundingMode.HALF_UP),
                    cost.setScale(2, RoundingMode.HALF_UP),
                    profit.setScale(2, RoundingMode.HALF_UP),
                    profitMargin
            ));
        }

        BigDecimal grossProfit = totalSales.subtract(totalCost);
        BigDecimal profitMargin = totalSales.compareTo(BigDecimal.ZERO) > 0
                ? grossProfit.multiply(BigDecimal.valueOf(100)).divide(totalSales, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        ProfitabilityReport report = new ProfitabilityReport(
                startDate, endDate,
                totalSales.setScale(2, RoundingMode.HALF_UP),
                totalCost.setScale(2, RoundingMode.HALF_UP),
                grossProfit.setScale(2, RoundingMode.HALF_UP),
                profitMargin,
                salesCount,
                itemsSold,
                productBreakdown
        );

        log.info("Profitability report generated: sales={}, revenue={}, profit={}",
                salesCount, totalSales, grossProfit);

        return report;
    }

    /**
     * Exports a profitability report to the specified format (Factory Method).
     *
     * @param report the report to export
     * @param type the export format
     * @return the exported report as bytes
     * @throws IllegalArgumentException if the export type is not supported
     */
    public byte[] exportReport(ProfitabilityReport report, ReportType type) {
        ReportExporter exporter = exporters.get(type);
        if (exporter == null) {
            throw new IllegalArgumentException("Unsupported export type: " + type);
        }
        log.info("Exporting report as {}", type);
        return exporter.export(report);
    }

    /**
     * Gets the content type for a report format.
     *
     * @param type the export format
     * @return the MIME content type
     */
    public String getContentType(ReportType type) {
        ReportExporter exporter = exporters.get(type);
        if (exporter == null) {
            throw new IllegalArgumentException("Unsupported export type: " + type);
        }
        return exporter.getContentType();
    }

    /**
     * Gets the file extension for a report format.
     *
     * @param type the export format
     * @return the file extension
     */
    public String getFileExtension(ReportType type) {
        ReportExporter exporter = exporters.get(type);
        if (exporter == null) {
            throw new IllegalArgumentException("Unsupported export type: " + type);
        }
        return exporter.getFileExtension();
    }
}
