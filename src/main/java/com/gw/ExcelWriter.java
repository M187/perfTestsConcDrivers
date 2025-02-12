package com.gw;

import com.gw.report.ReportModel;
import com.gw.report.ReportRow;
import org.apache.commons.cli.CommandLine;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import static com.gw.ArgumentParser.ARG_THREADS;
import static com.gw.ArgumentParser.NAME_SUFFIX;

public class ExcelWriter {

    public String prepareReport(ReportModel reportModel, CommandLine commandLine, long runtime) {

        try {
//            String filename = "runReport_" + new SimpleDateFormat("dd.MM.yyyy_HHmm").format(new Date()) + "_" + commandLine.getOptionValue(ARG_THREADS) + "browserInstances.xls";

            String filename = commandLine.getOptionValue(NAME_SUFFIX) != null ?
                    "runReport_" + new SimpleDateFormat("dd.MM.yyyy_HHmm").format(new Date()) + "_" + commandLine.getOptionValue(ARG_THREADS) + "threads_" + commandLine.getOptionValue(NAME_SUFFIX) + ".xls"
            : "runReport_" + new SimpleDateFormat("dd.MM.yyyy_HHmm").format(new Date()) + "_" + commandLine.getOptionValue(ARG_THREADS) + "threads.xls";


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
//                    row.createCell(0).setCellValue("SSP portal loading time");
//                    row.createCell(1).setCellValue(rR.getLandingPageLoadTime());
                    row.createCell(2).setCellValue("List load time");
                    Cell cell = row.createCell(3);
                    cell.setCellValue(
                            new BigDecimal(rR.getListLoadTime().replaceAll("[^\\d.,]+","")).doubleValue());
                    cell.setCellType(Cell.CELL_TYPE_NUMERIC);

                    cell = row.createCell(4);
                    cell.setCellValue(
                            new BigDecimal(rR.getListRenderTime().replaceAll("[^\\d.,]+","")).doubleValue());
                    cell.setCellType(Cell.CELL_TYPE_NUMERIC);
//                    row.createCell(5).setCellValue(rR.getNumberOfResultsReturned());
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
        Row rowDesc = sheet.createRow(lastRowOfMetrics + 1);
        rowDesc.createCell(3).setCellValue("Load time");
        rowDesc.createCell(4).setCellValue("Render time");
        Row rowAverage = sheet.createRow(lastRowOfMetrics + 2);
        rowAverage.createCell(2).setCellValue("Average ");
        Row rowMax = sheet.createRow(lastRowOfMetrics + 3);
        rowMax.createCell(2).setCellValue("Maximum");
        Row rowMin = sheet.createRow(lastRowOfMetrics + 4);
        rowMin.createCell(2).setCellValue("Minimum ");

        sheet.autoSizeColumn(2);

        rowAverage.createCell(3).setCellFormula("AVERAGE(D2:D" + lastRowOfMetrics + ")");
        rowMax.createCell(3).setCellFormula("MAX(D2:D" + lastRowOfMetrics + ")");
        rowMin.createCell(3).setCellFormula("MIN(D2:D" + lastRowOfMetrics + ")");

        rowAverage.createCell(4).setCellFormula("AVERAGE(E2:E" + lastRowOfMetrics + ")");
        rowMax.createCell(4).setCellFormula("MAX(E2:E" + lastRowOfMetrics + ")");
        rowMin.createCell(4).setCellFormula("MIN(E2:E" + lastRowOfMetrics + ")");
    }
}