package com.gw;

import com.codeborne.selenide.SelenideConfig;
import com.codeborne.selenide.SelenideDriver;
import com.gw.report.ReportModel;
import com.gw.report.ReportRow;
import lombok.AllArgsConstructor;
import org.apache.commons.cli.CommandLine;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.codeborne.selenide.Selenide.sleep;
import static com.gw.ArgumentParser.*;
import static org.openqa.selenium.firefox.GeckoDriverService.GECKO_DRIVER_LOG_PROPERTY;

@AllArgsConstructor
public class TestThread extends Thread {

    private CommandLine commandLine;
    private ReportModel reportData;
    private final CountDownLatch doneSignal;
    private final CountDownLatch nextBrowserLatch;
    private final CountDownLatch startFilteringSignal;
    private final CountDownLatch collectDataSignal;
    private final int threadIndex;

    @Override
    public void run() {
        try {
            System.setProperty(GECKO_DRIVER_LOG_PROPERTY, "/dev/null");
            SelenideConfig config = new SelenideConfig();
            config.browser("edge");
            config.browserSize("1920x1080");
            config.headless(commandLine.hasOption(ARG_HEADLESS));

            SelenideDriver browser = new SelenideDriver(config);
            long before = System.currentTimeMillis();
            browser.open("https://partner2-qa.colonnade.pl/myColonnade/dashboard");
            WebDriver driver = browser.getWebDriver();
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            driver.manage().timeouts().implicitlyWait(100, TimeUnit.SECONDS);
            System.out.println(" ---- Thread " + threadIndex + " - SSP login page reached.");

            driver.findElement(By.cssSelector("#email")).isDisplayed();
            long loginPageLoadDuration = System.currentTimeMillis() - before;
            sleep(300);
            driver.findElement(By.cssSelector("#email")).sendKeys(commandLine.getOptionValue(ARG_LOGIN));
            driver.findElement(By.cssSelector("#password")).sendKeys(commandLine.getOptionValue(ARG_PASSWORD));
            try {
                driver.findElement(By.cssSelector("#next")).click();
            } catch (Exception e) {
                sleep(1000);
                driver.findElement(By.cssSelector("#next")).click();
            }

            new CookieHandler().acceptCookies(driver);
            System.out.println(" ---- Thread " + threadIndex + " - Succ logged into SSP instance and accepted cookies");

            //move to reports tab
            wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#dashboard_reporting_id")));
            driver.findElement(By.cssSelector("#dashboard_reporting_id")).click();

            wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#applyFilterBtnId")));
            //set parameters for filtering
            if (commandLine.getOptionValue(ARG_USER) != null) {
                driver.findElement(By.cssSelector("#userConfigFilterEmailId")).findElement(By.xpath(".//div[contains(text(),'" + commandLine.getOptionValue(ARG_USER) + "')]/..")).click();
            } else if (commandLine.getOptionValue(ARG_SUPERVISORS) != null) {
                for (String producer : commandLine.getOptionValue(ARG_SUPERVISORS).split(",")) {
                    driver.findElement(By.cssSelector("#userConfigFilterProducerCodeId")).findElement(By.xpath(".//div[contains(text(),'" + producer + "')]/..")).click();
                }
            }

            nextBrowserLatch.countDown();
            startFilteringSignal.countDown();
            String loadTime;
            String renderTime;
            try {
                startFilteringSignal.await();
                //press Apply Filters button
                System.out.println(" ---- Thread " + threadIndex + " - triggering filtering.");
                waitForReadyState(driver);
                browser.$("#applyFilterBtnId").scrollIntoCenter();
                driver.findElement(By.cssSelector("#applyFilterBtnId")).click();

                collectDataSignal.countDown();
                collectDataSignal.await();

                waitForReadyState(driver);
                loadTime = driver.findElement(By.cssSelector("#loading-time")).getText();
                renderTime = driver.findElement(By.cssSelector("#rendered-time")).getText();

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println(" ---- Thread " + threadIndex + " closing browser instance.");
            driver.quit();

            reportData.addRow(new ReportRow("Thread" + threadIndex, loginPageLoadDuration, loadTime, renderTime, ""));
            System.out.println(" ---- Thread " + threadIndex + " - reached end of lifecycle. Added data to report.");
        } catch (Exception e) {
            System.out.println(" ---- Thread " + threadIndex + " had exception");
            throw new RuntimeException(e);
        } finally {
            collectDataSignal.countDown();
            startFilteringSignal.countDown();
            doneSignal.countDown();
        }
    }

    public void waitForReadyState(WebDriver driver){
        long startTime = System.currentTimeMillis();
        while (
            driver.findElement(By.cssSelector("#PowerBiEmbededStatusId")).getText().contains("Not ready")
            && System.currentTimeMillis() - startTime < 100000)
        {
        }
    }
}
