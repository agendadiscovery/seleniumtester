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

//http://media.avcaptureall.com/#/?prefilter=761%2c4551&target=_blank&view=list&tabs=rec%7Cupc
class WA_gigharbor_citycouncil_agenda extends BaseCrawler {
    private static final Logger log = LoggerFactory.getLogger(this.class)
    int current_year = Year.now().getValue()
    List<WebElement> docList = []

    public List getDocuments(String baseUrl) throws Exception {
        log.info("Starting crawler " + this.class.name)
        log.info("Requesting baseURL: " + baseUrl)
        try {
            driver.manage().timeouts() implicitlyWait(5000, TimeUnit.MILLISECONDS)
            driver.get(baseUrl);
            sleep(3000)
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
        WebElement showmoreBtn = driver.findElementByXPath("//button[contains(.,\"Show More\")]")
        showmoreBtn.click()
        sleep(4000)
        List<WebElement> agendaWebs = driver.findElementsByXPath("//div[@data-ng-if=\"showListView\"]/table//tbody/tr[position() > 1]/td[2]//a")
        List<String> agendaLinks = []

        Integer index = 0
        for(WebElement agendaWeb: agendaWebs){
            try{
                agendaLinks[index] = agendaWeb.getAttribute('href')
            }
            catch(Exception e){
                log.debug('error at agendaLink generation')
                e.printStackTrace(System.out)
            }
            finally{
                index++
            }
        }
        index = 0
        for (String agendaLink : agendaLinks) {
            //visit url
            driver.get(agendaLink)
            sleep(3500)
            DocumentWrapper doc = new DocumentWrapper()
            try {
                //grab document stuff
                doc.title = driver.findElementByXPath("//div[@id=\"videoTitle\"]").getText()
                log.info("\tTitle: ${doc.title}")
                doc.dateStr = driver.findElementByXPath("//span[@id=\"videoRecorded\"]").getText()
                log.info("\tDate: ${doc.dateStr}")
                doc.link = driver.findElementByXPath("//iframe[@id=\"docIframe\"]").getAttribute('src')
                log.info("\tlink: ${doc.link}")
            }
            catch (Exception e) {
                log.debug(e.message)
                e.printStackTrace(System.out)  //skip bad lines but print errors
            }
            finally {
                log.debug("index " + index.toString())
                index++;
                if (index >= 65) {
                    break
                }
                if (doc.title == "") {
                    break
                }
                docList = docList + doc
            }
        }
    } //end documentsByPage()
} // end class

