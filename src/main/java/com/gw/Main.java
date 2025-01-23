package com.gw;

import com.gw.report.ReportModel;
import org.apache.commons.cli.CommandLine;

import java.util.concurrent.CountDownLatch;

import static com.gw.ArgumentParser.*;

public class Main {

    public static void main(String[] args) throws Exception {

        System.out.println(" --------------------------------");
        System.out.println(" --- SSP performance test run ---");
        System.out.println(" --------------------------------");
        System.out.println("");

        CommandLine commandLine = new ArgumentParser().parse(args);
        ReportModel reportModel = new ReportModel();

        CountDownLatch latch = new CountDownLatch(Integer.valueOf(commandLine.getOptionValue(ARG_THREADS)));
        CountDownLatch syncLatchForFilteringStart = new CountDownLatch(Integer.valueOf(commandLine.getOptionValue(ARG_THREADS)));

        System.out.println(" --- Starting to spawn browser threads.");
        for (int i = 0; i < Integer.valueOf(commandLine.getOptionValue(ARG_THREADS)); i++) {
            new TestThread(commandLine, reportModel, latch, syncLatchForFilteringStart, i)
                    .start();
            System.out.println(" --- Thread " + i + " started.");
        }

        latch.await();

        System.out.println(" --- Writing results into excel.");
//        new ExcelWriter().prepareReport();
//        new ExcelWriter().prepareReport(new ReportModel().getMockObject());
        String fileName = new ExcelWriter().prepareReport(reportModel, commandLine.getOptionValue(ARG_THREADS));
        System.out.println(" --- Finished writing results into excel file: " + fileName);
    }
}
