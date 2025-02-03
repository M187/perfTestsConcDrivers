package com.gw;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideConfig;
import com.codeborne.selenide.SelenideDriver;
import com.gw.report.ReportModel;
import com.gw.report.ReportRow;
import lombok.AllArgsConstructor;
import org.apache.commons.cli.CommandLine;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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
            SelenideConfig config = new SelenideConfig();
            config.browser("edge");
            config.browserSize("1920x1080");
            config.headless(commandLine.hasOption(ARG_HEADLESS));


            SelenideDriver browser = new SelenideDriver(config);
            long before = System.currentTimeMillis();
            browser.open("https://partner2-qa.colonnade.pl/myColonnade/dashboard");
            WebDriver driver = browser.getWebDriver();
            driver.manage().timeouts().implicitlyWait(100, TimeUnit.SECONDS);
            System.out.println(" ---- Thread " + threadIndex + " - SSP login page reached.");

//            browser.$("#email").should(Condition.visible, Duration.ofSeconds(60));
            driver.findElement(By.cssSelector("#email")).isDisplayed();
            long loginPageLoadDuration = System.currentTimeMillis() - before;
            driver.findElement(By.cssSelector("#email")).sendKeys(commandLine.getOptionValue(ARG_LOGIN));
            driver.findElement(By.cssSelector("#password")).sendKeys(commandLine.getOptionValue(ARG_PASSWORD));
            driver.findElement(By.cssSelector("#next")).click();

            new CookieHandler().acceptCookies(driver);
            System.out.println(" ---- Thread " + threadIndex + " - Succ logged into SSP instance and accepted cookies");

            //move to reports tab
            driver.findElement(By.cssSelector("#dashboard_reporting_id")).click();

            //set parameters for filtering
            if (commandLine.getOptionValue(ARG_USER) != null) {
//                browser.$("#userConfigFilterEmailId").should(Condition.visible, Duration.ofSeconds(100)).$x(".//div[contains(text(),'" + commandLine.getOptionValue(ARG_USER) + "')]").parent().click();
                driver.findElement(By.cssSelector("#userConfigFilterEmailId")).findElement(By.xpath(".//div[contains(text(),'" + commandLine.getOptionValue(ARG_USER) + "')]/..")).click();
            } else if (commandLine.getOptionValue(ARG_SUPERVISORS) != null) {
                for (String producer : commandLine.getOptionValue(ARG_SUPERVISORS).split(",")) {
//                    browser.$("#userConfigFilterProducerCodeId").should(Condition.visible, Duration.ofSeconds(100)).$x(".//div[contains(text(),'" + producer + "')]").parent().click();
                    driver.findElement(By.cssSelector("#userConfigFilterProducerCodeId")).findElement(By.xpath(".//div[contains(text(),'" + producer + "')]/..")).click();
                }
            }

            nextBrowserLatch.countDown();
            startFilteringSignal.countDown();
            String noOfResults;
            long listLoadTime;
            try {
                startFilteringSignal.await();
                //press Apply Filters button
                System.out.println(" ---- Thread " + threadIndex + " - triggering filtering.");
//                browser.$("#applyFilterBtnId").should(Condition.exist, Duration.ofSeconds(100));
                driver.findElement(By.cssSelector("#applyFilterBtnId")).isDisplayed();
                before = System.currentTimeMillis();
//                browser.$("#applyFilterBtnId").click();
                driver.findElement(By.cssSelector("#applyFilterBtnId")).click();

                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(100));

                //wait for page load finished
                WebDriver frame = wait.until( ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.xpath(".//div[contains(@class,'EmbeddedPowerBI_powerBiEmbededContainer__')]/iframe")));
//                noOfResults = browser.$x(".//div[contains(@aria-rowindex,'501')]").should(Condition.exist, Duration.ofSeconds(100)).$x(".//div[contains(@column-index,'8')]").should(Condition.exist, Duration.ofSeconds(10)).getText();
                noOfResults = driver.findElement(By.xpath(".//div[contains(@aria-rowindex,'501')]")).findElement(By.xpath(".//div[contains(@column-index,'8')]")).getText();
                listLoadTime = System.currentTimeMillis() - before;

                driver.switchTo().parentFrame();

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println(" ---- Thread " + threadIndex + " closing browser instance.");
            driver.quit();

            reportData.addRow(new ReportRow("Thread" + threadIndex, loginPageLoadDuration, listLoadTime, noOfResults));
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
