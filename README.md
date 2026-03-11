# CodeDroid 🐍

<div align="center">

![CodeDroid Banner](https://img.shields.io/badge/CodeDroid-v2.1.0-blue?style=for-the-badge&logo=android&logoColor=white)
![Android](https://img.shields.io/badge/Android-8.0%2B-green?style=for-the-badge&logo=android)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0-purple?style=for-the-badge&logo=kotlin)
![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-Material3-orange?style=for-the-badge)
![License](https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge)

**Editor kode profesional untuk Android — powerful, ringan, dan lengkap.**

</div>

---

## 📱 Tentang CodeDroid

CodeDroid adalah editor kode sumber terbuka yang dirancang khusus untuk Android. Dilengkapi dengan syntax highlighting, terminal Termux-style, live preview untuk web, Python runner dengan auto-install library, dan file manager lengkap dengan dukungan media — semua dalam satu aplikasi.

---

## ✨ Fitur Utama

### 🖊️ Editor Kode
- **Syntax Highlighting** untuk 10+ bahasa: Kotlin, Java, Python, JavaScript, TypeScript, HTML, CSS, JSON, XML, Bash, Markdown
- **6 Tema Editor**: Monokai, Darcula, GitHub, Dracula, Nord, Solarized
- **Quick Toolbar** — akses cepat karakter spesial `{ } ( ) [ ] ; : = " ' < > / \ |`
- **Undo / Redo** tidak terbatas
- **Word Wrap** & **Nomor Baris** yang bisa diaktifkan/nonaktifkan
- **Auto Save** opsional
- **Tab Size** yang bisa dikonfigurasi (2 / 4 / 8 spasi)
- **Status Bar** — tampilkan baris, kolom, jumlah karakter, bahasa, encoding

### 📁 File Manager
- **Tampilan Grid & List** — beralih sesuai preferensi
- **Thumbnail gambar** langsung tampil di daftar file
- **Buka gambar** — full-screen image viewer tap-to-open
- **Putar audio** — built-in player untuk MP3, WAV, OGG, M4A, FLAC
- **Putar video** — redirect ke media player eksternal
- **Long press media** → pilih format kode untuk sisipkan langsung ke editor (HTML tag, Markdown, CSS, Kotlin Uri, dll)
- **Dukungan SD Card** — otomatis deteksi penyimpanan eksternal
- **Pencarian file** real-time
- **Buat file/folder** baru langsung dari aplikasi
- **Quick extension chips** — `.kt .py .html .css .js .txt .java`

### 🌐 Live Preview
- **HTML/CSS** — render langsung di WebView built-in
- **JavaScript** — output `console.log` tampil di console preview
- **Markdown** — render jadi halaman HTML yang rapi
- **Python** — auto-detect, langsung ke Python Runner

### 🐍 Python Runner
- **Jalankan file Python** langsung dari editor — tap tombol ▶
- **Auto-detect import** — scan semua `import` di kode
- **Auto pip install** — library yang belum ada langsung diinstall otomatis
- **Skip jika sudah ada** — tidak install ulang library yang sudah terinstall
- **Output real-time** dengan color coding (sukses/error/warning)
- **Error hints** — setiap error Python dijelaskan solusinya dalam Bahasa Indonesia

### 💻 Terminal Termux-Style
- **Tampilan Catppuccin** — dark terminal yang nyaman di mata
- **Shortcut bar** — tap langsung masukkan perintah umum (`ls`, `cd`, `python3`, `pip install`, dll)
- **Navigasi history** ▲▼ — tekan panah untuk perintah sebelumnya
- **Color-coded output** — prompt biru, sukses hijau, error merah, warning kuning
- **Error hints otomatis** — setiap error langsung dikasih solusi dalam Bahasa Indonesia
- **Perintah lengkap**: `ls`, `cd`, `pwd`, `mkdir`, `rm`, `cat`, `cp`, `mv`, `echo`, `date`, `whoami`, `uname`, `env`, `ping`, `python`, `pip`, `history`, `clear`

### 📡 FTP Client
- Koneksi ke server FTP
- Browse direktori remote
- Download file langsung ke editor
- Support passive mode

### 📝 Snippet Manager
- Simpan potongan kode favorit
- Kategorikan per bahasa
- Sisipkan ke editor dengan satu tap
- Edit & hapus snippet

### ⚙️ Pengaturan Lengkap
- Tema aplikasi: Gelap / Terang / Otomatis (ikut sistem)
- Pilih tema editor dari 6 pilihan
- Atur ukuran font (8–28sp)
- Konfigurasi tab size
- Toggle word wrap, nomor baris, auto save

---

## 🎨 Navigation Bar Modern

Navigation bar bawah yang animatif dengan:
- **Dot indicator** pada tab aktif
- **Warna unik** per tab (biru, kuning, hijau, cyan, ungu, hijau terminal, oranye)
- **Animasi smooth** saat berpindah tab
- **7 tab**: Editor · File · Preview · FTP · Snippet · Terminal · Pengaturan

---

## 📸 Screenshot

> *Segera tersedia*

---

## 🚀 Instalasi

### Download APK
1. Buka halaman [Releases](../../releases)
2. Download file `CodeDroid-v2.1.0-debug.apk`
3. Install di perangkat Android (aktifkan "Install dari sumber tidak dikenal")

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

## 📋 Persyaratan

| Komponen | Versi Minimum |
|----------|---------------|
| Android  | 8.0 (API 26)  |
| RAM      | 2 GB          |
| Storage  | 50 MB         |

---

## 🛠️ Tech Stack

| Komponen | Library/Tool |
|----------|-------------|
| Language | Kotlin 2.0 |
| UI | Jetpack Compose + Material3 |
| Architecture | MVVM + ViewModel |
| Storage | DataStore Preferences |
| Image Loading | Coil Compose |
| Media | AndroidX Media3 ExoPlayer |
| Web Preview | AndroidX WebKit |
| FTP | Apache Commons Net |
| JSON | Gson |
| Coroutines | Kotlinx Coroutines |
| Build | Gradle 8.11.1 + AGP 8.7.2 |

---

## 🗺️ Roadmap

### ✅ v2.0.0
- [x] Editor dasar dengan syntax highlighting
- [x] 6 tema editor
- [x] File manager dasar
- [x] Terminal sederhana
- [x] FTP client
- [x] Snippet manager
- [x] Pengaturan lengkap

### ✅ v2.1.0 (Sekarang)
- [x] Navigation bar modern dengan animasi
- [x] File manager lengkap (gambar, audio, video, SD card)
- [x] Image viewer full-screen
- [x] Audio player built-in
- [x] Insert media ke editor (long press)
- [x] Terminal Termux-style dengan Catppuccin theme
- [x] Shortcut bar & history navigation
- [x] Error hints dalam Bahasa Indonesia
- [x] Live Preview (HTML/CSS/JS/Markdown)
- [x] Python Runner dengan auto-install library

### 🔜 v2.2.0 (Dalam Pengembangan)
- [ ] Extension System
  - [ ] Prettier (auto-format kode)
  - [ ] Material Icon Theme
  - [ ] ESLint (deteksi error JS/TS)
  - [ ] Error Lens (error inline)
  - [ ] PHP Intelephense
  - [ ] Laravel Extra Intellisense
  - [ ] Laravel Blade Snippets
  - [ ] Auto Close Tag
  - [ ] One Dark Pro theme
  - [ ] Auto Rename Tag
  - [ ] Indent Rainbow
  - [ ] WakaTime
  - [ ] CodeDroid Pets 🐱
  - [ ] CodeSnap (screenshot kode)
  - [ ] Image Preview inline
- [ ] AI Panel (ChatGPT, Claude, DeepSeek, Kimi, Grok, Gemini, Qwen)
- [ ] Python Library Browser (browse & install dari PyPI)

---

## 📂 Struktur Project

```
app/src/main/java/com/example/codedroid/
├── MainActivity.kt
├── data/
│   ├── ThemePreference.kt
│   ├── SnippetManager.kt
│   └── RecentFilesManager.kt
├── editor/
│   ├── EditorThemes.kt
│   ├── SyntaxHighlighter.kt
│   └── UndoRedoManager.kt
├── network/
│   └── FtpClient.kt
├── terminal/
│   ├── TerminalManager.kt
│   └── TerminalSession.kt
├── ui/
│   ├── NavBar.kt
│   ├── NavPage.kt
│   ├── EditorScreen.kt
│   ├── FileManagerScreen.kt
│   ├── PreviewScreen.kt
│   ├── FtpScreen.kt
│   ├── SnippetsScreen.kt
│   ├── TerminalScreen.kt
│   ├── SettingsScreen.kt
│   ├── QuickToolbar.kt
│   ├── StatusBar.kt
│   ├── TabBar.kt
│   └── theme/
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
└── viewmodel/
    ├── EditorViewModel.kt
    └── TerminalViewModel.kt
```

---

## 🤝 Kontribusi

Kontribusi sangat disambut! Silakan:

1. Fork repository ini
2. Buat branch baru: `git checkout -b fitur/nama-fitur`
3. Commit perubahan: `git commit -m 'Tambah fitur: nama-fitur'`
4. Push ke branch: `git push origin fitur/nama-fitur`
5. Buat Pull Request

---

## 🐛 Laporan Bug

Temukan bug? Buka [Issues](../../issues) dan sertakan:
- Versi Android
- Langkah reproduce
- Screenshot (jika ada)
- Log error (jika ada)

---

## 📄 Lisensi

```
MIT License

Copyright (c) 2025 Ade Pratama

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
```

---

## 👨‍💻 Developer

<div align="center">

**Ade Pratama**

📧 luarnegriakun702@gmail.com

*Dibuat dengan ❤️ menggunakan Kotlin & Jetpack Compose*

</div>

---

<div align="center">

⭐ **Jika CodeDroid membantu, jangan lupa kasih bintang!** ⭐

</div>
