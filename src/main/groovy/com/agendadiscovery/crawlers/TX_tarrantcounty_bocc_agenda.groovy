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

//http://tarrantcounty.granicus.com/ViewPublisher.php?view_id=6
class TX_tarrantcounty_bocc_agenda extends BaseCrawler {
    private static final Logger log = LoggerFactory.getLogger(this.class)
    int current_year = Year.now().getValue()
    List<WebElement> docList = []

    int position = 1
    public List getDocuments(String baseUrl) throws Exception {
        log.info("Starting crawler " + this.class.name)
        log.info("Requesting baseURL: " + baseUrl)
        try {
            driver.manage().timeouts() implicitlyWait(200, TimeUnit.MILLISECONDS)
            driver.get(baseUrl);
            sleep(2000)
            getDocumentByPage(driver)
            position++

            driver.get(baseUrl);
            sleep(2000)
            //click next year
            WebElement year = driver.findElementByXPath("//li[@class=\"TabbedPanelsTab\"][contains(.,\"${current_year -1}\")]")
            year.click()
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
        List<WebElement> days = driver.findElementsByXPath("//div[@class=\"TabbedPanelsContentGroup\"]/div[contains(@class,\"TabbedPanelsContent\")][${position.toString()}]//tbody//tr[td[@class = 'listItem']]")
//        List<WebElement> daysUpcoming = driver.findElementsByXPath("//h2[contains(.,\"Upcoming\")]/following-sibling::table//tr[@class=\"listingRow\"][contains(.,\"Agenda\")]")
//        days.addAll(daysUpcoming)

        Integer index = 0
        for (WebElement day : days) {
            DocumentWrapper doc = new DocumentWrapper()
            try {
                //grab document stuff
                doc.title = day.findElementByXPath("./td[1]").getText()
                log.debug("\tTitle: ${doc.title}")
                doc.dateStr = day.findElementByXPath("./td[1]").getText()
                log.debug("\tDate: ${doc.dateStr}")
                doc.link = day.findElementByXPath("./td//a[text() = \"Agenda\"]").getAttribute('href')
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

    //process each link of docList to process
    public void processLinks(){
        //change link for each
        driver.manage().timeouts().pageLoadTimeout(1, TimeUnit.SECONDS);
        for(DocumentWrapper doc: docList){
            try{driver.get(doc.link)}
            catch (TimeoutException e) {log.debug("intentional timeout exception.  don't worry")}
            sleep(200)
            String raw_link = driver.getCurrentUrl()
            doc.link = processLink(raw_link)
            log.debug("\tfinal Url: ${doc.link}")
        }
        driver.manage().timeouts().pageLoadTimeout(20, TimeUnit.SECONDS);
    }

    //process single link
    public String processLink(String raw_link){
        //parse link and remake
        Matcher m = QuickMatch.matchGroups(".+id=([0-9]+)", raw_link)
        if (m != null) {
            String id = m.group(1)
            //dl link example -> https://drive.google.com/uc?export=download&id=1aAivoeNydSf761Hmih7lHWH7rLXkhuzV
            return "http://courtbook.tarrantcounty.com/sirepub/agview.aspx?agviewmeetid=${id}&agviewdoctype=agenda"
        } else {
            return "no match, skipping"
        }
    }
} // end class
