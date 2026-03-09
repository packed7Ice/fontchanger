# FontChanger

アルファベットを特殊なUnicodeフォントに変換するAndroidアプリです。  
フローティングウィンドウ機能により、他のアプリを使いながらフォント変換・コピーが行えます。

## 機能

- **17種類のフォント変換** — Bold, Italic, Script, Fraktur, Double-Struck, Monospace, Circled, Squared など
- **フローティングウィンドウ** — 他アプリの上に小窓を表示し、テキスト入力・変換・コピーが可能
- **ワンタップコピー** — 変換結果をすぐにクリップボードへコピー
- **ライト/ダークテーマ** — システム設定に追従、または手動で切替
- **カスタマイズ** — デフォルトフォント、ウィンドウ不透明度・幅、自動コピー機能

## 対応フォント一覧

| スタイル | 例 |
|---------|-----|
| Bold | 𝐀𝐁𝐂 |
| Italic | 𝐴𝐵𝐶 |
| Bold Italic | 𝑨𝑩𝑪 |
| Sans-Serif | 𝖠𝖡𝖢 |
| Sans-Serif Bold | 𝗔𝗕𝗖 |
| Sans-Serif Italic | 𝘈𝘉𝘊 |
| Sans-Serif Bold Italic | 𝘼𝘽𝘾 |
| Script | 𝒜ℬ𝒞 |
| Bold Script | 𝓐𝓑𝓒 |
| Fraktur | 𝔄𝔅ℭ |
| Bold Fraktur | 𝕬𝕭𝕮 |
| Double-Struck | 𝔸𝔹ℂ |
| Monospace | 𝙰𝙱𝙲 |
| Circled | ⒶⒷⒸ |
| Squared | 🄰🄱🄲 |
| Small Caps | ᴀʙᴄ |
| Upside Down | ɐqɔ |

## 動作環境

- Android 8.0 (API 26) 以上
- フローティングウィンドウには「他のアプリの上に表示」権限が必要

## ビルド

```bash
# Android Studio の JBR を使用
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
.\gradlew.bat assembleDebug
```

## 技術スタック

- Kotlin
- Jetpack Compose (Material 3)
- Android Foreground Service (フローティングウィンドウ)

## ライセンス

[Apache License 2.0](LICENSE)

© 2026 YorikawaAise
