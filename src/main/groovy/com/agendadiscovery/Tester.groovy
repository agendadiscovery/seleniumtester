package com.agendadiscovery

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.lang.reflect.Method

class Tester {
    public String test = ""
    public static final Logger log = LoggerFactory.getLogger(Tester.class)
    //private static final File csv = new File(getClass().getResource("/csv/crawlCouncil.csv").getPath())
    //debug using test csv
    private static final File csv = new File(getClass().getResource("/csv/test_single.csv").getPath())
    private static final String driverPath = getClass().getResource("/drivers/chromedriver.exe").getPath()


    public static void main(String[] args) {
        log.info("Starting test")
        System.setProperty("webdriver.chrome.driver", driverPath)
        Tester Tester = new Tester()
        int idx = 1

        csv.splitEachLine(',') { row ->
            if (idx > 1) { // Skip the header
                log.info("Processing row: "+idx)
                def testName = row[0].toLowerCase()
                log.info(testName);
                Tester.runTest(testName)
            }
            idx++ // add 1 to the idx
        }
        log.info("End test")
    }

    private void runTest(String testName) {
        // Write the output to the file
        String className = "com.agendadiscovery.tests." + testName

        log.info("Looking for className: ${className}")
        File resultsDir = new File("results")

        // Create the directory if it doesnt exist
        if (!resultsDir.exists()) {
            resultsDir.mkdir()
        }

        new File("results/${className}-results.txt").withWriter { out ->
            try {
                out.println "================================================"
                out.println "Processing class: ${className}"
                //String parameter
                Class[] paramString = new Class[1]
                paramString[0] = String.class

                // Process City
                Class cls = Class.forName(className)
                Object obj = cls.newInstance()

                Method method = cls.getDeclaredMethod("test")
                method.invoke(obj);
//                out.println baseUrl
//
//                List dws = method.invoke(obj, baseUrl);
//                dws.each { dw ->
//                    out.println "Found document: "
//                    out.println "\tTitle - ${dw.title}"
//                    out.println "\tDate - ${dw.dateStr.toString()}"
//                    out.println "\tLink - ${dw.link}"
//                    out.println "\tText - ${dw.text}"
//                }
                out.println "\nEnding process"
//                out.println "Found ${dws.size()} documents"
            } catch (Exception ex) {
                out.println "FAILED TO PROCESS site: ${className}"
                out.println "Caught exception: " + ex.message
            } finally {
                out.println "================================================"
            }
        }
    }
}//end class