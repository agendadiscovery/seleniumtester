package com.agendadiscovery.tests

import com.agendadiscovery.Tester
import org.openqa.selenium.By
import org.openqa.selenium.WebElement

// https://stage.agendadiscovery.com/site/list
class site_list extends BaseTester{
    boolean result = true
    String usernameStr = "administrator@agendadiscovery.com"
    String passwordStr = "kireland"
    String baseUrl = "https://stage.agendadiscovery.com/site/list"

    By usernameBY = By.xpath("//input[@id=\"username\"]")
    By passwordBY = By.xpath("//input[@id=\"password\"]")
    By singinBY = By.xpath("//button[contains(translate(.,\"SIGN IN\",\"sign in\"),\"sign in\")]")
    By logoutBY = By.xpath("//a[contains(translate(.,\"LOGOUT\",\"logout\"),\"logout\")]")
    By navbarBY = By.xpath("//div[@id=\"navbar\"]/ul[last()]")

    public void test(){
        Tester.log.info('starting test')

        try{
            //driver.manage().timeouts().pageLoadTimeout(5, TimeUnit.SECONDS)  //pageLoadTimeout yields un-grabable page
            driver.get(baseUrl)

            WebElement usernameWE = driver.findElement(usernameBY)
            WebElement passwordWE = driver.findElement(passwordBY)
            WebElement signinWE = driver.findElement(singinBY)

            usernameWE.sendKeys(usernameStr)
            passwordWE.sendKeys(passwordStr)

            signinWE.click()
            sleep(1000)


        }
        catch (Exception e) {
            Tester.log.info(e.message)
            e.printStackTrace(System.out)
            result = false
        }
        finally{
            String resultStr =  (result)? 'pass' : 'FAIL'
            String resultMsg = 'Results for ' + this.class.toString().split("\\.").last() + ": " + resultStr
            Tester.log.info(resultMsg)
            driver.quit()
            //return result
        }
    }
}
