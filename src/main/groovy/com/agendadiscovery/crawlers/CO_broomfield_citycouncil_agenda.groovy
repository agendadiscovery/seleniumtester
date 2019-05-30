package com.agendadiscovery.crawlers

import com.agendadiscovery.DocumentWrapper
import com.agendadiscovery.helpers.Popup
import com.agendadiscovery.helpers.QuickMatch
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.openqa.selenium.JavascriptExecutor;

import java.time.Year
import java.util.concurrent.TimeUnit
import java.util.regex.Matcher

// UNFINISHED
// direct download url no longer works.  Google docs
//CO,broomfield,citycouncil,agenda,http://broomfield.granicus.com/ViewPublisher.php?view_id=6
class CO_broomfield_citycouncil_agenda extends BaseCrawler {
        private static final Logger log = LoggerFactory.getLogger(this.class)
        int current_year = Year.now().getValue()
        List<WebElement> docList = []

        public List getDocuments(String baseUrl) throws Exception {
            log.info("Starting crawler " + this.class.name)
            log.info("Requesting baseURL: " + baseUrl)
            try {
                driver.manage().timeouts().implicitlyWait(1000, TimeUnit.MILLISECONDS)
                driver.get(baseUrl);
                sleep(2000)
                //grab links
                getDocumentByPage(driver)
            } catch (Exception e) {
                log.debug(e.message)
                e.printStackTrace(System.out)
                throw e  //throw for total failure
            } finally {
                driver.quit()
                return docList
            }
        } //end getDocuments

        public void getDocumentByPage(WebDriver driver) throws Exception {
            WebElement header2019 = driver.findElementByXPath("//h3[contains(.,'2019')]")
            header2019.click()
            sleep(2000)
//            WebElement header2018 = driver.findElementByXPath("//h3[contains(.,'2018')]")
//            header2018.click()
//            sleep(2000)
            List<WebElement> rows = driver.findElementsByXPath("//h3[contains(.,'2019') or contains(.,'2018') or contains(.,'2020')]/following-sibling::div[1]/table//tr")
            Integer index = 0
            for (WebElement row : rows) {
                DocumentWrapper doc = new DocumentWrapper()
                try {
                    //grab document stuff
                    By.ByXPath byTitle = By.xpath("./td[1]")
                    doc.title = row.findElement(byTitle).getText()
                    log.info("\tTitle: ${doc.title}")
                    By.ByXPath byDate = By.xpath("./td[2]")
                    doc.dateStr = row.findElement(byDate).getText()
                    log.info("\tDate: ${doc.dateStr}")
                    By.ByXPath byLink = By.xpath("./td[4]//a")
                    String google_drive_link = row.findElement(byLink).getAttribute('href')
                    log.info("\traw Url: ${google_drive_link}")
                    String base = driver.getWindowHandle()
                    JavascriptExecutor js=(JavascriptExecutor) driver
                    js.executeScript("window.open()");
                    sleep(1000)
                    Popup.switchToPopup(base,driver)
                    driver.get(google_drive_link)
                    sleep(2000)
                    google_drive_link = driver.getCurrentUrl()
                    Popup.closeToBase(base,driver)
                    //parse link and remake
                    Matcher m = QuickMatch.matchGroups(".+d/([^/]+)", google_drive_link)
                    if (m != null) {
                        String id = m.group(1)
                        //dl link example -> https://drive.google.com/uc?export=download&id=1aAivoeNydSf761Hmih7lHWH7rLXkhuzV
                        doc.link = "https://drive.google.com/uc?export=download&id=" + id
                    } else {
                        doc.link = "no match, skipping"
                        continue
                    }
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
                    if (doc.title == "") {
                        break
                    }
                    docList = docList + doc
                    //debug
//                log.info("\tTitle: ${doc.title}")
//                log.info("\tDate: ${doc.dateStr}")
//                log.info("\tUrl: ${doc.link}")
                }
            }
        } //end documentsByPage()
    } // end class
