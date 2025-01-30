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

    public String prepareReport(ReportModel reportModel, String threadCount) {

        try {
            String filename = "runReport_" + new SimpleDateFormat("dd.MM.yyyy_HHmm").format(new Date())+"_"+ threadCount +"browserInstances.xls";
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

            sheet.addMergedRegion(new CellRangeAddress(0,0,0,6));

            Row headerRow = sheet.createRow(0);
            if (reportModel.isUser) {
                headerRow.createCell(0).setCellValue("  User used: " + reportModel.getHeader());
            } else {
                headerRow.createCell(0).setCellValue("  Supervisor/s used: " + reportModel.getHeader());
            }

            int rowNum = 1;

            synchronized (reportModel.getRows()) {
                Iterator<ReportRow> it = reportModel.getRows().iterator();
                while (it.hasNext()) {
                    ReportRow rR = it.next();
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue( "Landing page loading time");
                    row.createCell(1).setCellValue(rR.getLandingPageLoadTime());
                    row.createCell(2).setCellValue("List load time");
                    row.createCell(3).setCellValue(rR.getListLoadTime());
                    row.createCell(4).setCellValue("Number of entries returned");
                    row.createCell(5).setCellValue(rR.getNumberOfResultsReturned());
//                    row.createCell(6).setCellValue(rR.getThreadName());
                }
                for (int i = 0; i < 6; i++) {
                    sheet.autoSizeColumn(i);
                }
            }

            workbook.write(fileOutputStream);
            workbook.close();
            return filename;
        } catch (IOException ie) {
            ie.printStackTrace();
        }
        return "Exception occurred!";
    }
}