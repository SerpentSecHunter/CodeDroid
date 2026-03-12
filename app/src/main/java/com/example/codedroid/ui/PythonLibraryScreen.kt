package com.example.codedroid.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codedroid.terminal.TerminalManager

data class PyLibrary(val name: String, val description: String, val category: String)

val pyLibraries = listOf(
    // Data Science
    PyLibrary("numpy","Komputasi numerik & array multidimensi","Data Science"),
    PyLibrary("pandas","Analisis dan manipulasi data tabel","Data Science"),
    PyLibrary("matplotlib","Visualisasi data dan grafik 2D","Data Science"),
    PyLibrary("seaborn","Statistik visualisasi berbasis matplotlib","Data Science"),
    PyLibrary("scipy","Komputasi saintifik dan teknik","Data Science"),
    PyLibrary("statsmodels","Model statistik dan ekonometrika","Data Science"),
    PyLibrary("plotly","Grafik interaktif dan dashboard","Data Science"),
    PyLibrary("bokeh","Visualisasi web interaktif","Data Science"),
    PyLibrary("altair","Visualisasi deklaratif berbasis Vega","Data Science"),
    PyLibrary("pyarrow","Format data kolumnar Apache Arrow","Data Science"),
    // Machine Learning
    PyLibrary("scikit-learn","Machine learning tools lengkap","Machine Learning"),
    PyLibrary("tensorflow","Deep learning framework Google","Machine Learning"),
    PyLibrary("torch","PyTorch — deep learning Facebook","Machine Learning"),
    PyLibrary("keras","API deep learning high-level","Machine Learning"),
    PyLibrary("xgboost","Gradient boosting tercepat","Machine Learning"),
    PyLibrary("lightgbm","Gradient boosting Microsoft","Machine Learning"),
    PyLibrary("catboost","Gradient boosting Yandex","Machine Learning"),
    PyLibrary("transformers","Model NLP Hugging Face","Machine Learning"),
    PyLibrary("sentence-transformers","Embedding kalimat semantic","Machine Learning"),
    PyLibrary("optuna","Optimasi hyperparameter otomatis","Machine Learning"),
    // Web Framework
    PyLibrary("flask","Web framework ringan dan fleksibel","Web"),
    PyLibrary("django","Web framework full-featured","Web"),
    PyLibrary("fastapi","API modern berbasis type hints","Web"),
    PyLibrary("starlette","ASGI framework ringan","Web"),
    PyLibrary("tornado","Web server async","Web"),
    PyLibrary("bottle","Micro web framework minimalis","Web"),
    PyLibrary("falcon","API framework performa tinggi","Web"),
    PyLibrary("sanic","Web server async cepat","Web"),
    PyLibrary("aiohttp","HTTP client/server async","Web"),
    PyLibrary("uvicorn","ASGI server berbasis uvloop","Web"),
    // HTTP & Network
    PyLibrary("requests","HTTP requests simpel dan elegan","Network"),
    PyLibrary("httpx","HTTP client sync dan async modern","Network"),
    PyLibrary("urllib3","HTTP client library tingkat rendah","Network"),
    PyLibrary("websockets","Library WebSocket async","Network"),
    PyLibrary("paramiko","SSH client dan server","Network"),
    PyLibrary("ftplib","FTP client standard library","Network"),
    PyLibrary("socket","Network socket low-level","Network"),
    PyLibrary("scapy","Manipulasi paket jaringan","Network"),
    PyLibrary("twisted","Event-driven networking","Network"),
    PyLibrary("pyzmq","Binding Python untuk ZeroMQ","Network"),
    // Database
    PyLibrary("sqlalchemy","ORM database Python terpopuler","Database"),
    PyLibrary("psycopg2","Driver PostgreSQL","Database"),
    PyLibrary("pymysql","Driver MySQL","Database"),
    PyLibrary("pymongo","Driver MongoDB","Database"),
    PyLibrary("redis","Client Redis","Database"),
    PyLibrary("motor","Driver MongoDB async","Database"),
    PyLibrary("tortoise-orm","ORM async untuk Python","Database"),
    PyLibrary("databases","Database async dengan SQLAlchemy","Database"),
    PyLibrary("alembic","Migrasi database SQLAlchemy","Database"),
    PyLibrary("peewee","ORM simpel dan ringan","Database"),
    // Image & Media
    PyLibrary("pillow","Pemrosesan gambar (PIL Fork)","Media"),
    PyLibrary("opencv-python","Computer vision dan pemrosesan gambar","Media"),
    PyLibrary("imageio","Baca/tulis berbagai format gambar","Media"),
    PyLibrary("moviepy","Editing video dengan Python","Media"),
    PyLibrary("pygame","Game development dan multimedia","Media"),
    PyLibrary("pyaudio","Audio recording dan playback","Media"),
    PyLibrary("pydub","Manipulasi audio mudah","Media"),
    PyLibrary("cairosvg","Konversi SVG ke PNG/PDF","Media"),
    PyLibrary("qrcode","Generate QR code","Media"),
    PyLibrary("barcode","Generate barcode berbagai format","Media"),
    // Automation & Scraping
    PyLibrary("selenium","Otomasi browser web","Automation"),
    PyLibrary("playwright","Browser automation modern","Automation"),
    PyLibrary("beautifulsoup4","Parsing HTML dan XML","Automation"),
    PyLibrary("scrapy","Framework web scraping","Automation"),
    PyLibrary("pyautogui","Otomasi GUI mouse dan keyboard","Automation"),
    PyLibrary("schedule","Penjadwalan tugas sederhana","Automation"),
    PyLibrary("apscheduler","Scheduler tugas advanced","Automation"),
    PyLibrary("celery","Distributed task queue","Automation"),
    PyLibrary("mechanize","Browser web programmable","Automation"),
    PyLibrary("lxml","Parsing XML dan HTML cepat","Automation"),
    // Security & Crypto
    PyLibrary("cryptography","Kriptografi dan keamanan","Security"),
    PyLibrary("pycryptodome","Implementasi algoritma kriptografi","Security"),
    PyLibrary("hashlib","Hash SHA, MD5, dll (stdlib)","Security"),
    PyLibrary("bcrypt","Hashing password bcrypt","Security"),
    PyLibrary("jwt","JSON Web Token","Security"),
    PyLibrary("passlib","Library hashing password","Security"),
    PyLibrary("pyotp","One-time password (TOTP/HOTP)","Security"),
    PyLibrary("bandit","Analisis keamanan kode Python","Security"),
    PyLibrary("certifi","Sertifikat CA Mozilla","Security"),
    PyLibrary("pyOpenSSL","Binding Python untuk OpenSSL","Security"),
    // Testing
    PyLibrary("pytest","Framework testing modern","Testing"),
    PyLibrary("unittest","Testing framework standard library","Testing"),
    PyLibrary("mock","Mocking untuk unit test","Testing"),
    PyLibrary("hypothesis","Property-based testing","Testing"),
    PyLibrary("factory-boy","Factory untuk test fixture","Testing"),
    PyLibrary("faker","Generator data palsu untuk testing","Testing"),
    PyLibrary("coverage","Analisis code coverage","Testing"),
    PyLibrary("tox","Otomasi testing multi-environment","Testing"),
    PyLibrary("locust","Load testing dengan Python","Testing"),
    PyLibrary("responses","Mock HTTP requests di test","Testing"),
    // Utilities
    PyLibrary("pydantic","Validasi data dengan type hints","Utility"),
    PyLibrary("attrs","Kelas dengan dekorator @attr","Utility"),
    PyLibrary("dataclasses","Dataclass Python 3.7+ (stdlib)","Utility"),
    PyLibrary("click","Buat CLI aplikasi yang elegan","Utility"),
    PyLibrary("typer","CLI berbasis type hints","Utility"),
    PyLibrary("rich","Output terminal yang cantik","Utility"),
    PyLibrary("tqdm","Progress bar untuk loop","Utility"),
    PyLibrary("loguru","Logging Python yang mudah","Utility"),
    PyLibrary("colorama","Warna teks di terminal Windows","Utility"),
    PyLibrary("tabulate","Format data tabel di terminal","Utility"),
    // File & System
    PyLibrary("pathlib","Path file modern (stdlib)","System"),
    PyLibrary("os","Operasi sistem OS (stdlib)","System"),
    PyLibrary("shutil","Operasi file tingkat tinggi (stdlib)","System"),
    PyLibrary("watchdog","Monitor perubahan file system","System"),
    PyLibrary("pyfilesystem2","Abstraksi sistem file","System"),
    PyLibrary("zipfile","Manipulasi file ZIP (stdlib)","System"),
    PyLibrary("tarfile","Manipulasi file TAR (stdlib)","System"),
    PyLibrary("py7zr","Baca/tulis arsip 7-Zip","System"),
    PyLibrary("glob","Pattern matching path file (stdlib)","System"),
    PyLibrary("tempfile","File sementara (stdlib)","System"),
    // Config & Parsing
    PyLibrary("python-dotenv","Load .env ke environment variable","Config"),
    PyLibrary("configparser","Parser file INI/CFG (stdlib)","Config"),
    PyLibrary("toml","Parser TOML","Config"),
    PyLibrary("pyyaml","Parser YAML","Config"),
    PyLibrary("jsonschema","Validasi JSON Schema","Config"),
    PyLibrary("marshmallow","Serialisasi/deserialisasi objek","Config"),
    PyLibrary("cerberus","Validasi data dictionary","Config"),
    PyLibrary("dynaconf","Manajemen konfigurasi dinamis","Config"),
    PyLibrary("environs","Parsing environment variables","Config"),
    PyLibrary("python-decouple","Separasi konfigurasi dari kode","Config"),
    // API & Cloud
    PyLibrary("boto3","AWS SDK untuk Python","Cloud"),
    PyLibrary("google-cloud-storage","Google Cloud Storage","Cloud"),
    PyLibrary("azure-storage-blob","Azure Blob Storage","Cloud"),
    PyLibrary("firebase-admin","Firebase Admin SDK","Cloud"),
    PyLibrary("stripe","API pembayaran Stripe","Cloud"),
    PyLibrary("twilio","SMS dan komunikasi Twilio","Cloud"),
    PyLibrary("sendgrid","Email via SendGrid","Cloud"),
    PyLibrary("slack-sdk","Slack API SDK","Cloud"),
    PyLibrary("discord.py","Bot Discord","Cloud"),
    PyLibrary("python-telegram-bot","Bot Telegram","Cloud"),
    // Async
    PyLibrary("asyncio","Framework async (stdlib)","Async"),
    PyLibrary("anyio","Async library agnostik","Async"),
    PyLibrary("trio","Async framework ramah","Async"),
    PyLibrary("curio","Library async berbasis coroutine","Async"),
    PyLibrary("aiomysql","Driver MySQL async","Async"),
    PyLibrary("aiofiles","File I/O async","Async"),
    PyLibrary("aioboto3","AWS SDK async","Async"),
    PyLibrary("aioredis","Redis client async","Async"),
    PyLibrary("httpcore","HTTP low-level async","Async"),
    PyLibrary("starlette","ASGI toolkit minimal","Async"),
)

