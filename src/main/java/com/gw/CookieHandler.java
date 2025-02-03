package com.gw;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class CookieHandler {

    public void acceptCookies(WebDriver driver) {
        driver.findElement(By.cssSelector("#ch2-dialog"))
                .findElement(By.xpath(".//button[1]")).click();
    }
}
