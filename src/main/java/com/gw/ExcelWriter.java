package com.gw;

import com.gw.report.ReportModel;
import com.gw.report.ReportRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

public class ExcelWriter {

    public String prepareReport(ReportModel reportModel, String threadCount, long runtime) {

        try {
            String filename = "runReport_" + new SimpleDateFormat("dd.MM.yyyy_HHmm").format(new Date()) + "_" + threadCount + "browserInstances.xls";
            File myObj = new File(filename);
            FileOutputStream fileOutputStream = null;
            try {
                myObj.createNewFile();
                fileOutputStream = new FileOutputStream(myObj);
            } catch (IOException e) {
                e.printStackTrace();
            }
            HSSFWorkbook workbook = new HSSFWorkbook();
            HSSFSheet sheet = workbook.createSheet("Load Times");

            int rowIndex = 1;

            synchronized (reportModel.getRows()) {
                Iterator<ReportRow> it = reportModel.getRows().iterator();
                while (it.hasNext()) {
                    ReportRow rR = it.next();
                    Row row = sheet.createRow(rowIndex++);
                    row.createCell(0).setCellValue("SSP portal loading time");
                    row.createCell(1).setCellValue(rR.getLandingPageLoadTime());
                    row.createCell(2).setCellValue("List load time");
                    row.createCell(3).setCellValue(rR.getListLoadTime());
                    row.createCell(4).setCellValue("Number of entries returned");
                    row.createCell(5).setCellValue(rR.getNumberOfResultsReturned());
                    row.createCell(6).setCellValue(rR.getThreadName());
                }

                for (int i = 0; i < 5; i++) {
                    sheet.autoSizeColumn(i);
                }
                sheet.setColumnWidth(3, 7 * 256);

                populateStatistics(sheet, rowIndex);
                populateHeaderData(sheet, reportModel, runtime);
            }

            workbook.write(fileOutputStream);
            workbook.close();
            return filename;
        } catch (IOException ie) {
            ie.printStackTrace();
        }
        return "Exception occurred!";
    }

    private void populateHeaderData(HSSFSheet sheet, ReportModel reportModel, long runtime) {
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 2));
        Row headerRow = sheet.createRow(0);
        if (reportModel.isUser) {
            headerRow.createCell(0).setCellValue("  User used: " + reportModel.getHeader());
        } else {
            headerRow.createCell(0).setCellValue("  Supervisor/s used: " + reportModel.getHeader());
        }

        sheet.addMergedRegion(new CellRangeAddress(0, 0, 3, 6));
        headerRow.createCell(3).setCellValue("  Total runtime: " + runtime);
    }

    private void populateStatistics(HSSFSheet sheet, int lastRowOfMetrics) {
        Row rowAverage = sheet.createRow(lastRowOfMetrics + 2);
        rowAverage.createCell(2).setCellValue("Average List load time");
        Row rowMax = sheet.createRow(lastRowOfMetrics + 3);
        rowMax.createCell(2).setCellValue("Maximum List load time");
        Row rowMin = sheet.createRow(lastRowOfMetrics + 4);
        rowMin.createCell(2).setCellValue("Minimum List load time");

        sheet.autoSizeColumn(2);

        rowAverage.createCell(3).setCellFormula("AVERAGE(D2:D" + lastRowOfMetrics + ")");
        rowMax.createCell(3).setCellFormula("MAX(D2:D" + lastRowOfMetrics + ")");
        rowMin.createCell(3).setCellFormula("MIN(D2:D" + lastRowOfMetrics + ")");
    }
}