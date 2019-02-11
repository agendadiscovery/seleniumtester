package com.agendadiscovery.crawlers

import com.agendadiscovery.DocumentWrapper
import org.openqa.selenium.By
import org.openqa.selenium.NoSuchElementException
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.FluentWait

import java.util.concurrent.TimeUnit

public class CA_pleasanton_citycountycouncil_agenda extends BaseCrawler{

    // http://chromedriver.chromium.org/getting-started
    public List getDocuments(String baseUrl) throws Exception {
        ////////////////////////////////////////////
        // CURRENTLY BROKEN
        ////////////////////////////////////////////
        try {
            driver.get(baseUrl)

            // Wait for years to be displayed
            By yearLink = By.xpath("/html/body/form/div[4]/div/div[2]/div[5]/div/div[1]/div[2]/div[2]/div/div/div[2]/div[2]/div[5]/ul/li/a[div/div/div[text()=2018]]")
            wait.until(ExpectedConditions.presenceOfElementLocated(yearLink))

            // Click the 2018 row
            driver.findElement(yearLink).click()

            // Iterate through the pages
            driver.findElementsByXPath("//div[@id=\"TheRightPanel\"]//tr/td[1]/a").each{ WebElement folder ->
                folder.click()
                // grab date

                By linkList = By.xpath("//div[@id=\"TheRightPanel\"]//tr/td[1]/a[@class=\"DocumentBrowserNameLink\"]")

                driver.findElementsByXPath("//div[@id=\"TheRightPanel\"]//tr/td[1]/a").each { WebElement agendaItem ->
                    //*[@class="PageNumberToolbarCount"]
                    /// Blah stale object exception
                    driver.findElement(textBy).click()
                }


//                docList = docList + getDocumentsByPage(driver)
            }
        } catch (Exception e) {
            throw e
        } finally{
            driver.quit()
            return docList
        }
    }

    public List getDocumentsByPage(WebDriver driver) throws Exception{
        List docList = []

        //*[@id="ViewTextLink"]

        return docList
    }

}
