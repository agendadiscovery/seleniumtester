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

//alternating two and one tier. SIRE & Granicus
//plan:  1) click link  2) switch to new window 3)inspect HTML 4) keep original URL if not iframe
//              5) if Iframe then change url
class NV_clarkcounty_countycouncil_agenda extends BaseCrawler{
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
                List <WebElement> rows = driver.findElementsByXPath("//tr[position()<20][td[contains(@class,'listItem')]]") //debug
                //run each path
                getDocumentsByPage(driver,rows)
                scanGranicus(driver,rows)
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
       //if HTML has iframe continue else next
        String base = driver.getWindowHandle()
        for (WebElement row : rows){
            try{
                String title,dateStr,link
                DocumentWrapper doc = new DocumentWrapper()
                WebElement agenda = row.findElement(By.xpath("td[3 or 4]//a[contains(., \"Agenda\")]"))
                //grab title and date first.  link can change later
                title = row.findElement(By.xpath("td[1]")).getText()
                dateStr = row.findElement(By.xpath("td[2]")).getText()
                link = row.findElement(By.xpath("td[3 or 4]//a[contains(., \"Agenda\")]")).getAttribute('href')
                agenda.click()
                sleep(800)

                //check condition for SIRE (iframe)
                if(driver.getWindowHandles().size() == 2){
                    Popup.switchToPopup(base,driver)
                    //use iframe link
                    if(driver.findElementsByXPath("//iframe[@id=\"agviewmain\"]").size() == 1) {
                        link = driver.findElementByXPath("//iframe[@id=\"agviewmain\"]").getAttribute('src')
                        doc.link = "https://agenda.co.clark.nv.us/" + link
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
       //switch back to homepage
        driver.switchTo().window(base)
    }//end getDocumentsByPage()
}
