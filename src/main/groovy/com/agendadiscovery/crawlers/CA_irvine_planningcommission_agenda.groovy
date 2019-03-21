package com.agendadiscovery.crawlers

import com.agendadiscovery.DocumentWrapper
import com.agendadiscovery.helpers.Popup
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.time.Year
import java.util.concurrent.TimeUnit


///////////////////////////////////////////////////////////////////////////
// Not yet working, needs repair.
// CA,irvine,planningcommission,agenda,http://legacy.cityofirvine.org/council/comms/planning/agenda.asp
///////////////////////////////////////// //////////////////////////////////

//uses Sire pub
class CA_irvine_planningcommission_agenda extends BaseCrawler {
    private static final Logger log = LoggerFactory.getLogger(this.class)
    int current_year = Year.now().getValue()
    List<WebElement> docList = []

    public List getDocuments(String baseUrl){
        log.info("Starting crawler " + this.class.name)
        log.info("Requesting baseURL: " + baseUrl)
        try {
            driver.manage().timeouts() implicitlyWait(5, TimeUnit.SECONDS)
            driver.get(baseUrl);
            sleep(5000)
            try {
                //grab links
                List <WebElement> rows = driver.findElementsByXPath("//a[contains(.,\"Agenda\")]") //debug
                //run each path
                getDocumentsByPage(driver,rows)
            }
            catch (Exception e) {
                log.debug(e.message)
                e.printStackTrace(System.out)  //skip bad lines but print errors
            }
        } catch (Exception e) {
            log.debug(e.message)
            e.printStackTrace(System.out)
            throw e  //throw for total failure
        } finally {
            driver.quit()
            return docList
        }
    } //getDocuments

    public void getDocumentsByPage(WebDriver driver, List<WebElement> rows){
        String base = driver.getWindowHandle()
        for (WebElement row : rows){
            try{
                String title,dateStr,link
                DocumentWrapper doc = new DocumentWrapper()
                WebElement agenda = row
                //grab title and date first.  link can change later
                title = row.getText()
                dateStr = row.getText()
                link = row.getAttribute('href')
                agenda.click()
                sleep(800)

                //check condition for SIRE (iframe)
                if(driver.getWindowHandles().size() == 2){
                    Popup.switchToPopup(base,driver)
                    //use iframe link
                    if(driver.findElementsByXPath("//iframe[@id=\"agviewmain\"]").size() == 1) {
                        link = driver.findElementByXPath("//iframe[@id=\"agviewmain\"]").getAttribute('src')
                        doc.link = "http://www.irvinequickrecords.com/sirepub/" + link
                        log.debug "We got an iframe"
                    }
                }
                else{
                    //keep original link (Granicus)
                    log.debug("Nope no iframe")
                }

                doc.title = title
                doc.dateStr = dateStr
                doc.link = link
                docList = docList + doc

                //debugger
                log.debug("\tTitle: ${doc.title}")
                log.debug("\tDate: ${doc.dateStr}")
                log.debug("\tUrl: ${doc.link}")
            }
            catch(Exception e){
                log.debug(e.message)
                e.printStackTrace(System.out)
            }
            finally{
                if(driver.getWindowHandles().size() == 2){
                    Popup.closeToBase(base,driver)
                }
            }
        } //end for loop
        //close new window
        driver.close()
        driver.switchTo().window(base)
    }//end getDocumentsByPage()
}
