package com.agendadiscovery.crawlers

import com.agendadiscovery.DocumentWrapper
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.ExpectedConditions

import java.util.concurrent.TimeUnit

public class CO_aurora_citycountycouncil_agenda extends BaseCrawler{

    // http://chromedriver.chromium.org/getting-started
    public List getDocuments(String baseUrl) throws Exception {
        log.debug("Starting AZ Youngtown Selenium crawl")
        log.debug("Requesting baseURL: "+baseUrl)

        try {
            driver.get(baseUrl)

            // Wait for the first PDF icon to show (ajax to finish)
            By firstPdfIcon = By.xpath("//li[2]/a[1]/div/div[2]/div/em[contains(@class,\"fa-file-pdf-o\")]")
            wait.until(ExpectedConditions.presenceOfElementLocated(firstPdfIcon))

            By rightArrowBy = By.xpath("//*[@id=\"PO-documentContainer_60273\"]/div[2]/div[7]/ul/li[7]/a/em")
            boolean hasNext = true

            while(hasNext){
                By rowSelector = By.xpath("//ul[@id=\"documentList\"]/li[position()>1]")
                List<WebElement> rowElements = driver.findElements(rowSelector)

                for(int i=0; i<rowElements.size(); i++){
                    WebElement row = rowElements.get(i)
                    DocumentWrapper doc = new DocumentWrapper()

                    // Xpath Selectors
                    By titleBy = By.xpath("./a/div/div/div[contains(@class,\"docTitle\")]")
                    By dateBy = titleBy // same in this case
                    By urlBy = By.xpath("./a")

                    // Get data
                    doc.title = row.findElement(titleBy).getText()
                    doc.dateStr = row.findElement(dateBy).getText()
                    doc.link = row.findElement(urlBy)?.getAttribute("href")

                    log.debug("\tTitle: ${doc.title}")
                    log.debug("\tDate: ${doc.dateStr}")
                    log.debug("\tUrl: ${doc.link}")

                    docList.add(doc)
                }

                // Grab right arrow button
                WebElement rightArrowWE = driver.findElement(rightArrowBy)

                // Sometimes the arrow is hidden
                if(rightArrowWE?.isDisplayed()){
                    // Click the right arrow
                    rightArrowWE.click()
                    sleep(5000)
                }else{ // no more arrow
                    hasNext = false
                }
            }
        } catch (Exception e) {
            log.error("Selenium crawl error: "+e)
            e.printStackTrace(System.out)
        } finally{
            driver.quit()
            return docList
        }
    }

}
