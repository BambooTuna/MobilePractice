
## 環境構築
- Gradlew作成
```bash
$ gradle wrapper


// Pathが通っていない場合(バージョンは各自の環境に合わせてください)
$ ~/.gradle/wrapper/dists/gradle-5.5.1-all/8yl0syll3fr5m1v472nzznadi/gradle-5.5.1/bin/gradle wrapper
```

## コンパイル・ビルド・テスト

- ビルド(.apkの作成)
`./app/build/outputs/apk`以下に作成されていた
```bash
$ ./gradlew build
```

- クリーン
```bash
$ ./gradlew clean
```

## エミュレーター
`$ export ANDROID_SDK_HOME=~/Library/Android/sdk`

- デバイス一覧
```bash
$ ${ANDROID_SDK_HOME}/emulator/emulator -list-avds

4_WVGA_Nexus_S_API_29
Nexus_5X_API_29_x86
```

- 起動
```bash
$ ${ANDROID_SDK_HOME}/emulator/emulator -avd 4_WVGA_Nexus_S_API_29
```

- ビルド済みアプリインストール
エミュレーターが標準設定で起動していれば、以下コマンド実行で勝手にインストールされる。
※起動は手動でエミュレーターの画面を操作する。
```bash
$ ${ANDROID_SDK_HOME}/platform-tools/adb install ./app/build/outputs/apk/debug/app-debug.apk
```
