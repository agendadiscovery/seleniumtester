package com.agendadiscovery.crawlers

import com.agendadiscovery.DocumentWrapper
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.ExpectedConditions
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.openqa.selenium.interactions.Actions
import java.time.Year
import java.util.concurrent.TimeUnit

//http://cityofbonitasprings.org/cms/one.aspx?pageId=13788499
class FL_bonitasprings_citycouncil_agenda extends BaseCrawler {
    private static final Logger log = LoggerFactory.getLogger(this.class)
    int current_year = Year.now().getValue()
    List <WebElement> docList = []

    int paginateIndex = 0
    boolean hasNext = true



    // http://chromedriver.chromium.org/getting-started
    public List getDocuments(String baseUrl ) throws Exception {
        log.info("Starting crawler " + this.class.name)
        log.info("Requesting baseURL: " + baseUrl)
        driver.manage().timeouts().implicitlyWait(2,TimeUnit.SECONDS)

        try {
            driver.get(baseUrl)
            // Wait for the first PDF icon to show (ajax to finish)
            By firstPdfIcon = By.xpath("//li[@class=\"folder\"][contains(.,\"City Council\")][1]")
            wait.until(ExpectedConditions.presenceOfElementLocated(firstPdfIcon))
            sleep(6000)
            driver.manage().timeouts().implicitlyWait(2,TimeUnit.SECONDS)
            //click City Council
            WebElement cityCouncil = driver.findElementByXPath("//li[@class=\"folder\"][contains(.,\"City Council\")][1]")
            cityCouncil.click()

            //click 2019
            WebElement year2019 = driver.findElementByXPath("//li[@class=\"folder\"][contains(.,\"2019\")][1]")
            year2019.click()
            sleep(1000)
            //grab pagination links
            String paginatorsPath = "//div[@class=\"PO-paging\"]//li//a[contains(@class,\"pageButton number\")]"
            List<WebElement> paginators = driver.findElementsByXPath(paginatorsPath)
            List<String> paginatorsStr = []
            int index = 0;
            for (WebElement paginator : paginators) {
                if (paginator.getText().matches("^[0-9]+"))  //remove blank buttons
                {
                    paginatorsStr[index] = paginator.getText()
                }
            }
            //cycle pagination
            while (hasNext) {
                List<WebElement> days = driver.findElementsByXPath("//ul/li[@class=\"folder\"]//div[@class=\"titleUser\"]") //ul/li[@class="folder"]//div[@class="titleUser"]
                List<String> daysStr = []
                int i = 0;
                for (WebElement day : days) {
                    daysStr[i] = day.getText()
                    i++
                }
                //ul[@id=\"documentList\"]//li//div[@class=\"titleUser\"][1]/ancestor::li
                //cycle each day
                for (String dayStr : daysStr) {
                    DocumentWrapper doc = new DocumentWrapper()

                    WebElement day = driver.findElementByXPath("//ul[@id=\"documentList\"]//li//div[@class=\"titleUser\"][contains(.,\"${dayStr}\")]/ancestor::li")
//
//                    Actions actions = new Actions(driver);
//                    actions.moveToElement(day);
//                    actions.perform();
//                    sleep(1000)

                    day.click()
                    sleep(1000)

                    //grab Agenda
                    WebElement agenda = driver.findElementByXPath("//ul[@id=\"documentList\"]//li//a[contains(.,\"Agenda\")]")

                    doc.title = agenda.getText()
                    doc.dateStr = agenda.getText()
                    doc.link = agenda.getAttribute("href")

                    log.debug("\tTitle: ${doc.title}")
                    log.debug("\tDate: ${doc.dateStr}")
                    log.debug("\tUrl: ${doc.link}")
                    docList = docList + doc

                    //go back
                    WebElement back = driver.findElementByXPath("//li[contains(@class,\"openfolderback\")]")
                    back.click()
                    sleep(1000)
                }//end cycle days

                // click paginated button (1..2..3)
                if (paginateIndex < paginatorsStr.size()) {
                    WebElement paginator = driver.findElement(By.linkText(paginatorsStr[paginateIndex]))
                    paginator.click()
                    //log.info("click")
                    sleep(1500)
                } else { // no more arrow
                    hasNext = false
                    // log.info("no click")
                }
                paginateIndex++
                // log.debug(paginateIndex.toString())
            }// end cycle pages
        } catch (Exception e) {
            log.info("Selenium crawl error: "+e)
            e.printStackTrace(System.out)
        } finally{
            driver.quit()
            return docList
        }
    }

} //end class