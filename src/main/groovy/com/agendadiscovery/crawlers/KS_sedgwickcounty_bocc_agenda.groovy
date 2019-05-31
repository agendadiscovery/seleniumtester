package com.agendadiscovery.crawlers

import com.agendadiscovery.DocumentWrapper
import com.agendadiscovery.helpers.QuickMatch
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.swing.text.Document
import java.time.Year
import java.util.concurrent.TimeUnit
import java.util.regex.Matcher

class KS_sedgwickcounty_bocc_agenda extends BaseCrawler {
    private static final Logger log = LoggerFactory.getLogger(this.class)
    int current_year = Year.now().getValue()
    List<WebElement> docList = []

    public List getDocuments(String baseUrl) throws Exception {
        log.info("Starting crawler " + this.class.name)
        log.info("Requesting baseURL: " + baseUrl)
        try {
            driver.manage().timeouts() implicitlyWait(200, TimeUnit.MILLISECONDS)
            driver.get(baseUrl);
            sleep(2000)
            //grab links
//            WebElement year = driver.findElementByXPath("")
//            year.click()
            getDocumentByPage(driver)
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
        //click regular meetings
        WebElement regularMeetings = driver.findElementByXPath("//div[contains(@id,\"Collapsibl\")][contains(.,\"Regular Meetings\")]")
        regularMeetings.click()
        sleep(2000)

        //cycle through days
        List<WebElement> days = driver.findElementsByXPath("//div[contains(@id,\"CollapsiblePanel2019\")]//tr[@class=\"listingRow\"][contains(.,\"Agenda\")]")
        Integer index = 0
        for (WebElement day : days) {
            DocumentWrapper doc = new DocumentWrapper()
            try {
                //grab document stuff
                doc.title = day.findElementByXPath("./td[1]").getText()
                //log.info("\tTitle: ${doc.title}")
                doc.dateStr = day.findElementByXPath("./td[2]").getText()
                //log.info("\tDate: ${doc.dateStr}")
                doc.link = day.findElementByXPath("./td[3]//a").getAttribute('href')
                if(doc.link == "" || doc.link == null){break}
                //log.info("\traw Url: ${doc.link}")
            }
            catch (Exception e) {
                log.debug(e.message)
                e.printStackTrace(System.out)  //skip bad lines but print errors
            }
            finally {
                docList = docList + doc
            }
        }//end for loop days

        //change link for each
        for(DocumentWrapper doc: docList){
            driver.get(doc.link)
            sleep(1000)
            String raw_link = driver.getCurrentUrl()
            doc.link = processLink(raw_link)
            //log.info("\tfinal Url: ${doc.link}")
        }
    } //end documentsByPage()

    public String processLink(String raw_link){
                //parse link and remake
                Matcher m = QuickMatch.matchGroups(".+id=([0-9]+)", raw_link)
                if (m != null) {
                    String id = m.group(1)
                    //dl link example -> https://drive.google.com/uc?export=download&id=1aAivoeNydSf761Hmih7lHWH7rLXkhuzV
                    return "https://imaging.sedgwickcounty.org/OnBaseAgendaOnline/Documents/ViewAgenda?meetingId=${id}&doctype=1"
                } else {
                    return "no match, skipping"
                }
    }
} // end class
