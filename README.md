# hifive-test-explorer

This tool is the Test Result Explorer which complements hifive-test-library.  
You can see easily your test results, details and wrong points,
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

1. Check out hifiveTestExplorer project.
2.  Run ivy_build.xml.  
     	Right-click on "hifiveTestExplorer ivy\_build.xml.launch" and choose "Run As" > "hifiveTestExplorer ivy_build.xml".
3.  Modify "api-conf.properties".  
    "api-conf.properties" is located in the following folder.  
        "hifiveTestExplorer" > "src/main/resources" > "appConf"  
    Modify the value of the key "resultDir" to your absolute path of the sample data folder.  
    "sampleData" folder is located in "hifiveTestExplorer" project.  
    ex)  
        resultsDir=C:\\hifive\\workspace\\hifiveTestExplorer\\sampleData
4.  Update Tomcat context definition.  
    Right-click "hifiveTestExplorer" project and choose "Tomcat project" > "Update context definition".
5.  Start Tomcat.
6.  Access this application URL:  
	http://localhost:8080/hifiveTestExplorer/list.html

------------------------------------------------------------