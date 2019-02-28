package com.agendadiscovery.crawlers

import com.agendadiscovery.DocumentWrapper
import com.agendadiscovery.helpers.QuickMatch
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.ExpectedConditions
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.time.Year
import java.util.concurrent.TimeUnit

//Grabs current / upcoming document
class NE_casscounty_countycouncil_agenda extends BaseCrawler {
    private static final Logger log = LoggerFactory.getLogger(this.class)
    int current_year = Year.now().getValue()
    List <WebElement> docList = []

    public List getDocuments(String baseUrl) throws Exception {
        log.info("Starting crawler " + this.class.name)
        log.info("Requesting baseURL: "+baseUrl)
        try {
            driver.manage().timeouts()implicitlyWait(5, TimeUnit.SECONDS)
            driver.get(baseUrl);

            //notes DL link takes ID and subtracts 1

            //grab document stuff
            DocumentWrapper doc = new DocumentWrapper()
            doc.title = driver.findElementByXPath("//tbody/tr/td[2]/a[contains(translate(. , \"AGEND\",\"agend\") , \"agenda\")]").getText()
            doc.dateStr = doc.title

            String firstLink =  driver.findElementByXPath("//tbody/tr/td[2]/a[contains(translate(. , \"AGEND\",\"agend\") , \"agenda\")]").getAttribute("href")
            String firstId = QuickMatch.matchGroup("ID=([0-9]+)",firstLink)
            String finalLink = "http://173.190.246.54/richshow2.php?ID=" + firstId


            //circumvent popup for pdf link
            driver.get(finalLink)
            sleep(1000)

            //grab data
            doc.link = driver.findElementByXPath("//body//a").getAttribute("href")

            docList = docList + doc

            //debug
            log.info("\tTitle: ${doc.title}")
            log.info("\tDate: ${doc.dateStr}")
            log.info("\tUrl: ${doc.link}")

        } catch (Exception e) {
            log.info(e.message)
            e.printStackTrace(System.out)
            System.out.println("Continuing with next link")
        } finally{
            driver.quit()
            return docList
        }
    }
}

