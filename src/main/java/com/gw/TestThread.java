package com.gw;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.SelenideConfig;
import com.codeborne.selenide.SelenideDriver;
import com.gw.report.ReportModel;
import com.gw.report.ReportRow;
import lombok.AllArgsConstructor;
import org.apache.commons.cli.CommandLine;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;

import static com.gw.ArgumentParser.*;
import static org.openqa.selenium.firefox.GeckoDriverService.GECKO_DRIVER_LOG_PROPERTY;

@AllArgsConstructor
public class TestThread extends Thread {

    private CommandLine commandLine;
    private ReportModel reportData;
    private final CountDownLatch doneSignal;
    private final CountDownLatch startFilteringSignal;
    private final int threadIndex;

    @Override
    public void run(){

        System.setProperty(GECKO_DRIVER_LOG_PROPERTY, "/dev/null");
        FirefoxOptions options = new FirefoxOptions();
        SelenideConfig config = new SelenideConfig();
        config.browser("firefox");
        config.browserCapabilities(options);
        config.browserSize("1920x1080");
        config.headless(commandLine.hasOption(ARG_HEADLESS));


        SelenideDriver browser = new SelenideDriver(config);
        browser.open("http://www.google.com");
        long before = System.currentTimeMillis();
        browser.open("https://partner2-qa.colonnade.pl/myColonnade/dashboard");
        long pageLoadDuration = System.currentTimeMillis() - before;
        //login
        System.out.println(" ---- Thread " +threadIndex+ " - SSP login page reached.");
        browser.$("#email").should(Condition.visible, Duration.ofSeconds(60)).setValue(commandLine.getOptionValue(ARG_LOGIN));
        browser.$("#password").setValue(commandLine.getOptionValue(ARG_PASSWORD));
        before = System.currentTimeMillis();
        browser.$("#next").click();
        long afterLoginLoadDuration = System.currentTimeMillis() - before;



        new CookieHandler().acceptCookies(browser);
        System.out.println(" ---- Thread " +threadIndex+ " - Succ logged into instance and accepted cookies");
        //switch to tab
        //input producer ID and search results
        //
        startFilteringSignal.countDown();
        try {
            startFilteringSignal.await();
        } catch (InterruptedException e) {
            browser.close();
            throw new RuntimeException(e);
        }
        System.out.println(" ---- Thread " +threadIndex+ " - Reached end of script and quiting browser instance.");
        browser.close();

        reportData.addRow(new ReportRow(pageLoadDuration, 1l, 1l));

        doneSignal.countDown();
    }
}
