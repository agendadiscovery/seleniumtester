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

//optional two or one tier
//fastest less elegant option: do two crawls, discarding bad request
//plan so far:  1) click link  2) switch to new window 3)inspect HTML 4)skip if wrong type
//              5) Grab info if good 6) repeat for other file type (second pass through)
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
            sleep(2000)
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
                WebElement agenda = row.findElementByXPath("//a[contains(., \"Agenda\")]")
                title = row.findElement(By.xpath("td[1]")).getText()
                dateStr = row.findElement(By.xpath("td[2]")).getText()
                link = agenda.getAttribute('href')
                agenda.click()
                sleep(2000)
                //get title and date first

                log.debug(title)
                log.debug(dateStr)

                //check condition for SIRE (iframe)
                if(driver.getWindowHandles().size() == 2){
                    Popup.switchToPopup(base,driver)
                    //get iframe link
                    link = driver.findElementByXPath("//iframe[@id=\"agviewmain\"]").getAttribute('src')
                    doc.link = "https://agenda.co.clark.nv.us/" + link
                    log.debug "We got an iframe"
                }
                else{
                    //Granicus keep original link
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
//SIRE & Granicus
    public void scanGranicus(WebDriver driver, List<WebElement> rows){
       //if HTML has iframe stop
    }
}
