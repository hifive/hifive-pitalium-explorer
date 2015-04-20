Hifive Test Explorer
========
This is the Test Result Explorer tool which supports hifive-test-library. You
can easily access your test results and understand what is wrong, if these tests
are written with hifive-test-library.

This product is licensed under the [Apache License, Version 2.0][license].
Our developer site is located at [htmlhifive.com][].

Let's **hifive**!

[license]: http://www.apache.org/licenses/LICENSE-2.0
[htmlhifive.com]: http://www.htmlhifive.com

### Prerequisite
* [Eclipse IDE for Java EE (Luna SR2)][ide]
* [Tomcat 7][tomcat]
* [Sysdeo Eclipse Tomcat Launcher plugin (3.3.1)][plugin]
  * Set `Tomcat home` in `Window` → `Preferences` → `Tomcat` in *Eclipse*.
  * Copy `DevloaderTomcat7.jar` from `com.sysdeo.eclipse.tomcat_3.3.1.jar` to
    `$TOMCAT_HOME/lib`.

[ide]: https://eclipse.org/downloads/packages/release/Luna/SR2
[tomcat]: http://tomcat.apache.org/download-70.cgi
[plugin]: http://www.eclipsetotale.com/tomcatPlugin.html

### Supported browsers
* Chrome *≥ 44*
* Internet Explorer 11
* Firefox *≥ 37*

### Procedure
1.  Right-click on `ivy_build.xml.launch`and choose `Run As` → `ivy_build.xml`.

2.  Copy `api-conf.properties.sample` to `api-conf.properties` and modify it.
    `api-conf.properties.sample` is located in
    `hifive-test-explorer/src/main/resources/appConf`. Modify the value of the
    key `resultDir` to your absolute path of the sample data folder like below;

    ```
    resultsDir=C:\\hifive\\workspace\\hifive-test-explorer\\sampleData
    ```

    `sampleData` folder is located in `hifiveTestExplorer`.

3.  Update Tomcat context definition. Right-click `hifive-test-explorer`
    project and choose `Tomcat project` → `Update context definition`.

4.  Start Tomcat.

5.  Go [http://localhost:8080/hifive-test-explorer/list.html][url-list]

[url-list]: http://localhost:8080/hifive-test-explorer/list.html

### About Web APIs
You can see the list of this application's APIs from
[http://localhost:8080/hifive-test-explorer/spec/api.html][url-api]

By clicking the buttons labeled "Get a sample result" in that page, you can see
the example data.

[url-api]: http://localhost:8080/hifive-test-explorer/spec/api.html

### How to generate API Documents (JSDoc)
1.  Download jsdoc3
    - Download [jsdoc](https://github.com/jsdoc3/jsdoc)
    - Download all fils from Tag to `hifive-test-explorer/jsTool/jsdoc/bin`. Let
      `jsdoc.bat` be in the `bin` folder.
2.  Generate
    - Right-click `hifive-test-explorer/jsTool/jsdoc/jsdoc.bat` and select open
      in the command prompt.
    - Run `jsdoc.bat`.
    - JSDocs are generated in `hifive-test-explorer/doc/jsdoc`.
