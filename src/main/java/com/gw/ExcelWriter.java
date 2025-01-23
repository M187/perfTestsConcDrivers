package com.gw;

import com.gw.report.ReportModel;
import com.gw.report.ReportRow;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

@Slf4j
public class ExcelWriter {

    public void prepareReport(ReportModel reportModel) {

        try {
            File myObj = new File("runReport_" + new SimpleDateFormat("dd.MM.yyyy_HHmm").format(new Date())+".xls");
            FileOutputStream fileOutputStream = null;
            try {
                myObj.createNewFile();
                fileOutputStream = new FileOutputStream(myObj);
            } catch (IOException e) {
                e.printStackTrace();
            }
            HSSFWorkbook workbook = new HSSFWorkbook();
            HSSFSheet sheet = workbook.createSheet("Load Times");

            int rowNum = 0;

            synchronized (reportModel.getRows()) {
                Iterator<ReportRow> it = reportModel.getRows().iterator();
                while (it.hasNext()) {
                    ReportRow rR = it.next();
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue( "Landing page loading time");
                    row.createCell(1).setCellValue(rR.getLoginPageLoadTime());
                    row.createCell(2).setCellValue("List load time");
                    row.createCell(3).setCellValue(rR.getListLoadTime());
                    row.createCell(4).setCellValue("Number of entries returned");
                    row.createCell(5).setCellValue(rR.getNumberOfResultsReturned());
                }
                for (int i = 0; i < 6; i++) {
                    sheet.autoSizeColumn(i);
                }
            }

            workbook.write(fileOutputStream);
            workbook.close();
        } catch (IOException ie) {
            ie.printStackTrace();
        }
    }

//	public void writeToExcelV1(List<InputData> inputDatas, List<ResultData> resultDatas) {
//		try {
//			File myObj = new File("policiesPriceComparison.xls");
//			FileOutputStream fileOutputStream = null;
//			try {
//				myObj.createNewFile();
//				fileOutputStream = new FileOutputStream(myObj);
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//			HSSFWorkbook workbook = new HSSFWorkbook();
//			HSSFSheet sheet = workbook.createSheet("PoliciesComparison" + new SimpleDateFormat("dd.MM.yyyy").format(new Date()));
//
//			int rownum = 0;
//			for (int i = 0; i < inputDatas.size();i++) {
//				ResultData resultData = resultDatas.get(i);
//				InputData inputData = inputDatas.get(i);
//				sheet.addMergedRegion(new CellRangeAddress(rownum,rownum,0,resultData.getCompanyName().size()));
//				Row rowOverview = sheet.createRow(rownum++);
//				rowOverview.createCell(0).setCellValue(inputData.string());
//
//				Object[][] arrayOfEntries = {
//						resultData.getCompanyName().toArray(),
//						resultData.getInsurancePrice().toArray(),
//						resultData.getLiecebneVylohy().toArray(),
//						resultData.getUrazovePojisteni().toArray(),
//						resultData.getPojistOdpovednosti().toArray()
//				};
//
//				for (Object[] entry : arrayOfEntries) {
//					Row row = sheet.createRow(rownum++);
//					int colnum = 0;
//					for (Object value : entry) {
//						Cell cell = row.createCell(colnum++);
//						if (value instanceof String) {
//							cell.setCellValue((String) value);
//						} else if (value instanceof Integer) {
//							cell.setCellValue((Integer) value);
//						}
//					}
//				}
//				for (int n = 0; n < resultData.getCompanyName().size(); n++) sheet.autoSizeColumn(n);
//				rownum++;
//			}
//			workbook.write(fileOutputStream);
//			workbook.close();
//		} catch (IOException ie) {
//			ie.printStackTrace();
//		}
//	}
}