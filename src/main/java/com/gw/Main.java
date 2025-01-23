package com.gw;

import com.gw.report.ReportModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;

import java.util.concurrent.CountDownLatch;

import static com.gw.ArgumentParser.*;

@Slf4j
public class Main {

    public static void main(String[] args) throws Exception {

        CommandLine commandLine = new ArgumentParser().parse(args);
        ReportModel reportModel = new ReportModel();

        CountDownLatch latch = new CountDownLatch(Integer.valueOf(commandLine.getOptionValue(ARG_THREADS)));
        CountDownLatch syncLatchForFilteringStart = new CountDownLatch(Integer.valueOf(commandLine.getOptionValue(ARG_THREADS)));

        for (int i = 0; i < Integer.valueOf(commandLine.getOptionValue(ARG_THREADS)); i++) {
            new TestThread(commandLine, reportModel, latch, syncLatchForFilteringStart)
                    .start();
        }

        latch.await();

        log.info(" --- Writing results into excel.");
//        new ExcelWriter().prepareReport();
//        new ExcelWriter().prepareReport(new ReportModel().getMockObject());
        new ExcelWriter().prepareReport(reportModel);
    }
}
