package com.veltro.inventory.application.report.exporter;

import com.veltro.inventory.application.report.dto.ProfitabilityReport;
import com.veltro.inventory.application.report.dto.ReportType;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

/**
 * Excel report exporter using Apache POI (B3-02 Factory Method Pattern).
 *
 * <p>Generates Excel (.xlsx) documents containing profitability reports
 * with formatted worksheets and summary sections.
 */
@Slf4j
@Component
public class ExcelReportExporter implements ReportExporter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public byte[] export(ProfitabilityReport report) {
        log.info("Generating Excel report for period {} to {}",
                report.startDate(), report.endDate());

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Profitability Report");

            // Styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            CellStyle percentStyle = createPercentStyle(workbook);
            CellStyle titleStyle = createTitleStyle(workbook);

            int rowNum = 0;

            // Title
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Profitability Report");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));

            // Period
            Row periodRow = sheet.createRow(rowNum++);
            periodRow.createCell(0).setCellValue(String.format("Period: %s - %s",
                    report.startDate().format(DATE_FORMATTER),
                    report.endDate().format(DATE_FORMATTER)));
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 6));

            rowNum++; // Empty row

            // Summary section
            Row summaryHeader = sheet.createRow(rowNum++);
            Cell summaryTitle = summaryHeader.createCell(0);
            summaryTitle.setCellValue("Summary");
            summaryTitle.setCellStyle(headerStyle);

            addSummaryRow(sheet, rowNum++, "Total Sales:", report.totalSales().doubleValue(), currencyStyle);
            addSummaryRow(sheet, rowNum++, "Total Cost:", report.totalCost().doubleValue(), currencyStyle);
            addSummaryRow(sheet, rowNum++, "Gross Profit:", report.grossProfit().doubleValue(), currencyStyle);
            addSummaryRowPercent(sheet, rowNum++, "Profit Margin:", report.profitMargin().doubleValue() / 100, percentStyle);
            addSummaryRowNumber(sheet, rowNum++, "Number of Sales:", report.salesCount());
            addSummaryRowNumber(sheet, rowNum++, "Items Sold:", report.itemsSold());

            rowNum++; // Empty row

            // Product breakdown
            if (!report.productBreakdown().isEmpty()) {
                Row breakdownHeader = sheet.createRow(rowNum++);
                Cell breakdownTitle = breakdownHeader.createCell(0);
                breakdownTitle.setCellValue("Product Breakdown");
                breakdownTitle.setCellStyle(headerStyle);

                // Table headers
                Row tableHeader = sheet.createRow(rowNum++);
                String[] headers = {"Product", "SKU", "Qty Sold", "Revenue", "Cost", "Profit", "Margin"};
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = tableHeader.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);
                }

                // Data rows
                for (ProfitabilityReport.ProductProfitability product : report.productBreakdown()) {
                    Row dataRow = sheet.createRow(rowNum++);
                    dataRow.createCell(0).setCellValue(product.productName());
                    dataRow.createCell(1).setCellValue(product.sku());
                    dataRow.createCell(2).setCellValue(product.quantitySold());

                    Cell revenueCell = dataRow.createCell(3);
                    revenueCell.setCellValue(product.revenue().doubleValue());
                    revenueCell.setCellStyle(currencyStyle);

                    Cell costCell = dataRow.createCell(4);
                    costCell.setCellValue(product.cost().doubleValue());
                    costCell.setCellStyle(currencyStyle);

                    Cell profitCell = dataRow.createCell(5);
                    profitCell.setCellValue(product.profit().doubleValue());
                    profitCell.setCellStyle(currencyStyle);

                    Cell marginCell = dataRow.createCell(6);
                    marginCell.setCellValue(product.profitMargin().doubleValue() / 100);
                    marginCell.setCellStyle(percentStyle);
                }
            }

            // Auto-size columns
            for (int i = 0; i <= 6; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            log.info("Excel report generated successfully ({} bytes)", outputStream.size());
            return outputStream.toByteArray();

        } catch (Exception e) {
            log.error("Failed to generate Excel report", e);
            throw new RuntimeException("Failed to generate Excel report", e);
        }
    }

    @Override
    public ReportType getType() {
        return ReportType.EXCEL;
    }

    private void addSummaryRow(Sheet sheet, int rowNum, String label, double value, CellStyle style) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(label);
        Cell valueCell = row.createCell(1);
        valueCell.setCellValue(value);
        valueCell.setCellStyle(style);
    }

    private void addSummaryRowPercent(Sheet sheet, int rowNum, String label, double value, CellStyle style) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(label);
        Cell valueCell = row.createCell(1);
        valueCell.setCellValue(value);
        valueCell.setCellStyle(style);
    }

    private void addSummaryRowNumber(Sheet sheet, int rowNum, String label, long value) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(label);
        row.createCell(1).setCellValue(value);
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        style.setFont(font);
        return style;
    }

    private CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("$#,##0.00"));
        return style;
    }

    private CellStyle createPercentStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("0.0%"));
        return style;
    }
}
