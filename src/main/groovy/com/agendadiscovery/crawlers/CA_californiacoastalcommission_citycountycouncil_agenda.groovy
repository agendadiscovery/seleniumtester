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
// website https://www.coastal.ca.gov/meetings/agenda/
// links  //a[contains(@href, "pdf")][strong]
// dates //p[contains(@id, "dayExtended")]  to cycle through
// title = links getText()

class CA_californiacoastalcommission_citycountycouncil_agenda extends BaseCrawler {
    int current_year = Year.now().getValue()
    private static final Logger log = LoggerFactory.getLogger(this.class)
    List<WebElement> docList = []
    List<WebElement> dates
    WebElement date

    public List getDocuments(String baseUrl) throws Exception {
        log.info("Starting crawler " + this.class.name)
        log.info("Requesting baseURL: " + baseUrl)
        try {
            driver.manage().timeouts() implicitlyWait(2, TimeUnit.SECONDS)
            driver.get(baseUrl);
            sleep(4000)
            //grab dates
            dates = driver.findElementsByXPath("//p[contains(@id, \"dayExtended\")]")
            for(WebElement this_date: dates) {
                date = this_date
                date.click()
                sleep(2000)
                log.info("test date is..")
                log.info(date.getText())
                //grab links
                try {
                    getDocumentByPage(driver)
                }
                catch (Exception e) {
                    log.debug(e.message)
                    e.printStackTrace(log.debug)  //skip bad lines but print errors
                }
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
        List<WebElement> pdfs = driver.findElementsByXPath("//a[contains(@href, \"pdf\")][strong]")

        for (WebElement pdf : pdfs) {
            DocumentWrapper doc = new DocumentWrapper()
            try {
                //grab document stuff
                doc.title = pdf.getText()
                log.info("\tTitle: ${doc.title}")
                doc.dateStr = date.getText() + " " + current_year
                log.info("\tDate: ${doc.dateStr}")
                doc.link = pdf.getAttribute("href")
                log.info("\tUrl: ${doc.link}")
            }
            catch (Exception e) {
                log.debug(e.message)
                e.printStackTrace(System.out)  //skip bad lines but print errors
            }
            finally {
                docList = docList + doc
                //debug
//                log.info("\tTitle: ${doc.title}")
//                log.info("\tDate: ${doc.dateStr}")
//                log.info("\tUrl: ${doc.link}")
            }
        }
    } //end documentsByPage()
} // end class
