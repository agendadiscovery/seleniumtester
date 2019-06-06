package com.agendadiscovery.crawlers

import com.agendadiscovery.DocumentWrapper
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.time.Year
import java.util.concurrent.TimeUnit

//https://go.boarddocs.com/ca/com/Board.nsf/Public
class CA_marysville_citycouncil_agenda extends BaseCrawler {
    private static final Logger log = LoggerFactory.getLogger(this.class)
    int current_year = Year.now().getValue()
    List<WebElement> docList = []
    String html ="";

    public List getDocuments(String baseUrl) throws Exception {
        log.info("Starting crawler " + this.class.name)
        log.info("Requesting baseURL: " + baseUrl)
        try {
            driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS)
            //driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS)
            try{driver.get(baseUrl)}
            catch(Exception e){log.debug("intentional timeout exception.  don't worry")}
            sleep(6000)

            //grab links
            try {
                //click meetings
                WebElement meetings = driver.findElementByXPath("//li[@id=\"li-meetings\"]")
                meetings.click()
                sleep(2000)

                //make list of tabs / cycle each
                List<WebElement> tabs = driver.findElementsByXPath("//h3[contains(@class,\"accordion-header\")][position() > 1][position() < 3]")
                for(WebElement tab:tabs){

                    //click tab
                    tab.click()
                    sleep(1000)  //debug

                    //make list of "unique" attribute for each agenda
                    List<WebElement> agendas= tab.findElementsByXPath("./following-sibling::div[contains(@class,\"wrap-year\")][position() < 2]/a")
                    List<String> unique_tags = []
                    for(WebElement agenda: agendas){
                        log.debug("agendas grabbed " + agendas.size().toString())
                        log.debug("Unique tag " + agenda.getAttribute("unique") )
                        unique_tags.add(agenda.getAttribute("unique"))
                    }

                    //cycle through agendas using unique tag
                    int index = 0
                    for(String unique: unique_tags) {
                        //if(index > 5){break}
                        getDocumentByPage(driver,unique, tab)
                        index++
                    }//for loop
                }
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
    } //end getDocuments

    public void getDocumentByPage(WebDriver driver, String unique, WebElement tab) throws Exception {
        try {
            DocumentWrapper doc = new DocumentWrapper()

            //click item
            WebElement agenda = tab.findElementByXPath("./following-sibling::div[contains(@class,\"wrap-year\")]//a[@unique=\"${unique}\"]")
            doc.title = agenda.getText().replace(/\n/, '')
            log.debug("\tTitle: ${doc.title}")
            doc.dateStr = doc.title
            log.debug("\tTitle: ${doc.dateStr}")
            agenda.click()

            //click View Agenda
            WebElement view_agenda = driver.findElementByXPath("//a[@id=\"btn-view-agenda\"]")
            view_agenda.click()
            sleep(1800)

            //list of "wrap-items"/divs  to cycle
            List<WebElement> sections = driver.findElementsByXPath("//div[@class=\"wrap-items\"]/div")

            //click and grab data (append to variable at HTML)
            for (WebElement section : sections) {
                section.click()
                sleep(500)
                WebElement section_html = driver.findElementByXPath("//div[@id=\"view-agenda-item\"]")
                html += section_html.getAttribute("innerHTML")
            }
            doc.text = html
            log.debug("\tHtml: ${doc.text}")
            html = ""
            docList = docList + doc
            sleep(1000)

            //go back to page with main list (click mieeintgs)
            WebElement meetings = driver.findElementByXPath("//li[@id=\"li-meetings\"]")
            meetings.click()
        }
        catch (Exception e){
            log.debug(e.message)
            e.printStackTrace(System.out)
        }
    } //end documentsByPage()
} // end class

