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

//http://cityofmaryesther.com/DocumentCenter/Index/153
//years -> //div[@id="AjaxTreeView"]/ul/li/ul/li
// agendas ->  //div[@id="Grid"]//table//tbody//tr
class FL_maryesther_citycouncil_agenda extends BaseCrawler {
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
            List<WebElement> years = driver.findElementsByXPath("//div[@id=\"AjaxTreeView\"]/ul/li/ul/li//span")
            //cycle years
            for (WebElement year : years) {
                try {
                    year.click()
                    sleep(1000)
                    getDocumentByPage(driver)
                }
                catch (Exception e) {
                    log.debug(e.message)
                    e.printStackTrace(System.out)  //skip bad lines but print errors
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
        List<WebElement> agendas = driver.findElementsByXPath("//div[@id=\"Grid\"]//table//tbody//tr/td[2]//a")
        Integer index = 0
        for (WebElement agenda : agendas) {
            DocumentWrapper doc = new DocumentWrapper()
            try {
                //grab document stuff
                doc.title = agenda.getText()
                log.info("\tTitle: ${doc.title}")
                doc.dateStr = doc.title
                log.info("\tDate: ${doc.dateStr}")
                doc.link = agenda.getAttribute('href')
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
}