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

class TEMPLATE_single_page extends BaseCrawler {
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
        List<WebElement> divs = driver.findElementsByXPath("//div[@class = \"w-dyn-items\"][contains(.//*,'${current_year}')]/div[@class = \"w-dyn-item\"]")
        Integer index = 0
        for (WebElement div : divs) {
            DocumentWrapper doc = new DocumentWrapper()
            try {
                //grab document stuff
                doc.title = div.findElementByXPath(".//h1").getText()
                log.info("\tTitle: ${doc.title}")
                doc.dateStr = div.findElementByXPath(".//div[@class=\"meta-tag\"]").getText()
                log.info("\tDate: ${doc.dateStr}")
                String google_drive_link = div.findElementByXPath(".//a[@class=\"drop-link\" and contains(. ,\"PDF Agenda\")]").getAttribute('href')
                log.info("\traw Url: ${google_drive_link}")
                //parse link and remake
                Matcher m = QuickMatch.matchGroups(".+id=([^/]+)", google_drive_link)
                if (m != null) {
                    String id = m.group(1)
                    //dl link example -> https://drive.google.com/uc?export=download&id=1aAivoeNydSf761Hmih7lHWH7rLXkhuzV
                    doc.link = "https://drive.google.com/uc?export=download&id=" + id
                } else {
                    doc.link = "no match, skipping"
                    continue
                }
            }
            catch (Exception e) {
                log.debug(e.message)
                e.printStackTrace(System.out)  //skip bad lines but print errors
            }
            finally {
                index++;
                if (index >= 60) {
                    break
                }
                if (doc.title == "") {
                    break
                }
                docList = docList + doc
                //debug
//                log.info("\tTitle: ${doc.title}")
//                log.info("\tDate: ${doc.dateStr}")
//                log.info("\tUrl: ${doc.link}")
            }
        }
    } //end documentsByPage()
} // end class
