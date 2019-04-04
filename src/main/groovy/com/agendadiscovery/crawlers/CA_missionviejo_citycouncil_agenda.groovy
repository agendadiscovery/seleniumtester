
// http://missionviejo.granicus.com/ViewPublisher.php?view_id=12

//dropdown ->  //div[@id="city${current_year - 2000}"]
// rows  //h3[text()="City Council Meetings"]/following-sibling::div[position() < 3]//table//tbody//tr
// title -> .//td[1]
// date ->  .//td[2]
// link1 -> ./td//a[text() = "Agenda"]
// link2 -> //a[contains(.,"Agenda Packet")]

//note this had issue with cycling rows.  Therefore will grab each web element up from for lists, not cycle rows.

package com.agendadiscovery.crawlers

import com.agendadiscovery.DocumentWrapper
import com.agendadiscovery.helpers.QuickMatch
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.time.Year
import java.util.concurrent.TimeUnit
import java.util.regex.Matcher

class CA_missionviejo_citycouncil_agenda extends BaseCrawler {
    private static final Logger log = LoggerFactory.getLogger(this.class)
    int current_year = Year.now().getValue()
    List<WebElement> docList = []

    List<WebElement> titlesWebEl = []
    List<WebElement> datesWebEl = []
    List<WebElement> linksWebEl = []

    List<String> titles = []
    List<String> dates = []
    List<String> links = []
    //By rowsBy =  new By.ByXPath("//h3[text()=\"City Council Meetings\"]/following-sibling::div[position() < 3]//table//tbody//tr")
    By titleBy = new By.ByXPath("//h3[text()=\"City Council Meetings\"]/following-sibling::div[position() < 3]//table//tbody//tr//td[1]")  //.//td[1]
    By dateBy =  new By.ByXPath("//h3[text()=\"City Council Meetings\"]/following-sibling::div[position() < 3]//table//tbody//tr//td[2]") // .//td[2]
    By link1By = new By.ByXPath("//h3[text()=\"City Council Meetings\"]/following-sibling::div[position() < 3]//table//tbody//tr/td[4]") // ./td//a[text() = "Agenda"]
    By link2By = new By.ByXPath("//a[contains(.,\"Agenda Packet\")]")    //a[contains(.,"Agenda Packet")]

    public List getDocuments(String baseUrl) throws Exception {
        log.info("Starting crawler " + this.class.name)
        log.info("Requesting baseURL: " + baseUrl)
        try {
            driver.manage().timeouts() implicitlyWait(1000, TimeUnit.MILLISECONDS)
            driver.get(baseUrl);
            sleep(1000)
            //click dropdown
            WebElement dropdown = driver.findElementByXPath("//div[@id=\"city${current_year - 2000}\"]")
            dropdown.click()
            sleep(1000)
            dropdown = driver.findElementByXPath("//div[@id=\"city${current_year - 2001}\"]")
            dropdown.click()
            sleep(2000)

            //dropdown ->  //div[@id="city${current_year - 2000}"]
            titlesWebEl = driver.findElements(titleBy)
            datesWebEl =   driver.findElements(dateBy)
            linksWebEl =  driver.findElements(link1By)
            //make a list of agenda urls, and of dates and titles
            populateLists(driver)
            getSecondLink(driver)
            makeDocs()

        } catch (Exception e) {
            log.debug(e.message)
            e.printStackTrace(System.out)
            throw e  //throw for total failure
        } finally {
            driver.quit()
            return docList
        }
    } //end getDocuments

    public void populateLists(WebDriver driver){
        //make a list of agenda urls, and of dates and titles
        //List<WebElement> rows = driver.findElementsByXPath("//h3[text()=\"City Council Meetings\"]/following-sibling::div[position() < 3]//table//tbody//tr")
        int index = 0
        for(WebElement titleWebEl:titlesWebEl){
            try {
                titles[index] = titlesWebEl[index].getText()
                dates[index] = datesWebEl[index].getText()
                links[index] = linksWebEl[index].findElementByXPath(".//a[text()=\"Agenda\"]").getAttribute('href')
            }
            catch (Exception e) {
                log.debug(e.message)
                e.printStackTrace(System.out)
                links[index] = null
            }
            finally {
                //if(index >= 5){break;} //debug remove
                index++
            }
        }
    } //end populateLists()

    public void getSecondLink(WebDriver driver){
        int index = 0;
        for (String link:links){
            try {
                driver.get(link)
                sleep(1500)
                if(driver.getCurrentUrl() == "http://dms.cityofmissionviejo.org/OnBaseAgendaOnline") {
                    links[index] = null
                }
                else{
                    links[index] = driver.findElement(link2By).getAttribute('href')
                }
                //log.debug('link2 ' + links[index])
            }
            catch (Exception e) {
                log.debug(e.message)
                e.printStackTrace(System.out)
                links[index] = null
            }
            finally {
                index++
            }
        }
    }

    public void makeDocs(){
        int index = 0;
        for (String link:links){
            DocumentWrapper doc = new DocumentWrapper()
            doc.title = titles[index]
            doc.dateStr = dates[index]
            doc.link = links[index]
            index++

            log.info("\tTitle: ${doc.title}")
            log.info("\tDate: ${doc.dateStr}")
            log.info("\tUrl: ${doc.link}")

            docList = docList + doc
        }
    }
} // end class
