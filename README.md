# 🧠 LocalMind AI — On-Device Offline AI Assistant

<p align="center">
  <img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher_foreground.png" width="120" height="120" alt="LocalMind AI Logo" />
</p>

<p align="center">
  <b>A private, minimalist, and ultra-fast AI chatbot running Small Language Models (SLMs) 100% on-device.</b><br>
  <i>Powered by Google LiteRT-LM • Native Android • Kotlin • Jetpack Compose • Material 3</i>
</p>

<p align="center">
  <a href="#features">Features</a> •
  <a href="#download-apk">Download APK</a> •
  <a href="#supported-models">Supported Models</a> •
  <a href="#tech-stack">Tech Stack</a> •
  <a href="#architecture">Architecture</a> •
  <a href="#fa-راهنمای-فارسی">راهنمای فارسی</a>
</p>

---

## 🌟 Overview

**LocalMind AI** is an open-source native Android application designed to run generative AI language models directly on your smartphone hardware without relying on cloud APIs, external servers, or internet connectivity. 

Once your chosen language model is downloaded or imported, the entire conversational inference pipeline runs strictly in **airplane mode**, providing zero latency from network round-trips, total data privacy, and zero subscription fees.

---

## 📦 Download APK

The pre-compiled production debug APK is included directly inside the repository under the [`project_apk/`](project_apk/) and [`build_app/`](build_app/) directories:

- 📥 **Direct APK:** [`project_apk/LocalMind_AI.apk`](project_apk/LocalMind_AI.apk)
- 📥 **Alternative mirror:** [`project_apk/app-debug.apk`](project_apk/app-debug.apk) (also mirrored in [`build_app/LocalMind_AI.apk`](build_app/LocalMind_AI.apk))
- **Minimum Android Version:** Android 10 (API level 29)
- **Target Android Version:** Android 15 (API level 35)

---

## ✨ Key Features

### 🔒 100% Offline & Pure Air-Gapped Privacy
* **Zero Internet Permissions:** No `INTERNET` permission in `AndroidManifest.xml` — physically impossible to send or receive data over the web.
* **No Download Links:** The app contains no remote download links or web APIs. Models are copied purely offline.
* **Direct File Path Placement:** Copy `.litertlm` models directly into the app storage path (`Android/data/.../files/models/`) via USB/file manager, or import directly from device storage.
* **Works Everywhere:** 100% functional in airplane mode on local CPU or GPU.
* **No Account Required:** Immediate access without logins, API keys, or cloud dependencies.

### ⚡ Powered by Google LiteRT-LM
* Uses official `com.google.ai.edge.litertlm:litertlm-android` inference engine.
* **Hardware Acceleration:** Automatic GPU delegate initialization with seamless fallback to CPU.
* **Real-time Streaming:** Token-by-token generation with an interactive **Stop** generation button.

### 📚 Purely Offline Local Model Manager
* **Zero Web Downloads:** No remote URLs or background internet downloads.
* **Direct Storage Path Display:** In-app folder address display with one-tap "Copy Path" button and "Scan Folder" trigger.
* **Local Storage Import:** One-tap import and copy from phone storage (Downloads, SD card, etc.) into the local models directory.
* **Curated Small Language Models (SLMs):**
  * **Qwen 2.5 0.5B Instruct** (`qwen2.5-0.5b-instruct.litertlm` ~446 MB) — Ultra-fast, ideal for 2GB–3GB RAM devices.
  * **Gemma 3 1B IT** (`gemma-3-1b-it.litertlm` ~1.2 GB) — Google mobile-optimized foundation model.
  * **Qwen 2.5 1.5B Instruct** (`qwen2.5-1.5b-instruct.litertlm` ~1.4 GB) — High precision multilingual responses for 4GB+ RAM.
  * **SmolLM2 360M Instruct** (`smollm2-360m-instruct.litertlm` ~329 MB) — Ultra-lightweight footprint.
  * **Custom `.litertlm` Models:** Full support for any custom-trained or quantized model file.

### 🎨 Minimalist ChatGPT-Inspired Design
* Clean, distraction-free interface adhering to **Material Design 3 (M3)** guidelines.
* Rounded conversational message bubbles with one-tap clipboard copy.
* Dynamic status badge indicating model state: `Offline AI Ready (GPU/CPU)`, `Loading...`, or `No Model Selected`.
* Quick suggestion prompt chips for rapid interaction.

### 🌍 Persian (فارسی) & English Multilingual Support
* Full native Right-To-Left (RTL) layout switching via `LocalLayoutDirection`.
* Persian welcome message and start prompts.
* Language selection: Persian, English, or System Default.

### 🗄️ Local Chat Persistence
* **Room Database:** Complete conversation and message history stored locally with SQLite and Kotlin Coroutines / Flow.
* Option to clear chat history or start clean conversations anytime.

---

## 🤖 Supported Models

