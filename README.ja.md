Pitalium Explorer
========
This is the Test Result Explorer tool which supports Pitalium. You
can easily access your test results and understand what is wrong, if these tests
are written with Pitalium.

This product is licensed under the [Apache License, Version 2.0][license].
Our developer site is located at [htmlhifive.com][].

Let's **hifive**!

[license]: http://www.apache.org/licenses/LICENSE-2.0)
[htmlhifive.com]: http://www.htmlhifive.com

### 前提
* [Eclipse IDE for Java EE (Luna SR2)][ide]
* [Tomcat 7][tomcat]
* [Sysdeo Eclipse Tomcat Launcher plugin (3.3.1)][plugin]
  * Set `Tomcat home` in `Window` → `Preferences` → `Tomcat` in *Eclipse*.
  * `com.sysdeo.eclipse.tomcat_3.3.1.jar`フォルダーにある`DevloaderTomcat7.jar`
    を`$TOMCAT_HOME/lib`へコピーすること。

[ide]: https://eclipse.org/downloads/packages/release/Luna/SR2
[tomcat]: http://tomcat.apache.org/download-70.cgi
[plugin]: http://www.eclipsetotale.com/tomcatPlugin.html

### Supported browsers
* Chrome *≥ 44*
* Internet Explorer 11
* Firefox *≥ 37*

### 手順
1.  `ivy_build.xml`を実行します。`ivy_build.xml.launch`を選択し、右クリックし、
    `Run As` → `ivy_build.xml` で実行します。

2.  データベースを初期化して起動します。カレントディレクトリーを
    `pitalium-explorer/db/hsql`に変わって`./init.sh`や`init.bat`を実行します。
    その後に`./start.sh`や`start.bat`を実行します。

3.  Tomcatのコンテキスト定義を更新します。プロジェクトを選択し、右クリックし、
    `Tomcatプロジェクト` → `コンテキスト定義を更新`で実行します。

4.  Tomcatを起動します。

5.  下記にアクセスできることを確認してください。

    [http://localhost:8080/pitalium-explorer/list.html][url-list]

[url-list]: http://localhost:8080/pitalium-explorer/list.html

### APIドキュメント（JSDocドキュメント）の生成方法
1.  jsdoc3をダウンロード
    - jsdoc3はここからダウンロードできます。https://github.com/jsdoc3/jsdoc
    - Tagなどからすべてのファイルをダウンロードし、 hifive/jsTool/jsdoc/bin に配置します。
      ("jsdoc"コマンドが"bin"フォルダに存在するようにします。)

2.  生成
    - `build_for_js.xml`の`jsdoc`ターゲットを実行します。
      `hifive/src/main/webapp/doc`の下にドキュメントが生成されます。
