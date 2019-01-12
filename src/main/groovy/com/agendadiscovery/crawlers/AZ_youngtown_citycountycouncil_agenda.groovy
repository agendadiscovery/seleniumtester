package com.agendadiscovery.crawlers

import org.openqa.selenium.By
import org.openqa.selenium.NoSuchElementException
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.FluentWait

import java.util.concurrent.TimeUnit
import com.agendadiscovery.DocumentWrapper

public class AZ_youngtown_citycountycouncil_agenda extends BaseCrawler {

    // http://chromedriver.chromium.org/getting-started
    public List getDocuments(String baseUrl) throws Exception {
        WebDriver driver = new ChromeDriver()
        List docList = []
        driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS)
        driver.get(baseUrl)

        try {
            FluentWait<WebDriver> wait = new FluentWait<WebDriver>(driver).ignoring(NoSuchElementException.class);

            // Wait for years to be displayed
            By yearLink = By.xpath("/html/body/form/div[4]/div/div[2]/div[5]/div/div[1]/div[2]/div[2]/div/div/div[2]/div[2]/div[5]/ul/li/a[div/div/div[text()=2018]]")
            wait.until(ExpectedConditions.presenceOfElementLocated(yearLink))

            // Click the 2018 row
            driver.findElement(yearLink).click()

            // Wait for the first PDF icon to show (ajax to finish)
            By firstPdfIcon = By.xpath("//li[2]/a[1]/div/div[2]/div/em[contains(@class,\"fa-file-pdf-o\")]")
            wait.until(ExpectedConditions.presenceOfElementLocated(firstPdfIcon))

            // Iterate through the pages
            driver.findElementsByXPath("//div[@class=\"PO-paging\"]/ul[@class=\"PO-pageButton\"]/li/a[contains(@class,\"number\")]").each{ WebElement we ->
                we.click()
                driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS)
                // Parse the links
                docList = docList + getDocumentsByPage(driver)
            }
        } catch (Exception e) {
            throw e
        } finally{
            driver.quit()
            return docList
        }
    }

    public List getDocumentsByPage(WebDriver driver) throws Exception{
        // Grab the element rows
        By rowSelector = By.xpath("//ul[@id=\"documentList\"]/li[position()>1]")
        List<WebElement> webElements = driver.findElements(rowSelector)
        List docList = []

        for(int i=0; i < webElements.size(); i++){
            DocumentWrapper doc = new DocumentWrapper()
            // Row element data will be relative to
            WebElement webElement = webElements.get(i)

            // Xpath Selectors
            By titleBy = By.xpath("./a/div/div/div[contains(@class,\"docTitle\")]")
            By dateBy = titleBy // same in this case
            By urlBy = By.xpath("./a")

            // Get data
            String title = webElement.findElement(titleBy).getText()
            String dateStr = webElement.findElement(dateBy).getText()?.replaceAll("-"," ") // clean date a bit
            String url = webElement.findElement(urlBy).getAttribute("href")

            // Save to the document obj
            doc.title = title
            doc.dateStr = dateStr
            doc.link = url

            docList.add(doc)
        }

        return docList
    }

}
