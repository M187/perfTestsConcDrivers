package com.gw;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.SelenideConfig;
import com.codeborne.selenide.SelenideDriver;
import com.gw.report.ReportModel;
import com.gw.report.ReportRow;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;

import static com.gw.ArgumentParser.*;
import static org.openqa.selenium.firefox.GeckoDriverService.GECKO_DRIVER_LOG_PROPERTY;

@Slf4j
@AllArgsConstructor
public class TestThread extends Thread {

    private CommandLine commandLine;
    private ReportModel reportData;
    private final CountDownLatch doneSignal;
    private final CountDownLatch startFilteringSignal;

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
        browser.open("");
        long pageLoadDuration = System.currentTimeMillis() - before;
        //login
        browser.$("#email").should(Condition.visible, Duration.ofSeconds(60)).setValue(commandLine.getOptionValue(ARG_LOGIN));
        browser.$("#password").setValue(commandLine.getOptionValue(ARG_PASSWORD));
        before = System.currentTimeMillis();
        browser.$("#next").click();
        long afterLoginLoadDuration = System.currentTimeMillis() - before;



        new CookieHandler().acceptCookies(browser);
        log.info("Succ logged into instance and accepted cookies");
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

        browser.close();

        reportData.addRow(new ReportRow(pageLoadDuration, 1l, 1l));

        doneSignal.countDown();
    }
}
