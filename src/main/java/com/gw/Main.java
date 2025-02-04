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

        if (args[0].matches("-help|help|h|-h")) {
            writeHelpMessage();
        } else {

            long runTime = System.currentTimeMillis();
            CommandLine commandLine = new ArgumentParser().parse(args);
            ReportModel reportModel =
                    commandLine.getOptionValue(ARG_USER) == null ? new ReportModel(commandLine.getOptionValue(ARG_SUPERVISORS), false)
                            : new ReportModel(commandLine.getOptionValue(ARG_USER), true);

            CountDownLatch latch = new CountDownLatch(Integer.valueOf(commandLine.getOptionValue(ARG_THREADS)));
            CountDownLatch filteringLatch = new CountDownLatch(Integer.valueOf(commandLine.getOptionValue(ARG_THREADS)));
            CountDownLatch collectDataLatch = new CountDownLatch(Integer.valueOf(commandLine.getOptionValue(ARG_THREADS)));

            System.out.println(" --- Starting to spawn browser threads.");
            for (int i = 1; i <= Integer.valueOf(commandLine.getOptionValue(ARG_THREADS)); i++) {
                CountDownLatch nextBrowserLatch = new CountDownLatch(1);
                new TestThread(commandLine, reportModel, latch, nextBrowserLatch, filteringLatch, collectDataLatch, i)
                        .start();
                System.out.println(" --- Thread " + i + " started.");
                nextBrowserLatch.await(300, TimeUnit.SECONDS);
            }

            latch.await();

            System.out.println(" --- Writing results into excel.");
//            String fileName = new ExcelWriter().prepareReport(ReportModel.getMockObject(), commandLine.getOptionValue(ARG_THREADS), System.currentTimeMillis() - runTime);
            String fileName = new ExcelWriter().prepareReport(reportModel, commandLine.getOptionValue(ARG_THREADS), System.currentTimeMillis() - runTime);
            System.out.println(" --- Finished writing results into excel file: " + fileName);
        }
    }

    private static void writeHelpMessage() {
        System.out.println(" --- List of arguments: ");
        System.out.println(" ---  -l *login*");
        System.out.println(" ---  -p *password*");
        System.out.println(" ---  -h enable headless mode (browser will be invisible/running in background)");
        System.out.println(" ---  -t *numberOfThreads*");
        System.out.println(" ---  -u *userToBeUsedToParseData* // please use either -u or -s. If both -u and -s is present, -u takes priority over -s (-s will be ignored)");
        System.out.println(" ---  -s *commaSeparatedListOfSupervisorsToBeUsedToParseData* // please use either -u or -s. If both -u and -s is present, -u takes priority over -s (-s will be ignored)");
        System.out.println("");
        System.out.println(" ---  !!! for whitespace, wrap the whole value in quotas like: \"whitespace example\"");
        System.out.println("");
        System.out.println(" ---        Example cmd line params for user:    -l pe_ctp_test7@colonnade.pl -p Lutu5059 -t 13 -h -u su");
        System.out.println(" ---  Example cmd line params for supervisor:    -l pe_ctp_test7@colonnade.pl -p Lutu5059 -t 13 -h -s \"Slovakia Bubo\",1000000,PRTNR-1000006");
    }
}
