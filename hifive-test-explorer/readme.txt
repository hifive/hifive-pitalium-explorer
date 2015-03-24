●Preparation
Prepare the following development tools.

・eclipse-jee-kepler-SR2
    https://eclipse.org/downloads/packages/release/Kepler/SR2

・apache-tomcat-7.0.59
    http://tomcat.apache.org/download-70.cgi

・com.sysdeo.eclipse.tomcat_3.3.0
    http://www.eclipsetotale.com/tomcatPlugin.html

    Note:
       Copy "DevloaderTomcat7.jar" to "TOMCAT_HOME/lib"
      "DevloaderTomcat7.jar" is located in "com.sysdeo.eclipse.tomcat_3.3.0" folder.

・Any one of browsers is installed.
    - chrome (latest version)
    - Internet Explorer11
    - Firefox (latest version)

------------------------------------------------------------
●Procedure
1.  Run ivy_build.xml.
    Right-click on "hifive-test-explorer ivy_build.xml.launch" and choose "Run As" > "hifive-test-explorer ivy_build.xml".

2.  Modify "api-conf.properties".
    "api-conf.properties" is located in the following folder.
        "hifive-test-explorer" > "src/main/resources" > "appConf"
    Modify the value of the key "resultDir" to your absolute path of the sample data folder.
    "sampleData" folder is located in "hifive-test-explorer" project.
    ex)
        resultsDir=C:\\hifive\\workspace\\hifive-test-explorer\\sampleData

3.  Update Tomcat context definition.
    Right-click "hifive-test-explorer" project and choose "Tomcat project" > "Update context definition".

4.  Start HSQLDB.
    Run db/hsqldb/startHSQLDB.bat.

5.  Start Tomcat.

6.  Access this application URL:
	http://localhost:8080/hifive-test-explorer/list.html

------------------------------------------------------------
●About Web APIs
You can see the list of this application's APIs from below:
	http://localhost:8080/hifive-test-explorer/spec/api.html

By clicking the buttons labeled "Get a sample result" in that page,
you can see the example data.
	Note: You need to complete the above procedure.

------------------------------------------------------------
●How to generate API Documents（JSDoc）:

1. Download jsdoc3

  - You can download jsdoc3 from as follow:
    https://github.com/jsdoc3/jsdoc.

  - Download all fils from Tag to hifive-test-explorer/jsTool/jsdoc/bin.
    (make "jsdoc.bat" be in the "bin" folder)

2. Generate

  - Right-click hifive-test-explorer/jsTool/jsdoc/jsdoc.bat and select open in the command prompt.

  - Run jsdoc.bat.

  - JSDocs are generated in hifive-test-explorer/doc/jsdoc.

------------------------------------------------------------