package com.agendadiscovery.crawlers

import com.agendadiscovery.DocumentWrapper
import com.agendadiscovery.helpers.QuickMatch
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.time.Year
import java.util.concurrent.TimeUnit
import java.util.regex.Matcher

class TEMPLATE_single_item_upcoming extends BaseCrawler {
    private static final Logger log = LoggerFactory.getLogger(this.class)
    int current_year = Year.now().getValue()
    List<WebElement> docList = []

    public List getDocuments(String baseUrl) throws Exception {
        log.info("Starting crawler " + this.class.name)
        log.info("Requesting baseURL: " + baseUrl)
        try {
            driver.manage().timeouts() implicitlyWait(200, TimeUnit.MILLISECONDS)
            driver.get(baseUrl);
            sleep(3000)
            //grab links
            try {
                getDocumentByPage(driver)
            }
            catch (Exception e) {
                log.debug(e.message)
                e.printStackTrace(System.out)  //skip bad lines but print errors
            }
        } catch (Exception e) {
            log.debug(e.message)
            e.printStackTrace(System.out)
            throw e  //throw for total failure
        } finally {
            driver.quit()
            return docList
        }
    } //end getDocuments

    public void getDocumentByPage(WebDriver driver) throws Exception {
        List<WebElement> links = driver.findElementsByXPath("//div[@dir=\"ltr\"][div[contains(text(),\"Agenda\")]]/div[position() < 21]//a")
        for (WebElement link : links) {
            //grab document stuff
            DocumentWrapper doc = new DocumentWrapper()
            doc.title = link.getText()
            doc.dateStr = doc.title
            String title = link.getText()
            //prep for doc.link
            Matcher m = QuickMatch.matchGroups("(.+)\\b([0-9]+.[0-9]+.[0-9]+.+)", title)
            if (m != null) {
                String title_1 = m.group(1)
                title_1 = title_1.toLowerCase()
                String title_2 = m.group(2)
                //concat link  (90% accuracy on their naming convention.  can be inconsistent)
                doc.link = "https://sites.google.com/a/elycity.com/city-of-ely/documents/" + title_1 + title_2 + ".pdf?attredirects=0&d=1"
            } else {
                doc.link = "no match, skipping"
                continue
            }
            docList = docList + doc
            //debug
            log.info("\tTitle: ${doc.title}")
            log.info("\tDate: ${doc.dateStr}")
            log.info("\tUrl: ${doc.link}")
        } //end getDocumentsByPage
    }

}
