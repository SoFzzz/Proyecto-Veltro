package com.veltro.inventory.infrastructure.adapters.web;

import com.veltro.inventory.application.report.dto.ProfitabilityReport;
import com.veltro.inventory.application.report.dto.ReportType;
import com.veltro.inventory.application.report.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ReportController} (B3-02).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ReportController")
class ReportControllerTest {

    @Mock
    private ReportService reportService;

    private ReportController reportController;

    @BeforeEach
    void setUp() {
        reportController = new ReportController(reportService);
    }

    @Test
    @DisplayName("getProfitabilityReport returns report for valid dates")
    void getProfitabilityReport_validDates_returnsReport() {
        // Arrange
        LocalDate startDate = LocalDate.of(2026, 3, 1);
        LocalDate endDate = LocalDate.of(2026, 3, 22);
        ProfitabilityReport mockReport = new ProfitabilityReport(
                startDate, endDate,
                BigDecimal.valueOf(10000.00),
                BigDecimal.valueOf(6000.00),
                BigDecimal.valueOf(4000.00),
                BigDecimal.valueOf(40.00),
                50L, 200L,
                List.of()
        );
        when(reportService.generateProfitabilityReport(startDate, endDate)).thenReturn(mockReport);

        // Act
        ResponseEntity<ProfitabilityReport> response = reportController.getProfitabilityReport(startDate, endDate);

        // Assert
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().totalSales()).isEqualByComparingTo(BigDecimal.valueOf(10000.00));
        assertThat(response.getBody().grossProfit()).isEqualByComparingTo(BigDecimal.valueOf(4000.00));
        verify(reportService).generateProfitabilityReport(startDate, endDate);
    }

    @Test
    @DisplayName("getProfitabilityReport returns 400 for invalid date range")
    void getProfitabilityReport_invalidDateRange_returnsBadRequest() {
        // Arrange
        LocalDate startDate = LocalDate.of(2026, 3, 22);
        LocalDate endDate = LocalDate.of(2026, 3, 1);

        // Act
        ResponseEntity<ProfitabilityReport> response = reportController.getProfitabilityReport(startDate, endDate);

        // Assert
        assertThat(response.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    @DisplayName("exportReport returns PDF file")
    void exportReport_pdf_returnsFile() {
        // Arrange
        LocalDate startDate = LocalDate.of(2026, 3, 1);
        LocalDate endDate = LocalDate.of(2026, 3, 22);
        ProfitabilityReport mockReport = new ProfitabilityReport(
                startDate, endDate,
                BigDecimal.valueOf(10000.00),
                BigDecimal.valueOf(6000.00),
                BigDecimal.valueOf(4000.00),
                BigDecimal.valueOf(40.00),
                50L, 200L,
                List.of()
        );
        byte[] pdfBytes = new byte[]{1, 2, 3, 4, 5};
        
        when(reportService.generateProfitabilityReport(startDate, endDate)).thenReturn(mockReport);
        when(reportService.exportReport(any(ProfitabilityReport.class), eq(ReportType.PDF))).thenReturn(pdfBytes);
        when(reportService.getContentType(ReportType.PDF)).thenReturn("application/pdf");
        when(reportService.getFileExtension(ReportType.PDF)).thenReturn(".pdf");

        // Act
        ResponseEntity<byte[]> response = reportController.exportReport(ReportType.PDF, startDate, endDate);

        // Assert
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(pdfBytes);
        assertThat(response.getHeaders().getContentType().toString()).isEqualTo("application/pdf");
        assertThat(response.getHeaders().getContentDisposition().toString())
                .contains("profitability_report_2026-03-01_2026-03-22.pdf");
    }

    @Test
    @DisplayName("exportReport returns Excel file")
    void exportReport_excel_returnsFile() {
        // Arrange
        LocalDate startDate = LocalDate.of(2026, 3, 1);
        LocalDate endDate = LocalDate.of(2026, 3, 22);
        ProfitabilityReport mockReport = new ProfitabilityReport(
                startDate, endDate,
                BigDecimal.valueOf(10000.00),
                BigDecimal.valueOf(6000.00),
                BigDecimal.valueOf(4000.00),
                BigDecimal.valueOf(40.00),
                50L, 200L,
                List.of()
        );
        byte[] excelBytes = new byte[]{10, 20, 30, 40, 50};
        
        when(reportService.generateProfitabilityReport(startDate, endDate)).thenReturn(mockReport);
        when(reportService.exportReport(any(ProfitabilityReport.class), eq(ReportType.EXCEL))).thenReturn(excelBytes);
        when(reportService.getContentType(ReportType.EXCEL))
                .thenReturn("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        when(reportService.getFileExtension(ReportType.EXCEL)).thenReturn(".xlsx");

        // Act
        ResponseEntity<byte[]> response = reportController.exportReport(ReportType.EXCEL, startDate, endDate);

        // Assert
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(excelBytes);
        assertThat(response.getHeaders().getContentDisposition().toString())
                .contains("profitability_report_2026-03-01_2026-03-22.xlsx");
    }

    @Test
    @DisplayName("exportReport returns 400 for invalid date range")
    void exportReport_invalidDateRange_returnsBadRequest() {
        // Arrange
        LocalDate startDate = LocalDate.of(2026, 3, 22);
        LocalDate endDate = LocalDate.of(2026, 3, 1);

        // Act
        ResponseEntity<byte[]> response = reportController.exportReport(ReportType.PDF, startDate, endDate);

        // Assert
        assertThat(response.getStatusCode().value()).isEqualTo(400);
    }
}
