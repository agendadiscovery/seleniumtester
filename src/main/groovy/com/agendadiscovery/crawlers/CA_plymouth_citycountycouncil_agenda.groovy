package com.agendadiscovery.crawlers

import com.agendadiscovery.DocumentWrapper
import org.openqa.selenium.By
import org.openqa.selenium.NoSuchElementException
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.FluentWait

import java.util.concurrent.TimeUnit

public class CA_plymouth_citycountycouncil_agenda extends BaseCrawler {

    // http://chromedriver.chromium.org/getting-started
    public List getDocuments(String baseUrl) throws Exception {
        WebDriver driver = new ChromeDriver()
        List docList = []
        driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS)
        driver.get(baseUrl)

        try {
            log.info("No selenium yet")
            ////////////////////////////////////////////////////////////
            //
            // Add selenium code to build docList here
            //
            ////////////////////////////////////////////////////////////
        } catch (Exception e) {
            throw e
        } finally{
            driver.quit()
            return docList
        }
    }

}
