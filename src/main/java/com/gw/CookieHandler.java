package com.gw;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.sleep;

public class CookieHandler {

    public void acceptCookies(WebDriver driver) {
        WebElement element = driver.findElement(By.cssSelector("#ch2-dialog"))
                .findElement(By.xpath(".//button[1]"));
        sleep(500);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        wait.until(ExpectedConditions.elementToBeClickable(element));
        element.click();
    }

    public void acceptCookies(SelenideDriver driver) {
        driver.$("#ch2-dialog").should(Condition.exist, Duration.ofSeconds(60))
                .$x(".//button[1]").should(Condition.clickable, Duration.ofSeconds(60));
        sleep(500);
        driver.$("#ch2-dialog").$x(".//button[1]").click();
    }
}
