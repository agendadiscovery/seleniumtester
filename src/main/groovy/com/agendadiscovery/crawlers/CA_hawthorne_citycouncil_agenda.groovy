package com.agendadiscovery.crawlers

import com.agendadiscovery.DocumentWrapper
import com.agendadiscovery.helpers.QuickMatch
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.time.Year
import java.util.concurrent.TimeUnit
import java.util.regex.Matcher


/////////////////// UPDATE works using Import.io JSON calls.  SKIP ////////////////////////////////


//////////////////////////////////
//// Not working.  Tried running Javascript function to get url, but it is Angular so a bit mysterious
/////////////////////////////////

//idea.  change the onclick function...  then click
class CA_hawthorne_citycouncil_agenda extends BaseCrawler {
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
                e.printStackTrace(log.debug)  //skip bad lines but print errors
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
        List<WebElement> rows = driver.findElementsByXPath("//div[@class=\"results-table\"]//table/tbody/tr[contains(.,\"Agenda\")]")
        for (WebElement row : rows) {
            //grab document stuff
            try {
                DocumentWrapper doc = new DocumentWrapper()
                doc.title = row.findElementByXPath(".//td[1]").getText()
                doc.dateStr = row.findElementByXPath(".//td[2]").getText()
                if (driver instanceof JavascriptExecutor)
                {
                    log.debug("We have JavascriptExecutor")
                    doc.link = ((JavascriptExecutor)driver).executeScript("pgService.getPublishedDownloadUrl(document.id);");
                }
                //prep for doc.link
            }
            catch (Exception e){
                log.debug(e.message)
                e.printStackTrace(System.out)  //skip bad lines but print errors
            }
            finally {
                //debug
                log.info("\tTitle: ${doc.title}")
                log.info("\tDate: ${doc.dateStr}")
                log.info("\tUrl: ${doc.link}")

                docList = docList + doc
            }
        } //end getDocumentsByPage
    }

}
