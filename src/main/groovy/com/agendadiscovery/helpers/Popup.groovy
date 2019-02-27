package com.agendadiscovery.helpers
import org.openqa.selenium.WebDriver

class Popup {
    //pre method invoke  String base = driver.getWindowHandle()

    //this methods assumes one window and one popup
    public static String switchToPopup(String base, WebDriver driver){
        Set <String> set = driver.getWindowHandles()
        set.remove(base)
        assert set.size()==1
        List<String> handles = new ArrayList<String>(set)
        driver.switchTo().window(handles[0])
        return handles[0]
    }


}
