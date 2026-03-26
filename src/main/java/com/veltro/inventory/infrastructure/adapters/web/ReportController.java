package com.veltro.inventory.infrastructure.adapters.web;

import com.veltro.inventory.application.report.dto.ProfitabilityReport;
import com.veltro.inventory.application.report.dto.ReportType;
import com.veltro.inventory.application.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * REST controller for reports (B3-02).
 *
 * <p>Provides endpoints for:
 * <ul>
 *   <li>Profitability reports with date range</li>
 *   <li>Export to PDF and Excel formats (Factory Method Pattern)</li>
 * </ul>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /**
     * Generates a profitability report for a date range.
     *
     * <p>GET /api/v1/reports/profitability?startDate=2026-03-01&endDate=2026-03-22
     *
     * @param startDate start of the report period
     * @param endDate end of the report period
     * @return the profitability report as JSON
     */
    @GetMapping("/profitability")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ProfitabilityReport> getProfitabilityReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        log.info("Profitability report request: {} to {}", startDate, endDate);
        
        if (startDate.isAfter(endDate)) {
            return ResponseEntity.badRequest().build();
        }

        ProfitabilityReport report = reportService.generateProfitabilityReport(startDate, endDate);
        return ResponseEntity.ok(report);
    }

    /**
     * Exports a profitability report to PDF or Excel format.
     *
     * <p>GET /api/v1/reports/export/{type}?startDate=2026-03-01&endDate=2026-03-22
     *
     * @param type export format (PDF or EXCEL)
     * @param startDate start of the report period
     * @param endDate end of the report period
     * @return the report file as bytes
     */
    @GetMapping("/export/{type}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<byte[]> exportReport(
            @PathVariable ReportType type,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        log.info("Report export request: type={}, {} to {}", type, startDate, endDate);

        if (startDate.isAfter(endDate)) {
            return ResponseEntity.badRequest().build();
        }

        ProfitabilityReport report = reportService.generateProfitabilityReport(startDate, endDate);
        byte[] exportedReport = reportService.exportReport(report, type);
        
        String filename = String.format("profitability_report_%s_%s%s",
                startDate, endDate, reportService.getFileExtension(type));

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(reportService.getContentType(type)))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(exportedReport);
    }
}