| Model | Size | Architecture | Min RAM | Languages | Recommended Use |
| :--- | :---: | :---: | :---: | :---: | :--- |
| **Qwen 2.5 0.5B Instruct** | 446 MB | LiteRT-LM | 2 GB | Persian, English, Multilingual | Ultra-lightweight, everyday tasks, fast inference |
| **Gemma 3 1B IT** | 1.2 GB | Google LiteRT | 3 GB | Persian, English, Multilingual | High reasoning, creative writing, translation |
| **Qwen 2.5 1.5B Instruct** | 1.4 GB | LiteRT-LM | 4 GB | Persian, English, Multilingual | Complex instructions, code, detailed answers |
| **SmolLM2 360M Instruct** | 329 MB | LiteRT-LM | 1.5 GB | English, Multilingual | Minimum RAM consumption, fastest loading |
| **Custom Local Model** | Dynamic | `.litertlm` | Device-dependent | Custom | User-imported weights from local storage |

---

## 🛠️ Tech Stack & Architecture

- **Language:** Kotlin 2.0+
- **UI Framework:** Jetpack Compose (Declarative UI)
- **Design System:** Material Design 3 (M3)
- **On-Device Inference:** Google AI Edge LiteRT-LM (`litertlm-android`)
- **Local Database:** Android Jetpack Room (with KSP)
- **Asynchronous Execution:** Kotlin Coroutines & StateFlow
- **Networking:** OkHttp 4 for chunked model downloads
- **Architecture Pattern:** MVVM (Model-View-ViewModel) + Clean Architecture

### Project Directory Structure

```text
LocalMind-AI/
├── build_app/
│   ├── LocalMind_AI.apk          # Pre-built installable APK
│   └── app-debug.apk             # Standard debug build artifact
├── app/
│   ├── src/main/java/com/example/
│   │   ├── MainActivity.kt       # Single activity entry point with edge-to-edge
│   │   ├── data/
│   │   │   ├── local/            # Room Database (AppDatabase, ChatDao, ChatEntities)
│   │   │   └── repository/       # Repositories (ChatRepository, SettingsRepository, ModelRepository)
│   │   ├── downloader/           # OkHttp background model downloader
│   │   ├── inference/            # LiteRT-LM engine wrapper with GPU/CPU fallback
│   │   ├── model/                # Data models (ModelInfo, AppSettings, DownloadState)
│   │   └── ui/
│   │       ├── ChatViewModel.kt  # State management & coroutine orchestration
│   │       ├── Localization.kt   # Persian & English strings and RTL helper
│   │       ├── screens/          # ChatScreen, ModelManagerScreen, SettingsScreen
│   │       ├── components/       # ChatMessageItem, ChatInputBar, ModelCardItem, StatusBadge
│   │       └── theme/            # Material 3 Color palette, Typography, Theme
│   └── src/main/res/             # Adaptive launcher icons, values, XML configs
└── build.gradle.kts              # Root & App Gradle Kotlin DSL configurations
```

---

## 🚀 Building from Source

### Prerequisites
1. Android Studio Ladybug / Meerkat or newer.
2. Android SDK with `minSdk = 29` and `compileSdk = 35`.
3. JDK 17 or JDK 21.

### Build Commands

```bash
# Clone the repository
git clone https://github.com/your-username/LocalMind-AI.git
cd LocalMind-AI

# Run local unit & Robolectric tests
gradle :app:testDebugUnitTest

# Assemble Debug APK
gradle assembleDebug

# Output APK location:
# app/build/outputs/apk/debug/app-debug.apk
```

---

## 🇮🇷 راهنمای فارسی

**لوکال‌مایند (LocalMind AI)** یک دستیار هوشمند و پیام‌رسان کاملاً آفلاین برای سیستم‌عامل اندروید است که مدل‌های زبانی کوچک (SLM) را مستقیماً بر روی سخت‌افزار تلفن همراه شما بدون نیاز به اتصال به اینترنت اجرا می‌کند.

### ویژگی‌های اصلی:
1. **۱۰۰٪ آفلاین و بدون دسترسی اینترنت:** دسترسی اینترنت در مانیفست برنامه حذف شده است. تمامی پردازش‌های هوش مصنوعی مستقیماً بر روی پردازنده گوشی شما (CPU یا شتاب‌دهنده گرافیکی GPU) انجام می‌شود.
2. **بدون لینک دانلود اینترنتی:** فایل‌های مدل تنها از طریق کپی آفلاین در مسیر پوشه (`Android/data/.../files/models/`) یا وارد کردن مستقیم از حافظه گوشی (Downloads، کارت حافظه و...) بارگذاری می‌شوند.
3. **حفظ کامل حریم خصوصی:** هیچ داده، پیام یا لاگی از دستگاه خارج نمی‌شود.
4. **پشتیبانی کامل از زبان فارسی:** رابط کاربری راست‌چین (RTL) خودکار همراه با پیام‌های راهنمای فارسی.
5. **مدیریت مدل‌های محلی:** امکان اسکن خودکار پوشه و شناسایی مدل‌های Qwen 2.5 0.5B/1.5B، Gemma 3 1B و SmolLM2 و مدل‌های دلخواه با فرمت `.litertlm`.
6. **طراحی مینیمال و کاربرپسند:** الهام‌گرفته از رابط کاربری تمیز ChatGPT، همراه با حباب‌های گفتگو، امکان کپی پاسخ‌ها و دکمه توقف تولید متن.

---

## 📄 License

This project is licensed under the Apache License 2.0. See the [LICENSE](LICENSE) file for details.
