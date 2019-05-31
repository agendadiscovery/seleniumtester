package com.agendadiscovery.crawlers

import com.agendadiscovery.DocumentWrapper
import com.agendadiscovery.helpers.QuickMatch
import org.openqa.selenium.TimeoutException
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.time.Year
import java.util.concurrent.TimeUnit
import java.util.regex.Matcher

//https://www.ci.tracy.ca.us/?navid=282
class CA_tracy_citycoucnil_agenda extends BaseCrawler {
    private static final Logger log = LoggerFactory.getLogger(this.class)
    int current_year = Year.now().getValue()
    List<WebElement> docList = []

    int position = 2
    public List getDocuments(String baseUrl) throws Exception {
        log.info("Starting crawler " + this.class.name)
        log.info("Requesting baseURL: " + baseUrl)
        try {
            driver.manage().timeouts() implicitlyWait(200, TimeUnit.MILLISECONDS)
            driver.get(baseUrl);
            sleep(1000)
            //click next year
            WebElement year = driver.findElementByXPath(" //div[@id=\"portal9503\"]")
            year.click()
            getDocumentByPage(driver)
            position++
            position++

            driver.get(baseUrl);
            sleep(2000)
            //click next year
            WebElement nextYear = driver.findElementByXPath(" //div[@id=\"portal8360\"]")
            nextYear.click()
            sleep(1500)
            getDocumentByPage(driver)

            processLinks()
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
        //cycle through days
        List<WebElement> days = driver.findElementsByXPath("//div[contains(@class, \"accordion\")]/div[${position}]//tbody/tr")
//        List<WebElement> daysUpcoming = driver.findElementsByXPath("//h2[contains(.,\"Upcoming\")]/following-sibling::table//tr[@class=\"listingRow\"][contains(.,\"Agenda\")]")
//        days.addAll(daysUpcoming)

        Integer index = 0
        for (WebElement day : days) {
            DocumentWrapper doc = new DocumentWrapper()
            try {
                //grab document stuff
                doc.title = day.findElementByXPath(" ./td[3]").getText()
                log.debug("\tTitle: ${doc.title}")
                doc.dateStr = day.findElementByXPath(" ./td[1]").getText()
                log.debug("\tDate: ${doc.dateStr}")
                doc.link = day.findElementByXPath(" ./td[4]//a[1]").getAttribute('href')
                log.debug("\traw Url: ${doc.link}")
            }
            catch (Exception e) {
                log.debug(e.message)
                e.printStackTrace(System.out)  //skip bad lines but print errors
            }
            finally {
                if(doc.link == "" || doc.link == null){continue}
                docList = docList + doc
            }
        }//end for loop days
    } //end documentsByPage()

    //process each link of docList
    public void processLinks(){
        //click link for redirect
        driver.manage().timeouts().pageLoadTimeout(1, TimeUnit.SECONDS);
        for(DocumentWrapper doc: docList){
            try{driver.get(doc.link)}
            catch (TimeoutException e) {log.debug("intentional timeout exception.  don't worry")}
            sleep(1200)
            String raw_link = driver.getCurrentUrl()
            doc.link = processLink(raw_link)
            log.debug("\tfinal Url: ${doc.link}")
        }
        driver.manage().timeouts().pageLoadTimeout(20, TimeUnit.SECONDS);
    }

    //process single link
    public String processLink(String raw_link){
        return raw_link
//        //parse link and remake
//        Matcher m = QuickMatch.matchGroups(".+id=([0-9]+)", raw_link)
//        if (m != null) {
//            String id = m.group(1)
//            //dl link example -> https://drive.google.com/uc?export=download&id=1aAivoeNydSf761Hmih7lHWH7rLXkhuzV
//            return "http://courtbook.tarrantcounty.com/sirepub/agview.aspx?agviewmeetid=${id}&agviewdoctype=agenda"
//        } else {
//            return "no match, skipping"
//        }
    }
} // end class


