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

public class CA_plymouth_citycountycouncil_agenda {

    // http://chromedriver.chromium.org/getting-started
    public List getDocuments(String baseUrl) throws Exception {
        log.debug("Requesting baseURL: "+baseUrl)
        driver.get(baseUrl)

        ////////////////////////////////////////////
        // CURRENTLY BROKEN
        ////////////////////////////////////////////

        try {
            FluentWait<WebDriver> wait = new FluentWait<WebDriver>(driver).ignoring(NoSuchElementException.class);

            // Wait for years to be displayed
            By yearLink = By.xpath("//div[@class=\"ms-List-cell\"]//a[text()=\"2018\"]")
            wait.until(ExpectedConditions.presenceOfElementLocated(yearLink))

            // Click the 2018 row
            driver.findElement(yearLink).click()

            // Wait for the first PDF icon to show (ajax to finish)
            By firstPdfIcon = By.xpath("//div[@class=\"ms-List-cell\"][1]//img")
            wait.until(ExpectedConditions.presenceOfElementLocated(firstPdfIcon))

            // Iterate through the pages
            driver.findElementsByXPath("//div[@class=\"ms-List-cell\"]").each{ WebElement we ->
                log.debug("Processing page: ${pageNum}")
                we.click()
                driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS)
            }
        } catch (Exception e) {
            log.error("Selenium crawl error: "+e)
            throw e
        } finally{
            driver.quit()
            return docList
        }
    }

    public List getDocumentsByPage(WebDriver driver) throws Exception{
        // Grab the element rows
        By rowSelector = By.xpath("//ul[@id=\"documentList\"]/li[position()>1]")
        List<WebElement> webElements = driver.findElements(rowSelector)
        List docList = []

        for(int i=0; i < webElements.size(); i++){
            log.debug("Processing row: ${i}")
            DocumentWrapper doc = new DocumentWrapper()
            // Row element data will be relative to
            WebElement webElement = webElements.get(i)

            // Xpath Selectors
            By titleBy = By.xpath("./a/div/div/div[contains(@class,\"docTitle\")]")
            By dateBy = titleBy // same in this case
            By urlBy = By.xpath("./a")

            // Get data
            String title = webElement.findElement(titleBy).getText()
            String dateStr = webElement.findElement(dateBy).getText()?.replaceAll("-"," ") // clean date a bit
            String url = webElement.findElement(urlBy).getAttribute("href")

            doc.title = title
            doc.dateStr = dateStr
            doc.link = url

            log.debug("\tTitle: ${title}")
            log.debug("\tDate: ${dateStr}")
            log.debug("\tUrl: ${url}")

            docList.add(doc)
        }

        return docList
    }

}
