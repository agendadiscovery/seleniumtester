package com.agendadiscovery.crawlers

import com.agendadiscovery.DocumentWrapper
import org.openqa.selenium.By
import org.openqa.selenium.NoSuchElementException
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.FluentWait

import java.util.concurrent.TimeUnit

abstract class BaseCrawler {
    WebDriver driver
    FluentWait<WebDriver> wait
    List<DocumentWrapper> docList

    public BaseCrawler() {
        docList = []
        driver = new ChromeDriver()
        driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS)
        wait = new FluentWait<WebDriver>(driver).ignoring(NoSuchElementException.class);
    }

    abstract List<DocumentWrapper> getDocuments(String baseUrl);
}
