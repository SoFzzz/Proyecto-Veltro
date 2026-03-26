package com.veltro.inventory.application.report.exporter;

import com.veltro.inventory.application.report.dto.ProfitabilityReport;
import com.veltro.inventory.application.report.dto.ReportType;

/**
 * Interface for report exporters (B3-02 Factory Method Pattern).
 *
 * <p>Defines the contract for exporting profitability reports to different formats.
 * Each implementation handles a specific format (PDF, Excel, etc.).
 *
 * @see PdfReportExporter
 * @see ExcelReportExporter
 */
public interface ReportExporter {

    /**
     * Exports a profitability report to a byte array.
     *
     * @param report the profitability data to export
     * @return the exported report as bytes
     */
    byte[] export(ProfitabilityReport report);

    /**
     * Returns the report type this exporter handles.
     *
     * @return the report type
     */
    ReportType getType();

    /**
     * Returns the content type for HTTP responses.
     *
     * @return the MIME content type
     */
    default String getContentType() {
        return getType().getContentType();
    }

    /**
     * Returns the file extension for downloads.
     *
     * @return the file extension (including dot)
     */
    default String getFileExtension() {
        return getType().getExtension();
    }
}
