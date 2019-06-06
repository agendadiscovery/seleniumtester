package com.agendadiscovery.tests
import com.agendadiscovery.Tester
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.lang.reflect.Method
import java.util.concurrent.TimeUnit

class login_001 extends BaseTester {
    boolean result = true
    String usernameStr = "administrator@agendadiscovery.com"
    String passwordStr = "kireland"
    By usernameBY = By.xpath("//input[@id=\"username\"]")
    By passwordBY = By.xpath("//input[@id=\"password\"]")
    By singinBY = By.xpath("//button[contains(translate(.,\"SIGN IN\",\"sign in\"),\"sign in\")]")
    By logoutBY = By.xpath("//a[contains(translate(.,\"LOGOUT\",\"logout\"),\"logout\")]")
    By navbarBY = By.xpath("//div[@id=\"navbar\"]/ul[last()]")

    public void test(){
        Tester.log.info('starting test')

        try{
            //driver.manage().timeouts().pageLoadTimeout(5, TimeUnit.SECONDS)  //pageLoadTimeout yields un-grabable page
            driver.get('https://stage.agendadiscovery.com')

            WebElement usernameWE = driver.findElement(usernameBY)
            WebElement passwordWE = driver.findElement(passwordBY)
            WebElement signinWE = driver.findElement(singinBY)

            usernameWE.sendKeys(usernameStr)
            passwordWE.sendKeys(passwordStr)

            signinWE.click()
            //Tester.log.info("intentional timeout.  don't worry")

            WebElement navbarWE = driver.findElement(navbarBY)
            navbarWE.click()
            sleep(1000)
            WebElement logoutWE = driver.findElement(logoutBY)
            logoutWE.click()
            sleep(1000)

            signinWE = driver.findElement(singinBY)  //check that we are back at login
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