Pitalium Explorer
========
This is the Test Result Explorer tool which supports `Pitalium`. You
can easily access your test results and understand what is wrong, if these tests
are written with Pitalium.

This product is licensed under the [Apache License, Version 2.0][license].
Our developer site is located at [htmlhifive.com][].

Let's **hifive**!

[license]: http://www.apache.org/licenses/LICENSE-2.0
[htmlhifive.com]: http://www.htmlhifive.com
<!--
![screenshot 1](https://hifive-snu.github.io/pitalium-explorer/img0.png)

![screenshot 2](https://hifive-snu.github.io/pitalium-explorer/img1.png)
-->
### Prerequisite
* [Eclipse IDE for Java EE (Luna SR2)][ide]
* [Tomcat 7][tomcat]
* [Sysdeo Eclipse Tomcat Launcher plugin (3.3.1)][plugin]
  * Set `Tomcat home` in `Window` → `Preferences` → `Tomcat` in *Eclipse*.
  * Copy `DevloaderTomcat7.jar` from `com.sysdeo.eclipse.tomcat_3.3.1.jar` to
    `$TOMCAT_HOME/lib`.
* [hifive-res project][hifive-res]

[ide]: https://eclipse.org/downloads/packages/release/Luna/SR2
[tomcat]: http://tomcat.apache.org/download-70.cgi
[plugin]: http://www.eclipsetotale.com/tomcatPlugin.html
[hifive-res]:https://github.com/hifive/hifive-res.git

### Supported browsers
* Chrome *≥ 44*
* Internet Explorer 11
* Firefox *≥ 37*

### Procedure
1.  Run `ivy_build.xml`. To do so, right-click on `ivy_build.xml.launch` and
    choose `Run As` → `ivy_build.xml`.

2.  Initialize and start database (if you want to use). Change current directory to
    `pitalium-explorer/db/hsql` and run `./init.sh` or `init.bat`.
    And then, run `./start.sh` or `start.bat`.

3.  Update Tomcat context definition. Right-click `pitalium-explorer`
    project and choose `Tomcat project` → `Update context definition`.

4.  Update Tomcat context definition. Right-click `hifive-res`
    project and choose `Tomcat project` → `Update context definition`.

5. Set a result folder. Open `pitalium-explorer/src/main/resources/persistentConfig.json` and change `file.resultDirectory` value to an absolute path of your result folder or a relative one from the `pitalium-explorer` project folder.

6.  Start Tomcat.

7.  Go [http://localhost:8080/pitalium-explorer/list.html][url-list]

[url-list]: http://localhost:8080/pitalium-explorer/list.html

### How to generate API Documents (JSDoc)
1.  Download jsdoc3
    - Download [jsdoc](https://github.com/jsdoc3/jsdoc)
    - Download all fils from Tag to `pitalium-explorer/jsTool/jsdoc/bin`. Let
      `jsdoc.bat` be in the `bin` folder.
2.  Generate
    - Right-click `pitalium-explorer/jsTool/jsdoc/jsdoc.bat` and select open
      in the command prompt.
    - Run `jsdoc.bat`.
    - JSDocs are generated in `pitalium-explorer/doc/jsdoc`.
