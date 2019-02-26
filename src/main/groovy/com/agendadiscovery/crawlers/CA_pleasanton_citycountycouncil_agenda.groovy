package com.agendadiscovery.crawlers

import com.agendadiscovery.DocumentWrapper
import com.agendadiscovery.helpers.QuickMatch
import org.openqa.selenium.By
import org.openqa.selenium.NoSuchElementException
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.FluentWait
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.time.Year
import java.util.concurrent.TimeUnit

public class CA_pleasanton_citycountycouncil_agenda extends BaseCrawler{
    private static final Logger log = LoggerFactory.getLogger(this.class)
    int current_year = Year.now().getValue()
    List <WebElement> docList = []

    // http://chromedriver.chromium.org/getting-started
    public List getDocuments(String baseUrl) throws Exception {
        //QUESTION ABOUT AGENDA.  CAN WE ADD NEW ATTRIBUTE TO DOCUMENTWRAPPER OR OTHER OPTION?
        log.info("Starting crawler " + this.class.name)
        log.info("Requesting baseURL: "+baseUrl)
        try {
            driver.manage().timeouts()implicitlyWait(5, TimeUnit.SECONDS)
            driver.get(baseUrl);
            // Wait for years to be displayed
            By yearLinkBy = By.xpath("//nobr[span[contains(.,'2018')]]")
            wait.until(ExpectedConditions.presenceOfElementLocated(yearLinkBy))

            // Click the 2018 row
            driver.findElement(yearLinkBy).click()
            sleep(2000)

            //get folder web elements
            List <WebElement> folders = driver.findElementsByXPath("//div[@id=\"TheRightPanel\"]//tbody//tr/td[1]/a")
            //get text from each for loop (cannot loop with web elements after DOM refresh)
            List folderNames = []
            for(WebElement folder:folders){
                folderNames.add(folder.getText())
                //debug  System.out.println(folder.getText()) //debug
            }
            //Iterate through the dated folders
            folderNames.each{ String folderName ->
                //grab link and refresh element (new DOM each time)
                  //debug System.out.println(folderName)
                By folderBy = By.xpath("//a[contains(. ,'" + folderName + "')]")
                WebElement folder = driver.findElement(folderBy)
                folder.click()
                sleep(1000)

                grabRow(driver)

                driver.navigate().back()
            }
        } catch (Exception e) {
            log.debug(e.message)
            e.printStackTrace(System.out)
            log.debug("Continuing with next link")
        } finally{
            driver.quit()
            return docList
        }
    }

    //not used
    public void grabRow(WebDriver driver){
        try {
            DocumentWrapper doc = new DocumentWrapper();
            doc.dateStr = driver.findElementByXPath("//div[@class=\"FolderNameHeader\"]").getText()
            doc.title = driver.findElementByXPath("//div[@title=\"Path\"]/following-sibling::div[@class=\"FolderDataValue\"][1]").getText()
            doc.title = doc.title.split('[\\\\]')[2]
            doc.link = driver.findElementByXPath("//div[@id=\"TheRightPanel\"]//tr/td[a]/*[contains(.,\"AGENDA\")]").getAttribute("href")
            docList = docList + doc
//
//            log.info("\tTitle: ${doc.title}")
//            log.info("\tDate: ${doc.dateStr}")
//            log.info("\tUrl: ${doc.link}")

        }
        catch (Exception e) {
            log.debug(e.message)
            e.printStackTrace(System.out)
            //continue crawling the rest
        }
    }
}
