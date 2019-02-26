package com.agendadiscovery.crawlers

import com.agendadiscovery.DocumentWrapper
import org.apache.log4j.Logger
import org.openqa.selenium.By
import org.openqa.selenium.NoSuchElementException
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.FluentWait
import org.slf4j.LoggerFactory

import java.time.Year
import java.util.concurrent.TimeUnit

import com.agendadiscovery.helpers.QuickMatch

public class AZ_sedona_citycountycouncil_agenda extends BaseCrawler {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(this.class)
    int current_year = Year.now().getValue()
    List <WebElement> docList = []
    
    // http://chromedriver.chromium.org/getting-started
    public List getDocuments(String baseUrl) throws Exception {
        log.info("Starting AZ Sedona Selenium crawl")
        log.info("Requesting baseURL: "+baseUrl)

        try {
            driver.get(baseUrl)
            // Wait for sort to be displayed
            By councilLink = By.xpath("//*[@id=\"GridView1\"]/tbody/tr//td[2]/a[text()=\"City Council\"]")
            wait.until(ExpectedConditions.presenceOfElementLocated(councilLink))

            // Click city council link
            driver.findElement(councilLink).click()
            sleep(5000)

            // Wait for agenda folders to load
            By agendaFoldersBy = By.xpath("//*[@id=\"GridView1\"]/tbody/tr/td[2]/a")
            wait.until(ExpectedConditions.presenceOfElementLocated(agendaFoldersBy))

            List<WebElement> folderWebElements = driver.findElements(agendaFoldersBy)

            // Iterate through the pages
            for(int i=0; i<folderWebElements.size(); i++){
                WebElement folder = folderWebElements.get(i)
                folder.click()
                sleep(2500)

                By agendaBy = By.xpath("//*[@id=\"GridView1\"]/tbody/tr//a[not(contains(text(),\"Minutes\"))]")

                driver.findElements(agendaBy).each { WebElement agenda ->
                    DocumentWrapper doc = new DocumentWrapper()

                    // Get data
                    doc.title = agenda.getText()
                    doc.dateStr = agenda.getText().replaceAll("[a-zA-Z]", "")
                    log.info("doc.dateStr " + doc.dateStr)//debug
                    doc.link = agenda.getAttribute("href")

//                    log.info("\tTitle: ${doc.title}")
//                    log.info("\tDate: ${doc.dateStr}")
//                    log.info("\tUrl: ${doc.link}")
                    docList.add(doc)
                }

                driver.navigate().back()
                Thread.sleep(2000)
                // Refresh folder elements
                folderWebElements = driver.findElements(agendaFoldersBy)
            }
        } catch (Exception e) {
            log.info(e.message)
            e.printStackTrace(System.out)
            throw e
        } finally{
            driver.quit()
            return docList
        }
    }
}


