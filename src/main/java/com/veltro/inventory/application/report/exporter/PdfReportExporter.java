package com.veltro.inventory.application.report.exporter;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.veltro.inventory.application.report.dto.ProfitabilityReport;
import com.veltro.inventory.application.report.dto.ReportType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

/**
 * PDF report exporter using iText (B3-02 Factory Method Pattern).
 *
 * <p>Generates PDF documents containing profitability reports with
 * formatted tables and summary sections.
 */
@Slf4j
@Component
public class PdfReportExporter implements ReportExporter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public byte[] export(ProfitabilityReport report) {
        log.info("Generating PDF report for period {} to {}",
                report.startDate(), report.endDate());

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            PdfFont boldFont = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD);
            PdfFont normalFont = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA);

            // Title
            document.add(new Paragraph("Profitability Report")
                    .setFont(boldFont)
                    .setFontSize(20)
                    .setTextAlignment(TextAlignment.CENTER));

            // Period
            document.add(new Paragraph(String.format("Period: %s - %s",
                    report.startDate().format(DATE_FORMATTER),
                    report.endDate().format(DATE_FORMATTER)))
                    .setFont(normalFont)
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20));

            // Summary section
            document.add(new Paragraph("Summary")
                    .setFont(boldFont)
                    .setFontSize(14)
                    .setMarginTop(10));

            Table summaryTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                    .useAllAvailableWidth();
            addSummaryRow(summaryTable, "Total Sales:", formatCurrency(report.totalSales()), boldFont, normalFont);
            addSummaryRow(summaryTable, "Total Cost:", formatCurrency(report.totalCost()), boldFont, normalFont);
            addSummaryRow(summaryTable, "Gross Profit:", formatCurrency(report.grossProfit()), boldFont, normalFont);
            addSummaryRow(summaryTable, "Profit Margin:", formatPercentage(report.profitMargin()), boldFont, normalFont);
            addSummaryRow(summaryTable, "Number of Sales:", String.valueOf(report.salesCount()), boldFont, normalFont);
            addSummaryRow(summaryTable, "Items Sold:", String.valueOf(report.itemsSold()), boldFont, normalFont);
            document.add(summaryTable);

            // Product breakdown
            if (!report.productBreakdown().isEmpty()) {
                document.add(new Paragraph("Product Breakdown")
                        .setFont(boldFont)
                        .setFontSize(14)
                        .setMarginTop(20));

                Table productTable = new Table(UnitValue.createPercentArray(
                        new float[]{25, 10, 10, 15, 15, 15, 10}))
                        .useAllAvailableWidth();

                // Header
                addHeaderCell(productTable, "Product", boldFont);
                addHeaderCell(productTable, "SKU", boldFont);
                addHeaderCell(productTable, "Qty", boldFont);
                addHeaderCell(productTable, "Revenue", boldFont);
                addHeaderCell(productTable, "Cost", boldFont);
                addHeaderCell(productTable, "Profit", boldFont);
                addHeaderCell(productTable, "Margin", boldFont);

                // Data rows
                for (ProfitabilityReport.ProductProfitability product : report.productBreakdown()) {
                    productTable.addCell(new Cell().add(new Paragraph(product.productName()).setFont(normalFont)));
                    productTable.addCell(new Cell().add(new Paragraph(product.sku()).setFont(normalFont)));
                    productTable.addCell(new Cell().add(new Paragraph(String.valueOf(product.quantitySold())).setFont(normalFont)));
                    productTable.addCell(new Cell().add(new Paragraph(formatCurrency(product.revenue())).setFont(normalFont)));
                    productTable.addCell(new Cell().add(new Paragraph(formatCurrency(product.cost())).setFont(normalFont)));
                    productTable.addCell(new Cell().add(new Paragraph(formatCurrency(product.profit())).setFont(normalFont)));
                    productTable.addCell(new Cell().add(new Paragraph(formatPercentage(product.profitMargin())).setFont(normalFont)));
                }

                document.add(productTable);
            }

            // Footer
            document.add(new Paragraph("Generated by Veltro ERP")
                    .setFont(normalFont)
                    .setFontSize(8)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(30));

            document.close();
            log.info("PDF report generated successfully ({} bytes)", outputStream.size());
            return outputStream.toByteArray();

        } catch (Exception e) {
            log.error("Failed to generate PDF report", e);
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }

    @Override
    public ReportType getType() {
        return ReportType.PDF;
    }

    private void addSummaryRow(Table table, String label, String value, PdfFont boldFont, PdfFont normalFont) {
        table.addCell(new Cell().add(new Paragraph(label).setFont(boldFont)));
        table.addCell(new Cell().add(new Paragraph(value).setFont(normalFont)));
    }

    private void addHeaderCell(Table table, String text, PdfFont boldFont) {
        table.addCell(new Cell()
                .add(new Paragraph(text).setFont(boldFont))
                .setBackgroundColor(ColorConstants.LIGHT_GRAY));
    }

    private String formatCurrency(java.math.BigDecimal value) {
        return value != null ? String.format("$%,.2f", value) : "$0.00";
    }

    private String formatPercentage(java.math.BigDecimal value) {
        return value != null ? String.format("%.1f%%", value) : "0.0%";
    }
}
