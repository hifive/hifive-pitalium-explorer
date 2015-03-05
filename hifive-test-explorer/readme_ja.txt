●前提
・tomcatPlugin（com.sysdeo.eclipse.tomcat_3.3.0）がインストール、設定されていること。
    com.sysdeo.eclipse.tomcat_3.3.0

    注意
      "com.sysdeo.eclipse.tomcat_3.3.0"フォルダーにある"DevloaderTomcat7.jar"を
      "TOMCAT_HOME/lib"へコピーすること。

・以下のいずれかのブラウザがインストールされていること。
    - chrome（最新版）
    - Internet Explorer11
    - Firefox（最新版）

●手順
1． ivy_build.xmlを実行します。
    hifive-test-explorer ivy_build.xml.launchを選択し、右クリックし、Run As > hifive-test-explorer ivy_build.xml で実行します。

2.  『hifive-test-explorer』プロジェクト - 『src/main/resources』 - 『appConf』のapi-conf.propertiesの中身を書き換えます。
    テストデータを格納したsampleDataフォルダはプロジェクトの直下にあります。各自の環境に合わせて絶対パスを書き換えてください。
    例）
        resultsDir=C:\\hifive\\workspace\\hifive-test-explorer\\sampleData

3.  Tomcatのコンテキスト定義を更新します。
    プロジェクトを選択し、右クリックし、『Tomcatプロジェクト』→『コンテキスト定義を更新』で実行します。

4.  Tomcatを起動します。

5. 下記にアクセスできることを確認してください。
        http://localhost:8080/hifive-test-explorer/list.html

------------------------------------------------------------
APIドキュメント（JSDocドキュメント）の生成方法:

1.jsdoc3をダウンロード

  - jsdoc3はここからダウンロードできます
    https://github.com/jsdoc3/jsdoc

  - Tagなどからすべてのファイルをダウンロードし、 hifive/jsTool/jsdoc/bin に配置します。
    ("jsdoc"コマンドが"bin"フォルダに存在するようにします。)

2.生成

  - build_for_js.xmlのjsdocターゲットを実行します。
    hifive/src/main/webapp/doc の下にドキュメントが生成されます。

------------------------------------------------------------