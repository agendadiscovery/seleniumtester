package com.agendadiscovery

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.reflect.Method

class Crawler {

    private static final Logger log = LoggerFactory.getLogger(Crawler.class)
    //private static final File csv = new File(getClass().getResource("/csv/crawlCouncil.csv").getPath())
    //debug using test csv
    private static final File csv = new File(getClass().getResource("/csv/crawlCouncil_test.csv").getPath())
    private static final String driverPath = getClass().getResource("/drivers/chromedriver.exe").getPath()


    public static void main(String[] args) {
        log.info("Starting crawl")
        System.setProperty("webdriver.chrome.driver", driverPath)
        Crawler crawler = new Crawler()
        int idx = 1

        csv.splitEachLine(',') { row ->
            if (idx > 1) { // Skip the header
                log.info("Processing row: "+idx)
                def stateAbbr = row[0]?.toUpperCase()
                def geoName = row[1]?.toLowerCase()?.replaceAll(" ","")
                def meetingType = row[2]?.toLowerCase()
                def documentType = row[3]?.toLowerCase()
                def baseUrl = row[4]
                log.info(stateAbbr);  //DEBUG check csv
                log.info(geoName);
                log.info(meetingType);
                log.info(documentType);
                log.info("url " + baseUrl);

                crawler.runCrawl(stateAbbr, geoName, meetingType, documentType, baseUrl)
            }
            idx++ // add 1 to the idx
        }
        log.info("End crawl")
    }

    private void runCrawl(String stateAbbr, String regionName, String meetingType, String documentType, String baseUrl){
        // Write the output to the file
        String className = "com.agendadiscovery.crawlers."+
                "${stateAbbr}_${regionName}_${meetingType}_${documentType}"

        log.info("Looking for className: ${className}")
        File resultsDir = new File("results")

        // Create the directory if it doesnt exist
        if(!resultsDir.exists()){
            resultsDir.mkdir()
        }

        new File("results/${className}-results.txt").withWriter { out ->
            try{
                out.println "================================================"
                out.println "Processing class: ${className}"
                //String parameter
                Class[] paramString = new Class[1]
                paramString[0] = String.class

                // Process City
                Class cls = Class.forName(className)
                Object obj = cls.newInstance()

                Method method = cls.getDeclaredMethod("getDocuments", paramString)
                out.println baseUrl

                List dws = method.invoke(obj, baseUrl);
                dws.each{ dw ->
                    out.println "Found document: "
                    out.println "\tTitle - ${dw.title}"
                    out.println "\tDate - ${dw.dateStr.toString()}"
                    out.println "\tLink - ${dw.link}"
                    out.println "\tText - ${dw.text}"
                }
                out.println "\nEnding process"
                out.println "Found ${dws.size()} documents"
            }catch(Exception ex){
                out.println "FAILED TO PROCESS site: ${className}"
                out.println "Caught exception: " + ex.message
            }finally{
                out.println "================================================"
            }
        }
    }

}
