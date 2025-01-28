package com.gw;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideDriver;

import java.time.Duration;

public class CookieHandler {

    public void acceptCookies(SelenideDriver driver) {
        driver.$("#ch2-dialog").should(Condition.visible, Duration.ofSeconds(60))
                .$x(".//button[1]").should(Condition.visible, Duration.ofSeconds(60)).click();
    }
}