@Composable
fun PythonLibraryScreen() {
    val context     = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var filterCat   by remember { mutableStateOf("Semua") }
    var installing  by remember { mutableStateOf<String?>(null) }
    var installLog  by remember { mutableStateOf("") }
    var showLog     by remember { mutableStateOf(false) }

    val categories  = listOf("Semua") + pyLibraries.map { it.category }.distinct()
    val filtered    = remember(searchQuery, filterCat) {
        pyLibraries.filter { lib ->
            (filterCat == "Semua" || lib.category == filterCat) &&
            (searchQuery.isBlank() || lib.name.contains(searchQuery, true) ||
             lib.description.contains(searchQuery, true))
        }
    }

    Column(Modifier.fillMaxSize()) {
        // Header
        Surface(tonalElevation = 2.dp) {
            Column(Modifier.fillMaxWidth().padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.LibraryBooks, null,
                        tint = Color(0xFF00BCD4), modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Python Library Browser", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("${pyLibraries.size} library • ${filtered.size} ditampilkan",
                            fontSize = 11.sp,
                            color    = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                    }
                }
                OutlinedTextField(
                    value         = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder   = { Text("Cari library Python...") },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(10.dp),
                    leadingIcon   = { Icon(Icons.Rounded.Search, null, Modifier.size(18.dp)) },
                    trailingIcon  = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Rounded.Close, null, Modifier.size(16.dp))
                            }
                        }
                    }
                )
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(categories.size) { i ->
                        FilterChip(
                            selected = filterCat == categories[i],
                            onClick  = { filterCat = categories[i] },
                            label    = { Text(categories[i], fontSize = 10.sp) }
                        )
                    }
                }
            }
        }

        // Install log
        if (showLog && installLog.isNotBlank()) {
            Surface(
                color    = Color(0xFF0C0C0C),
                modifier = Modifier.fillMaxWidth().height(120.dp)
            ) {
                Column(Modifier.padding(10.dp)) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Text("Install Log", fontSize = 11.sp, color = Color(0xFFA6E3A1),
                            fontWeight = FontWeight.Bold)
                        IconButton(onClick = { showLog = false; installLog = "" },
                            Modifier.size(24.dp)) {
                            Icon(Icons.Rounded.Close, null, Modifier.size(14.dp),
                                Color(0xFF6C7086))
                        }
                    }
                    androidx.compose.foundation.rememberScrollState().let { scroll ->
                        Text(installLog,
                            fontSize   = 10.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            color      = Color(0xFFCDD6F4),
                            lineHeight = 14.sp,
                            modifier   = Modifier.verticalScroll(scroll))
                    }
                }
            }
            HorizontalDivider()
        }

        HorizontalDivider()

        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(filtered, key = { it.name }) { lib ->
                val isInstalling = installing == lib.name
                Card(shape = RoundedCornerShape(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Category color
                        Surface(
                            color  = getCategoryColor(lib.category).copy(0.12f),
                            shape  = RoundedCornerShape(8.dp),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(Modifier.fillMaxSize(), Alignment.Center) {
                                Text(lib.category.take(2).uppercase(),
                                    fontSize = 10.sp, fontWeight = FontWeight.Bold,
                                    color = getCategoryColor(lib.category))
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(lib.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.width(6.dp))
                                Surface(
                                    color = getCategoryColor(lib.category).copy(0.1f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(lib.category, fontSize = 9.sp,
                                        color = getCategoryColor(lib.category),
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                                }
                            }
                            Text(lib.description, fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(0.6f), maxLines = 1)
                        }
                        Spacer(Modifier.width(6.dp))
                        if (isInstalling) {
                            CircularProgressIndicator(Modifier.size(24.dp),
                                color = Color(0xFF00BCD4), strokeWidth = 2.dp)
                        } else {
                            OutlinedButton(
                                onClick = {
                                    installing = lib.name
                                    showLog = true
                                    installLog = "⬇️ Installing ${lib.name}...\n"
                                    Thread {
                                        try {
                                            val proc = ProcessBuilder("pip3", "install", lib.name)
                                                .redirectErrorStream(true).start()
                                            proc.inputStream.bufferedReader().forEachLine { line ->
                                                installLog += "$line\n"
                                            }
                                            proc.waitFor()
                                            installLog += if (proc.exitValue() == 0)
                                                "✅ ${lib.name} berhasil diinstall!\n"
                                            else "❌ Gagal install ${lib.name}\n"
                                        } catch (e: Exception) {
                                            installLog += "❌ pip3 tidak tersedia: ${e.message}\n"
                                            installLog += "💡 Gunakan Termux: pkg install python\n"
                                        } finally {
                                            installing = null
                                        }
                                    }.start()
                                },
                                shape  = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(30.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Icon(Icons.Rounded.Download, null, Modifier.size(12.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Install", fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun getCategoryColor(category: String): Color = when (category) {
    "Data Science"    -> Color(0xFF2196F3)
    "Machine Learning"-> Color(0xFF9C27B0)
    "Web"             -> Color(0xFFE64A19)
    "Network"         -> Color(0xFF00BCD4)
    "Database"        -> Color(0xFF4CAF50)
    "Media"           -> Color(0xFFFF9800)
    "Automation"      -> Color(0xFFFF5722)
    "Security"        -> Color(0xFFF44336)
    "Testing"         -> Color(0xFF607D8B)
    "Utility"         -> Color(0xFF795548)
    "System"          -> Color(0xFF455A64)
    "Config"          -> Color(0xFF8BC34A)
    "Cloud"           -> Color(0xFF03A9F4)
    "Async"           -> Color(0xFF00BFA5)
    else              -> Color(0xFF9E9E9E)
}