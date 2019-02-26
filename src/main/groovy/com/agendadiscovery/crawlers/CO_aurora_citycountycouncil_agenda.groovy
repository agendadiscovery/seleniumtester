package com.agendadiscovery.crawlers

import com.agendadiscovery.DocumentWrapper
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.ExpectedConditions
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.time.Year
import java.util.concurrent.TimeUnit

public class CO_aurora_citycountycouncil_agenda extends BaseCrawler{
    private static final Logger log = LoggerFactory.getLogger(this.class)
    int current_year = Year.now().getValue()
    List <WebElement> docList = []

    int paginateIndex = 1
    boolean hasNext = true

    // http://chromedriver.chromium.org/getting-started
    public List getDocuments(String baseUrl ) throws Exception {
        log.info("Starting AZ Youngtown Selenium crawl")
        log.info("Requesting baseURL: "+baseUrl)
        driver.manage().timeouts().implicitlyWait(10,TimeUnit.SECONDS)

        try {
            driver.get(baseUrl)

            // Wait for the first PDF icon to show (ajax to finish)
            By firstPdfIcon = By.xpath("//li[2]/a[1]/div/div[2]/div/em[contains(@class,\"fa-file-pdf-o\")]")
            wait.until(ExpectedConditions.presenceOfElementLocated(firstPdfIcon))

            //Grab paginate buttons
            String paginatorsPath = "//div[@class=\"PO-paging\"]//li//a[contains(@class,\"pageButton number\")]"
            List <WebElement> paginators = driver.findElementsByXPath(paginatorsPath)
            List <String> paginatorsText = []
            int index = 0;
            for(WebElement paginator : paginators){
                if(paginator.getText().matches("^[0-9]+"))  //remove blank buttons
                    {paginatorsText[index] = paginator.getText()}
                //log.debug(paginatorsText[index])
                index++
            }
            //cycle click through paginated display
            while(hasNext){
                By rowSelector = By.xpath("//ul[@id=\"documentList\"]/li[position()>1]")
                List<WebElement> rowElements = driver.findElements(rowSelector)
                //cycle rows in display
                for(int i=0; i<rowElements.size(); i++){
                    WebElement row = rowElements.get(i)
                    DocumentWrapper doc = new DocumentWrapper()

                    // Xpath Selectors
                    By titleBy = By.xpath("./a/div/div/div[contains(@class,\"docTitle\")]")
                    By dateBy = titleBy // same in this case
                    By urlBy = By.xpath("./a")

                    // Get data
                    doc.title = row.findElement(titleBy).getText()
                    doc.dateStr = row.findElement(dateBy).getText()
                    doc.link = row.findElement(urlBy)?.getAttribute("href")

//                    log.debug("\tTitle: ${doc.title}")
//                    log.debug("\tDate: ${doc.dateStr}")
//                    log.debug("\tUrl: ${doc.link}")

                    docList.add(doc)
                }
                // click paginated button (1..2..3)
                if(paginateIndex < paginatorsText.size()){
                    WebElement paginator = driver.findElement(By.linkText(paginatorsText[paginateIndex]))
                    paginator.click()
                    //log.info("click")
                    sleep(5000)
                }else{ // no more arrow
                    hasNext = false
                    // log.info("no click")
                }
                paginateIndex++
                // log.debug(paginateIndex.toString())
            }
        } catch (Exception e) {
            log.info("Selenium crawl error: "+e)
            e.printStackTrace(System.out)
        } finally{
            driver.quit()
            return docList
        }
    }

}
