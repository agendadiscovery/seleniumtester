package com.agendadiscovery.crawlers

import org.openqa.selenium.By
import org.openqa.selenium.NoSuchElementException
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.FluentWait
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.time.Year
import java.util.concurrent.TimeUnit
import com.agendadiscovery.DocumentWrapper

import com.agendadiscovery.helpers.QuickMatch

//Mostly left as is.  Modified Xpaths and variable names to make it work.  Randall 2/21/19
//url: http://youngtownaz.hosted.civiclive.com//cms/One.aspx?pageId=13406575&portalId=12609077&objectId.259675=13424223&contextId.259675=13406577&parentId.259675=13406578
public class AZ_youngtown_citycountycouncil_agenda extends BaseCrawler{
    private static final Logger log = LoggerFactory.getLogger(this.class)
    int current_year = Year.now().getValue()
    List <WebElement> docList = []
    
    // http://chromedriver.chromium.org/getting-started
    public List getDocuments(String baseUrl) throws Exception {
        log.info("Starting AZ Youngtown Selenium crawl")
        log.info("Requesting baseURL: "+baseUrl)

        try {
            driver.get(baseUrl)
            // Wait for sort to be displayed
            By sortLink = By.xpath("//*[@id=\"listHeader\"]/div[1]/div[1]/a")
            wait.until(ExpectedConditions.presenceOfElementLocated(sortLink))
            // Click sort to avoid paging and put 2019 on top
            // Asc sort
            driver.findElement(sortLink).click()
            // desc sort
            driver.findElement(sortLink).click()

            By yearLink = By.xpath("/html/body/form/div[4]/div/div[2]/div[5]/div/div[1]/div[2]/div[2]/div/div/div[2]/div[2]/div[5]/ul/li/a[div/div/div[text()=2019]]")

            // Click the 2019 row
            driver.findElement(yearLink).click()
            sleep(2500)

            // Wait for the first PDF icon to show (ajax to finish)
            By firstPdfIcon = By.xpath("//li[2]/a[1]/div/div[2]/div/em[contains(@class,\"fa-file-pdf-o\")]")
            wait.until(ExpectedConditions.presenceOfElementLocated(firstPdfIcon))

            // Iterate through the pages
            int pageNum = 1
            driver.findElementsByXPath("//div[@class=\"PO-paging\"]/ul[@class=\"PO-pageButton\"]/li/a[contains(@class,\"number\")]").each{ WebElement we ->
                log.info("Processing page: ${pageNum}")
                we.click()
                sleep(2500)
                driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS)
                // Parse the links
                docList = docList + getDocumentsByPage(driver)
                pageNum++
            }
        } catch (Exception e) {
            log.info(e.message)
            e.printStackTrace(System.out)
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
            log.info("Processing row: ${i}")
            DocumentWrapper doc = new DocumentWrapper()
            // Row element data will be relative to
            WebElement webElement = webElements.get(i)

            // Xpath Selectors
            By titleBy = By.xpath("./a/div/div/div[contains(@class,\"docTitle\")]")
            By dateBy = titleBy // same in this case
            By urlBy = By.xpath("./a")

            // Get data
            String title = webElement.findElement(titleBy).getText()
            String dateStr = QuickMatch.match("[0-9]{2}.[0-9]{2}.[0-9]{2}",webElement.findElement(dateBy).getText() )
            String url = webElement.findElement(urlBy).getAttribute("href")

            doc.title = title
            doc.dateStr = dateStr
            doc.link = url

//            log.info("\tTitle: ${title}")
//            log.info("\tDate: ${dateStr}")
//            log.info("\tUrl: ${url}")

            docList.add(doc)
        }

        return docList
    }

}
