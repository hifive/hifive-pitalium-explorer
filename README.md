# hifive-test-explorer

This is the Test Result Explorer tool which supports hifive-test-library.  
You can easily access your test results and understand what is wrong,
if these tests are written with hifive-test-library.

This product is licensed under the Apache License, Version 2.0.  
[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Our developer site is located at  
[http://www.htmlhifive.com](http://www.htmlhifive.com)

Let's hifive !

------------------------------------------------------------------------------
●Preparation  
Prepare the following development tools.

+ [eclipse-jee-kepler-SR2](https://eclipse.org/downloads/packages/release/Kepler/SR2)

+ [apache-tomcat-7.0.59](http://tomcat.apache.org/download-70.cgi)

+ [com.sysdeo.eclipse.tomcat_3.3.0](http://www.eclipsetotale.com/tomcatPlugin.html)  
   **Note:** Copy "DevloaderTomcat7.jar" to "TOMCAT_HOME/lib".  
   "DevloaderTomcat7.jar" is located in "com.sysdeo.eclipse.tomcat_3.3.0" folder.

+ Any one of browsers is installed.
   * chrome (latest version)
   * Internet Explorer11
   * Firefox (latest version)

------------------------------------------------------------
●Procedure

1. Check out hifive-test-explorer project.
2.  Run ivy_build.xml.  
     	Right-click on "hifive-test-explorer ivy\_build.xml.launch" and choose "Run As" > "hifiveTestExplorer ivy_build.xml".
3.  Copy "api-conf.properties.sample" to "api-conf.properties" and modify "api-conf.properties".
    "api-conf.properties.sample" is located in the following folder.
        "hifive-test-explorer" > "src/main/resources" > "appConf"  
    Modify the value of the key "resultDir" to your absolute path of the sample data folder.  
    "sampleData" folder is located in "hifiveTestExplorer" project.  
    ex)  resultsDir=C:\\hifive\\workspace\\hifive-test-explorer\\sampleData
4.  Update Tomcat context definition.  
    Right-click "hifive-test-explorer" project and choose "Tomcat project" > "Update context definition".
5.  Start Tomcat.
6.  Access this application URL:  
	http://localhost:8080/hifive-test-explorer/list.html

------------------------------------------------------------