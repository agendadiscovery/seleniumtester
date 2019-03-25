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
//Java applet.  Raw html grab
class KS_mcpherson_citycouncil_agenda extends BaseCrawler {
    private static final Logger log = LoggerFactory.getLogger(this.class)
    int current_year = Year.now().getValue()
    List<WebElement> docList = []
    String html ="";

    // https://www.boarddocs.com/ks/comks/Board.nsf/Public
    // meetings tab  //li[@id="li-meetings"]
    // 2019 agendas  //div[contains(@class,"wrap-year")][position() < 2]/a/@unique
    // view agenda button  //a[@id="btn-view-agenda"]
    // sections of single agenda   //div[@class="wrap-items"]/div
    public List getDocuments(String baseUrl) throws Exception {
        log.info("Starting crawler " + this.class.name)
        log.info("Requesting baseURL: " + baseUrl)
        try {
            driver.manage().timeouts() implicitlyWait(10, TimeUnit.SECONDS)
            driver.get(baseUrl);
            sleep(3000)

            //grab links
            try {
                //click meetings
                WebElement meetings = driver.findElementByXPath("//li[@id=\"li-meetings\"]")
                meetings.click()
                sleep(2000)
                //make list of "unique" attribute for each agenda
                 List<WebElement> agendas= driver.findElementsByXPath("//div[contains(@class,\"wrap-year\")][position() < 2]/a")
                 List<String> unique_tags = []
                 for(WebElement agenda: agendas){
                    unique_tags.add(agenda.getAttribute("unique"))
                 }
                //cycle through agendas using unique tag
                int index = 0
                for(String unique: unique_tags) {
                    if(index > 5){break}
                    getDocumentByPage(driver,unique)
                    index++
                }//for loop
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

    public void getDocumentByPage(WebDriver driver, String unique) throws Exception {
        try {
            DocumentWrapper doc = new DocumentWrapper()
            //click one
            WebElement agenda = driver.findElementByXPath("//a[@unique=\"${unique}\"]")
            doc.title = agenda.getText().replace(/\n/, '')
            log.info("\tTitle: ${doc.title}")
            doc.dateStr = doc.title
            agenda.click()
            //click view agenda
            WebElement view_agenda = driver.findElementByXPath("//a[@id=\"btn-view-agenda\"]")
            view_agenda.click()
            sleep(1000)
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
            log.info("\tHtml: ${doc.text}")
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
