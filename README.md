# CodeDroid 🐍

<div align="center">

![CodeDroid Banner](https://img.shields.io/badge/CodeDroid-v2.3.1-blue?style=for-the-badge&logo=android&logoColor=white)
![Android](https://img.shields.io/badge/Android-8.0%2B-green?style=for-the-badge&logo=android)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0-purple?style=for-the-badge&logo=kotlin)
![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-Material3-orange?style=for-the-badge)
![License](https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge)

**Editor kode profesional tercanggih untuk Android — powerful, cerdas, dan menyenangkan.**

</div>

---

## 📱 Tentang CodeDroid

CodeDroid adalah mobile IDE (Integrated Development Environment) yang dirancang khusus untuk pengembang Android yang membutuhkan efisiensi dan kekuatan di mana saja. Lebih dari sekadar editor teks, CodeDroid kini hadir dengan integrasi **AI (Artificial Intelligence)**, **Vibe Coding**, **Extension System**, dan fitur unik **Virtual Pets** untuk menemani Anda saat coding.

---

## ✨ Fitur Utama (v2.3.1)

### 🤖 AI Panel & Vibe Coding
- **Multi-Provider AI**: Integrasi langsung dengan Claude (Anthropic), Gemini (Google), OpenAI, dan OpenRouter (DeepSeek, Qwen, Llama 3).
- **Vibe Coding Mode**: Perintahkan AI untuk memodifikasi atau menulis kode secara langsung ke editor Anda (inline execution).
- **Context Awareness**: Kirimkan potongan kode aktif Anda ke asisten AI untuk diagnosis atau refactoring instan.

### 🖊️ Editor Kode & Extensions
- **Extension System**: Mendukung 125+ plugin fungsional, snippet library, dan tema premium.
- **Syntax Highlighting**: Mendukung puluhan bahasa populer (Kotlin, Python, Java, JS/TS, HTML/CSS, dll).
- **Material Icon Theme & Themes**: Ubah tampilan editor dengan tema populer seperti Monokai, Darcula, GitHub, Dracula, Nord, dan Solarized.
- **Auto Close Tag & Rename Tag**: Mempercepat penulisan HTML/XML secara otomatis.

### 🐈 CodeDroid Pets (Virtual Pets)
- **Digital Companion**: Teman setia saat coding yang bisa berjalan, tidur, dan memberikan pesan motivasi/lucu.
- **Interaktif**: Sentuh hewan peliharaan Anda untuk melihat reaksi yang berbeda-beda.
- **Micro-Animations**: Didukung oleh Lottie & Compose Animations untuk pergerakan yang mulus.

### 🐍 Python Powerhouse
- **Python Runner**: Jalankan skrip Python langsung dengan dukungan Chaquopy (Python 3.11).
- **Python Library Browser**: Cari dan instal library dari **PyPI (Pip)** hanya dengan satu klik — tanpa ribet terminal.
- **Auto-Pip**: Deteksi otomatis `import` yang hilang dan tawarkan instalasi otomatis.

### 📁 Advanced File Manager & Media
- **Rich Preview**: Thumbnail gambar dan built-in player untuk Audio/Video.
- **Drag-to-Code**: Konversi file media menjadi tag HTML, Markdown, atau Kotlin Uri dengan long-press.
- **SD Card Support**: Akses penuh ke penyimpanan eksternal.

### 💻 Terminal & Live Preview
- **Full Terminal**: Terminal Termux-style dengan tema Catppuccin, shortcut bar, dan history navigation.
- **Web Preview**: Render HTML/CSS/JS secara real-time dengan Console Log viewer.
- **Error Hints**: Penjelasan error coding dalam Bahasa Indonesia yang mudah dipahami.

---

## 🚀 Instalasi

### Build dari Source
```bash
# Clone repository
git clone https://github.com/adepratama/CodeDroid.git
cd CodeDroid

# Build debug APK
./gradlew assembleDebug

# Install ke device
./gradlew installDebug
```

---

## 🛠️ Tech Stack & Library

| Komponen | Teknologi/Library |
|----------|-------------------|
| **Language** | Kotlin 2.0 (K2 Compiler) |
| **UI Framework** | Jetpack Compose (Material Design 3) |
| **Python Runtime**| Chaquopy 15.0.1 (Python 3.11) |
| **Animations** | Lottie Compose & Compose Motion |
| **Image Loading** | Coil Compose |
| **Networking** | OkHttp 4.12.0 & Apache Commons Net (FTP) |
| **Media Player** | AndroidX Media3 (ExoPlayer) |
| **Security** | AndroidX Security-Crypto |

---

## 🗺️ Roadmap & History

### ✅ v2.1.0 (Stabil)
- [x] Modern Navigation Bar & Animation
- [x] Full Media Manager Support
- [x] Terminal Termux-Style

### ✅ v2.2.0 (Update Besar)
- [x] Extension System (125+ Plugins)
- [x] CodeDroid Pets 🐱 (Virtual Pet Companion)
- [x] UI/UX Refresh ke Material You (M3)

### ✅ v2.3.1 (Rilis Terbaru - SEKARANG)
- [x] **AI Panel**: Integrasi Claude, Gemini, OpenAI via OpenRouter
- [x] **Vibe Coding**: AI Inline Code Editing
- [x] **Python Library Browser**: Pip Graphical Manager
- [x] **Improved Editor Performance**: Penanganan file besar lebih lancar

### 🔜 v2.4.0 (Akan Datang)
- [ ] Git Integration (Clone, Commit, Push, Pull)
- [ ] Collaborative Coding (P2P Over Wi-Fi)
- [ ] Full Desktop Mode (Support DeX & Android Desktop)

---

## 📂 Struktur Project (Current)

```
app/src/main/java/com/example/codedroid/
├── data/           # Config, API Key Vault, & Managers
├── editor/         # Core Editor, Themes, & Syntax Highlighter
├── network/        # FTP & Network Client
├── terminal/       # Terminal Logic & Session
├── ui/             # Jetpack Compose Screens
│   ├── AiPanel/    # AI Integration UI & Vibe Coding
│   ├── Extensions/ # Plugins & Themes Manager
│   ├── VirtualPet/ # Life Cycle Pet & Animation
│   └── ...         # Editor, File, Preview, Settings
└── viewmodel/      # Core Application Logic (MVVM)
```

---

## 🤝 Kontribusi & Dukungan

Kontribusi sangat disambut! Jika Anda menemukan bug atau ingin menambahkan fitur (seperti pets baru atau extension), jangan ragu untuk:
1. Fork Repository
2. Buat Pull Request
3. Buka Discussion

---

## 📄 Lisensi

Proyek ini berada di bawah lisensi **MIT**. Anda bebas menggunakan, memodifikasi, dan mendistribusikan kode ini sesuai dengan ketentuan yang berlaku.

---

## 👨‍💻 Developer

<div align="center">

**Ade Pratama**

📧 luarnegriakun702@gmail.com

*Dibuat dengan ❤️ di Indonesia menggunakan Kotlin & Compose*

---

⭐ **Jika Anda menyukai CodeDroid, berikan bintang di repository ini!** ⭐

</div>
