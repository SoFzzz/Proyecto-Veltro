package com.veltro.inventory.application.report.dto;

/**
 * Supported report export formats (B3-02).
 *
 * <p>Used by the Factory Method pattern to create the appropriate
 * report exporter.
 */
public enum ReportType {
    /**
     * PDF format using iText library.
     */
    PDF("application/pdf", ".pdf"),

    /**
     * Excel format using Apache POI library.
     */
    EXCEL("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", ".xlsx");

    private final String contentType;
    private final String extension;

    ReportType(String contentType, String extension) {
        this.contentType = contentType;
        this.extension = extension;
    }

    public String getContentType() {
        return contentType;
    }

    public String getExtension() {
        return extension;
    }
}
