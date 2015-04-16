Hifive Test Explorer
========
This is the Test Result Explorer tool which supports hifive-test-library. You
can easily access your test results and understand what is wrong, if these tests
are written with hifive-test-library.

This product is licensed under the [Apache License, Version 2.0][license].
Our developer site is located at [htmlhifive.com][].

Let's **hifive**!

[license]: http://www.apache.org/licenses/LICENSE-2.0)
[htmlhifive.com]: http://www.htmlhifive.com

### 前提
*   [eclipse-jee-kepler-SR2](https://eclipse.org/downloads/packages/release/Kepler/SR2)
*   [apache-tomcat-7.0.59](http://tomcat.apache.org/download-70.cgi)
*   [com.sysdeo.eclipse.tomcat_3.3.0](http://www.eclipsetotale.com/tomcatPlugin.html)
    * **注意**:
      `com.sysdeo.eclipse.tomcat_3.3.0`フォルダーにある`DevloaderTomcat7.jar`を
      `TOMCAT_HOME/lib`へコピーすること。
*   以下のいずれかのブラウザがインストールされていること。
    * chrome (最新版)
    * Internet Explorer11
    * Firefox (最新版)

### 手順
1.  `ivy_build.xml`を実行します。
    `hifive-test-explorer ivy_build.xml.launch`を選択し、右クリックし、`Run As`
    → `hifive-test-explorer ivy_build.xml` で実行します。

2.  `hifive-test-explorer/src/main/webapp/WEB-INF/classes/appConf`の
    `api-conf.properties.sample`をコピーして`api-conf.properties`を作ってその中
    身を書き換えます。テストデータを格納した`sampleData`フォルダはプロジェクトの
    直下にあります。各自の環境に合わせて絶対パスを書き換えてください。

    例）`resultsDir=C:\\hifive\\workspace\\hifive-test-explorer\\sampleData`

3.  Tomcatのコンテキスト定義を更新します。プロジェクトを選択し、右クリックし、
    `Tomcatプロジェクト` → `コンテキスト定義を更新`で実行します。

4.  Tomcatを起動します。

5.  下記にアクセスできることを確認してください。

    [http://localhost:8080/hifive-test-explorer/list.html][url-list]

[url-list]: http://localhost:8080/hifive-test-explorer/list.html

### About Web APIs
You can see the list of this application's APIs from
[http://localhost:8080/hifive-test-explorer/spec/api.html][url-api]

By clicking the buttons labeled "Get a sample result" in that page, you can see
the example data.

[url-api]: http://localhost:8080/hifive-test-explorer/spec/api.html

### APIドキュメント（JSDocドキュメント）の生成方法
1.  jsdoc3をダウンロード
    - jsdoc3はここからダウンロードできます。https://github.com/jsdoc3/jsdoc
    - Tagなどからすべてのファイルをダウンロードし、 hifive/jsTool/jsdoc/bin に配置します。
      ("jsdoc"コマンドが"bin"フォルダに存在するようにします。)

2.  生成
    - `build_for_js.xml`の`jsdoc`ターゲットを実行します。
      `hifive/src/main/webapp/doc`の下にドキュメントが生成されます。
