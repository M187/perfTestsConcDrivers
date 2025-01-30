package com.gw;

import com.gw.report.ReportModel;
import org.apache.commons.cli.CommandLine;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.gw.ArgumentParser.*;

public class Main {

    /**
     * Scenarios
     * 1.) 200 rows - use producer "PRTNR-1000007"
     * 2.) 5000 rows - use producer "POLAND DIRECT"
     * 3.) 20000 rows - use producers "HUNGARY DIRECT", "Slovakia Bubo", "1000000"
     *
     *
     *
     * @param args
     * @throws Exception
     */

    public static void main(String[] args) throws Exception {

        System.out.println(" --------------------------------");
        System.out.println(" --- SSP performance test run ---");
        System.out.println(" --------------------------------");
        System.out.println("");

        CommandLine commandLine = new ArgumentParser().parse(args);
        ReportModel reportModel = new ReportModel();

        CountDownLatch latch = new CountDownLatch(Integer.valueOf(commandLine.getOptionValue(ARG_THREADS)));
        CountDownLatch filteringLatch = new CountDownLatch(Integer.valueOf(commandLine.getOptionValue(ARG_THREADS)));

        System.out.println(" --- Starting to spawn browser threads.");
        for (int i = 0; i < Integer.valueOf(commandLine.getOptionValue(ARG_THREADS)); i++) {
            CountDownLatch nextBrowserLatch = new CountDownLatch(1);
            new TestThread(commandLine, reportModel, latch, nextBrowserLatch, filteringLatch, i)
                    .start();
            System.out.println(" --- Thread " + i + " started.");
            nextBrowserLatch.await(300, TimeUnit.SECONDS);
        }

        latch.await();

        System.out.println(" --- Writing results into excel.");
        String fileName = new ExcelWriter().prepareReport(reportModel, commandLine.getOptionValue(ARG_THREADS));
        System.out.println(" --- Finished writing results into excel file: " + fileName);
    }
}
