package com.gw;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideConfig;
import com.codeborne.selenide.SelenideDriver;
import com.gw.report.ReportModel;
import com.gw.report.ReportRow;
import lombok.AllArgsConstructor;
import org.apache.commons.cli.CommandLine;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;

import static com.gw.ArgumentParser.*;
import static org.openqa.selenium.firefox.GeckoDriverService.GECKO_DRIVER_LOG_PROPERTY;

@AllArgsConstructor
public class TestThread extends Thread {

    private CommandLine commandLine;
    private ReportModel reportData;
    private final CountDownLatch doneSignal;
    private final CountDownLatch nextBrowserLatch;
    private final CountDownLatch startFilteringSignal;
    private final int threadIndex;

    @Override
    public void run() {
        try {
            System.setProperty(GECKO_DRIVER_LOG_PROPERTY, "/dev/null");
            FirefoxOptions options = new FirefoxOptions();
            SelenideConfig config = new SelenideConfig();
            config.browser("firefox");
            config.browserCapabilities(options);
            config.browserSize("1920x1080");
            config.headless(commandLine.hasOption(ARG_HEADLESS));


            SelenideDriver browser = new SelenideDriver(config);
            long before = System.currentTimeMillis();
            browser.open("https://partner2-qa.colonnade.sk/myColonnade/dashboard");
//            browser.open("https://portal2-qa.colonnade.cz/myColonnade/dashboard");
            System.out.println(" ---- Thread " + threadIndex + " - SSP login page reached.");

            browser.$("#email").should(Condition.visible, Duration.ofSeconds(60));
            long loginPageLoadDuration = System.currentTimeMillis() - before;
            browser.$("#email").setValue(commandLine.getOptionValue(ARG_LOGIN));
            browser.$("#password").setValue(commandLine.getOptionValue(ARG_PASSWORD));
            browser.$("#next").click();

            new CookieHandler().acceptCookies(browser);
            System.out.println(" ---- Thread " + threadIndex + " - Succ logged into SSP instance and accepted cookies");
            nextBrowserLatch.countDown();

            //move to reports tab
            browser.$("#dashboard_reporting_id").click();

            //set parameters for filtering
            if (commandLine.getOptionValue(ARG_USER) != null) {
                browser.$("#userConfigFilterEmailId").should(Condition.visible, Duration.ofSeconds(20)).$x(".//div[contains(text(),'" + commandLine.getOptionValue(ARG_USER) + "')]").parent().click();
            } else if (commandLine.getOptionValue(ARG_SUPERVISORS) != null) {
                for (String producer : commandLine.getOptionValue(ARG_SUPERVISORS).split(",")) {
                    browser.$("#userConfigFilterProducerCodeId").should(Condition.visible, Duration.ofSeconds(20)).$x(".//div[contains(text(),'" + producer + "')]").parent().click();
                }
            }

            startFilteringSignal.countDown();
            long listLoadTime;
            try {
                startFilteringSignal.await();
                //press Apply Filters button
                System.out.println(" ---- Thread " + threadIndex + " - triggering filtering.");
                before = System.currentTimeMillis();
                browser.$("#applyFilterBtnId").click();

                //wait for page load finished
                listLoadTime = System.currentTimeMillis() - before;

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println(" ---- Thread " + threadIndex + " closing browser instance.");
            browser.close();

            reportData.addRow(new ReportRow("Thread" + threadIndex, loginPageLoadDuration, listLoadTime, 1l));
            System.out.println(" ---- Thread " + threadIndex + " - reached end of lifecycle. Added data to report.");
        } catch (Exception e) {
            System.out.println(" ---- Thread " + threadIndex + " had exception");
            throw e;
        } finally {
            startFilteringSignal.countDown();
            doneSignal.countDown();
        }
    }

    private void waitUntilPageLoaded(SelenideDriver driver) {
        new WebDriverWait(driver.getWebDriver(), Duration.ofSeconds(120)).until(
                webDriver -> ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete"));
    }
}
