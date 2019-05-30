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

// UNFINISHED
//http://www.cityofcolusa.com/cms/one.aspx?pageId=11803948
//CA,colusa,citycouncil,agenda,http://www.cityofcolusa.com/cms/one.aspx?pageId=11803948
class CA_colusa_citycouncil_agenda extends BaseCrawler {
        private static final Logger log = LoggerFactory.getLogger(this.class)
        int current_year = Year.now().getValue()
        List<WebElement> docList = []

        public List getDocuments(String baseUrl) throws Exception {
            log.info("Starting crawler " + this.class.name)
            log.info("Requesting baseURL: " + baseUrl)
            try {
                driver.manage().timeouts() implicitlyWait(2000, TimeUnit.MILLISECONDS)
                driver.get(baseUrl);
                sleep(2000)
                //grab links
                getDocumentByPage1(driver)
                //remember to change pages  from 1 to 2

            } catch (Exception e) {
                log.debug(e.message)
                e.printStackTrace(System.out)
                throw e  //throw for total failure
            } finally {
                driver.quit()
                return docList
            }
        } //end getDocuments

        public void getDocumentByPage1(WebDriver driver) throws Exception {
            List<WebElement> months = driver.findElementsByXPath("//ul[@id=\"documentList\"]/li/a//div[contains(@class,\"docTitle\")]")
            int monthsCount = months.size()
            int monthIndex = 0

            log.debug(months.size().toString())

            int index = 0;
            List<String> monthsString = []
            for (WebElement month : months) {
                monthsString[index] = month.getText()
                index++
            }
            index = 0;
            for (String monthString : monthsString) {
                try {
                   WebElement month = driver.findElementByXPath("//ul[@id=\"documentList\"]/li[position() > 1]/a//div[contains(@class,\"docTitle\")][text()=\"${monthString}\"]")
                   month.click()
                   sleep(2000)
                   getDocumentsByPage2(driver)
                    WebElement back_btn = driver.findElementByXPath("//ul[@id=\"documentList\"]/li[contains(@class,\"openfolderback\")]")
                   back_btn.click()
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
                }
            }
        } //end documentsByPage1()

    //individual days
   public void getDocumentsByPage2(WebDriver driver) throws Exception{
       List<WebElement> days = driver.findElementsByXPath("//ul[@id=\"documentList\"]/li[@class = \"folder\"]//a//div[contains(@class,\"docTitle\")]")

       int index = 0;
       List<String> daysString = []
       for (WebElement day : days) {
           daysString[index] = day.getText()
           index++
       }

       for (String dayString : daysString) {
           try {
               log.debug("dayString:  " + dayString)
               WebElement day = driver.findElementByXPath("//li//a//div[contains(@class,\"docTitle\")][text()=\"${dayString}\"]")
               day.click()
               sleep(2000)
               getDocumentsByPage3(driver)
               WebElement back_btn = driver.findElementByXPath("//ul[@id=\"documentList\"]/li[contains(@class,\"openfolderback\")]")
               back_btn.click()
           }
           catch (Exception e) {
               log.debug(e.message)
               e.printStackTrace(System.out)  //skip bad lines but print errors
           }
       }
   } //end ..2()

    //grab agenda and packets for single day
    public void getDocumentsByPage3(WebDriver driver) throws Exception{
        DocumentWrapper doc = new DocumentWrapper()
        try {
            //grab document stuff
            doc.title = driver.findElementByXPath("//ul[@id=\"documentList\"]/li[position() > 1]/a[contains(.,\"Agenda\") and not(contains(.,\"Packet\"))]").getText() + ' ' + (current_year - 1).toString()
            log.info("\tTitle: ${doc.title}")
            doc.dateStr = driver.findElementByXPath("//ul[@id=\"documentList\"]/li[position() > 1]/a[contains(.,\"Agenda\") and not(contains(.,\"Packet\"))]").getText() + ' ' + (current_year - 1).toString()
            log.info("\tDate: ${doc.dateStr}")
            doc.link = driver.findElementByXPath("//ul[@id=\"documentList\"]/li[position() > 1]/a[contains(.,\"Agenda\") and not(contains(.,\"Packet\"))]").getAttribute('href')
            log.info("\tLink: ${doc.link}")
            //parse link and remake
            docList = docList + doc
        }
        catch (Exception e) {
            log.debug(e.message)
            e.printStackTrace(System.out)  //skip bad lines but print errors
        }
    } //end ..3()

} // end class

