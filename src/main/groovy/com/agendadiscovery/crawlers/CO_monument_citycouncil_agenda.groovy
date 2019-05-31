package com.agendadiscovery.crawlers

import com.agendadiscovery.DocumentWrapper
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.ExpectedConditions
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.time.Year
import java.util.concurrent.TimeUnit

//https://monumenttownco.documents-on-demand.com/?l=a4ffc44a70ad47f08b547a84955bea55&r=2019
class CO_monument_citycouncil_agenda extends BaseCrawler {
    private static final Logger log = LoggerFactory.getLogger(this.class)
    int current_year = Year.now().getValue()
    List <WebElement> docList = []

    public List getDocuments(String baseUrl ) throws Exception {
        log.info("Starting crawler " + this.class.name)
        log.info("Requesting baseURL: " + baseUrl)
        driver.manage().timeouts().implicitlyWait(2,TimeUnit.SECONDS)

        try {
            driver.get(baseUrl)
            sleep(6000)
            driver.manage().timeouts().implicitlyWait(2,TimeUnit.SECONDS)

            getDocumentsByPage(driver)

            //grab 2018.  click 2018
//            WebElement year2019 = driver.findElementByXPath("//li[@class=\"folder\"][contains(.,\"2019\")][1]")
//            year2019.click()
//            sleep(1000)

//            getDocumentsByPage(driver)


        } catch (Exception e) {
            log.info("Selenium crawl error: "+e)
            e.printStackTrace(System.out)
        } finally{
            driver.quit()
            return docList
        }
    }

    public void getDocumentsByPage(WebDriver driver){
        try{
            List<WebElement> days = driver.findElementsByXPath("//ul[@id=\"ClientFileTree_2_ul\"]//li")
            List<String> daysStr = []
            int i = 0;
            for (WebElement day : days) {
                daysStr[i] = day.getText()
                i++
            }

            //cycle each day
            for (String dayStr : daysStr) {
                //log.debug("Total days: " + daysStr.size().toString())
                DocumentWrapper doc = new DocumentWrapper()
                WebElement day = driver.findElementByXPath("//ul[@id=\"ClientFileTree_2_ul\"]//li[contains(.,\"${dayStr}\")]//span[contains(@id,\"_span\")]")
//
//                    Actions actions = new Actions(driver);
//                    actions.moveToElement(day);
//                    actions.perform();
//                    sleep(1000)
                //ul[@id=\"ClientFileTree_2_ul\"]//li[contains(.,\"${dayStr}\")]//span[contains(@id,\"_span\")]
                doc.title = day.getText()
                doc.dateStr = day.getText()

                day.click()
                //log.debug(day.toString())
                sleep(2000)

                //grab Agenda (Iframe)
                WebElement agenda = driver.findElementByXPath("//iframe[@id=\"pdfIFrame\"]")
                doc.link = agenda.getAttribute("src")

                log.debug("\tTitle: ${doc.title}")
                log.debug("\tDate: ${doc.dateStr}")
                log.debug("\tUrl: ${doc.link}")
                docList = docList + doc

                //go back
                WebElement back = driver.findElementByXPath("//li[@id=\"ClientFolderTree_3\"]//span[contains(.,\"BOT Agenda\")]")
                back.click()
                sleep(1000)
                WebElement year2019 = driver.findElementByXPath("//li[@id=\"ClientFileTree_2\"]//span[@id=\"ClientFileTree_2_switch\"]")
                year2019.click()
                sleep(1000)
            }//end cycle days
        }
        catch (Exception e) {
            log.info("Selenium crawl error: "+e)
            e.printStackTrace(System.out)
        }
    }
} //end class