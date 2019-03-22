package com.agendadiscovery.crawlers

import com.agendadiscovery.DocumentWrapper
import com.agendadiscovery.helpers.QuickMatch
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.lang.reflect.Array
import java.time.Year
import java.util.concurrent.TimeUnit
import java.util.regex.Matcher
//dl link ->http://jp-appserver.jeffparish.net/agenda/12192018/agenda121918_affidavit.pdf
class LA_jeffersonparish_citycouncil_agenda extends BaseCrawler {
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
        List<WebElement> rows = driver.findElementsByXPath("//table[@id=\"MainContent_grdAgenda\"]//tbody//tr")
        Integer index = 0
        for (WebElement row : rows) {
            DocumentWrapper doc = new DocumentWrapper()
            try {
                //grab document stuff
                doc.title = "City Council Meeting " + row.findElementByXPath(".//td[1]").getText()
                //log.info("\tTitle: ${doc.title}")
                doc.dateStr = row.findElementByXPath(".//td[1]").getText()
                //log.info("\tDate: ${doc.dateStr}")

                //parse link and remake
                List<String> link_dates = formatDate(doc.dateStr)
                doc.link = 'http://jp-appserver.jeffparish.net/agenda/' + link_dates[0] + '/agenda' + link_dates[1] + '_affidavit.pdf'
                //log.info("\tUrl: ${doc.link}")
            }
            catch (Exception e) {
                log.debug(e.message)
                e.printStackTrace(System.out)  //skip bad lines but print errors
            }
            finally {
                docList = docList + doc
                //debug
                log.info("\tTitle: ${doc.title}")
                log.info("\tDate: ${doc.dateStr}")
                log.info("\tUrl: ${doc.link}")
            }
        }
    } //end getDocumentsByPage()

    private List<String> formatDate(String date){
        Matcher m = QuickMatch.matchGroups("(\\d+)[^0-9](\\d+)[^0-9](\\d+)", date)
        String d1
        String d2
        String d3
        //add zero to single digits
        if (m != null) {
            d1 = m.group(1)
            d2 = m.group(2)
            d3 = m.group(3)

            //add zero if needed
            if(d1.length() == 1){
                d1 = "0" + d1
            }
            if(d2.length() == 1){
                d2 = "0" + d2
            }
            if(d3.length() == 1){
                d3 = "0" + d3
            }
        }
        return [d1 + d2 + d3 , d1+ d2 + d3.substring(2,4)]
    }//end formatDate()
} // end class
