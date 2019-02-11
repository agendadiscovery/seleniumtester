package com.agendadiscovery.crawlers

import com.agendadiscovery.DocumentWrapper
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.ExpectedConditions

public class KS_lyons_citycountycouncil_agenda extends BaseCrawler{

    // http://chromedriver.chromium.org/getting-started
    public List getDocuments(String baseUrl) throws Exception {
        log.debug("Starting ${this.class.name} Selenium crawl")
        log.debug("Requesting baseURL: "+baseUrl)

        try {
            driver.get(baseUrl)

            // Grab the parent folder name which should be 4 digit year
            String year = driver.findElement(By.xpath("//*[@id=\"AjaxTreeView\"]/ul/li/div/span[2]"))?.getText()

            // Iterate through folder years folders
            By rowSelector = By.xpath("//*[@id=\"AjaxTreeView\"]/ul/li/ul/li")
            wait.until(ExpectedConditions.presenceOfElementLocated(rowSelector))

            List<WebElement> rowElements = driver.findElements(rowSelector)

            for(int i=0; i<rowElements.size(); i++){
                log.debug("Processing folder: "+i)
                WebElement row = rowElements.get(i)
                String dateStr = row.getText()

                // Date format is all over but append the year if its tiny
                if(dateStr.size() < 8){
                    dateStr = dateStr+year
                }

                // Click on the folder to load pdfs
                row.click()
                sleep(5000)

                // Iterate through the folders PDFs
                By agendaRowSelector = By.xpath("//*[@id=\"Grid\"]/table/tbody/tr/td[2]/a")
//                wait.until(ExpectedConditions.presenceOfElementLocated(agendaRowSelector))

                List<WebElement> agendaRowElements = driver.findElements(agendaRowSelector)

                for(int j=0; j<agendaRowElements.size(); j++){
                    log.debug("Processing agenda: "+j)
                    WebElement agenda = agendaRowElements.get(j)
                    String title = agenda?.getText()
                    String link = agenda?.getAttribute("href")

                    if(title?.toLowerCase().contains("council") || title?.toLowerCase().contains("agenda") || title?.toLowerCase().contains("packet")){
                        DocumentWrapper dw = new DocumentWrapper()

                        dw.title = title
                        dw.dateStr = dateStr
                        dw.link = link

                        log.debug("\tTitle: ${dw.title}")
                        log.debug("\tDate: ${dw.dateStr}")
                        log.debug("\tUrl: ${dw.link}")

                        docList.add(dw)
                    }
                }
                // Cheap way to get rid of stale element and refresh original folders list
                rowElements = driver.findElements(rowSelector)
                sleep(5000)
            }
        } catch (Exception e) {
            log.error("Selenium crawl error: "+e)
            throw e
        } finally{
            driver.quit()
            return docList
        }
    }

}
