package com.agendadiscovery.crawlers

import com.agendadiscovery.DocumentWrapper
import com.agendadiscovery.helpers.Popup
import com.agendadiscovery.helpers.QuickMatch
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.time.Year
import java.util.concurrent.TimeUnit

////////////////////////////////////////////
// CURRENTLY BROKEN
////////////////////////////////////////////

class CA_bradbury_citycouncil_agenda extends BaseCrawler {
    private static final Logger log = LoggerFactory.getLogger(this.class)
    int current_year = Year.now().getValue()
    List <WebElement> docList = []

    //single pass through of p tags.  If item 'part*' follows then grab parts loop else single document

    public List getDocuments(String baseUrl){
        log.info("Starting crawler " + this.class.name)
        log.info("Requesting baseURL: " + baseUrl)
        try {
            driver.manage().timeouts() implicitlyWait(2, TimeUnit.SECONDS)
            driver.get(baseUrl);
            sleep(2000)
             //grab links
             List <WebElement> rows = driver.findElementsByXPath("//div[@class=\"item-page\"]/p")
                //run each path
             getDocumentsByPage(driver,rows)
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
        //cycle rows
        //for (WebElement row : rows){
        for(int i=0; i < rows.size(); i++){
            WebElement row = rows.get(i)
            try{
                String title, dateStr, link
                dateStr = row.getText()
                //skipping conditions
                if(QuickMatch.match("^\\s{3}",dateStr) || i == 0 || dateStr == null || dateStr ==" "){continue}

                //deals with parts
                if( row.findElementsByXPath("a").size == 0) {
                    //has parts
                    title = row.getText()
                    //grab all parts
                    List <WebElement> parts = row.findElementsByXPath("./following-sibling::ul[1]/li//a")
                    //cycle parts
                    for(WebElement part: parts){
                        DocumentWrapper doc = new DocumentWrapper()
                        doc.title = title + part.getText()
                        doc.dateStr = dateStr
                        doc.link = part.getAttribute("href")
                        docList = docList + doc

                        //debugger
                        log.debug("\tTitle: ${doc.title}")
                        log.debug("\tDate: ${doc.dateStr}")
                        log.debug("\tUrl: ${doc.link}")
                    }
                }
                else{
                    //single document
                    DocumentWrapper doc = new DocumentWrapper()
                    doc.title = row.findElement(By.xpath("./a[1]")).getText()
                    doc.dateStr = dateStr
                    doc.link = row.findElement(By.xpath("./a[1]")).getAttribute("href")
                    docList = docList + doc

                    //debugger
                    log.debug("\tTitle: ${doc.title}")
                    log.debug("\tDate: ${doc.dateStr}")
                    log.debug("\tUrl: ${doc.link}")
                }

            }
            catch(Exception e){
                log.debug(e.message)
                e.printStackTrace(System.out)
            }
            finally{
            }
        } //end for loop
    }//end getDocumentsByPage()
}//end class
