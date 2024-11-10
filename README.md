# 勤怠管理システム

## CSSについて
- htmlファイルのheadタグ内に、以下の指定をする。
- ``` <link th:href="@{/css/style.css}" rel="stylesheet"> ```
- style.cssの部分は、任意のファイル名を指定する
- cssファイルは、main/resources/static/css内にある
- 何個でもcssファイル作成可能
  
## JavaScriptについて
- htmlファイルのheadタグ内に、以下の指定をする。
- ``` <script th:src="@{/js/test.js}"></script> ```
- test.jsの部分は、任意のファイル名を指定する
- jsファイルは、main/resources/static/js内にある
- 何個でもjsファイル作成可能

## DBについて
### セットアップの流れ
- pgAdmin4の初期設定を行う（エンジニアフリークス第7章を参照）
  - username: postgres
  - password: postgres
- pgAdmin4の「Servers/PostgreSQL/データベース」内に、DBを作成する
  - DB名: kintai_systemで登録する（アンダーバーなのでハイフンにしないよう注意！）
- あとは、初回のサーバー起動（アプリケーション実行）時に自動で必要なテーブルが作成される
- 初回以降は、保存されたレコードがそのまま使える

## Appの起動（サーバーの起動）について
### Eclipse
- Eclipse内で、KintaiSystemフォルダにカーソルを合わせる
- 右クリック
- 「実行 > SpringBootアプリケーション」でサーバー起動
- コンソールにログが出力されるので、エラーが起きてないか確認
- 起動が完了したら、以下のリンクにアクセス
- http://localhost:8080/[任意のhtmlファイル名]
- 拡張子（.html）は不要です！
### VsCode
- 下記プラグインをインストール
  - Java Extension Pack
  - Spring Boot Extension Pack
  - Lombok Annotations Support for VS Code
- このリポジトリをcloneする
- Spring Boot Dashboard > APPS > 起動ボタンをクリック
- 起動が完了したら、以下のリンクにアクセス
- http://localhost:8080/[任意のhtmlファイル名]
- 拡張子（.html）は不要です！
  
## Appの概要
- 講師: login.html
- 社員: loginManager.html
- からどこの画面にも遷移できるようになっています！
- まずは社員アカウントを作成
- 社員アカウントでログイン > 講師登録をクリック > 講師アカウントの登録
- ここまで済めば、login.htmlから講師ログインが可能
