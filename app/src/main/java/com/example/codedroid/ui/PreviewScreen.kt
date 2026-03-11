package com.example.codedroid.ui

import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.codedroid.terminal.TerminalManager
import java.io.File

@Composable
fun PreviewScreen(
    content        : String,
    language       : String,
    fileName       : String,
    terminalManager: TerminalManager,
    currentFilePath: String = ""
) {
    var previewMode    by remember { mutableStateOf("auto") }
    var isRunning      by remember { mutableStateOf(false) }
    var runOutput      by remember { mutableStateOf<List<String>>(emptyList()) }
    val context        = LocalContext.current

    // Detect mode
    val detectedMode = remember(language) {
        when (language.lowercase()) {
            "html","htm"             -> "web"
            "python","py"            -> "python"
            "javascript","js"        -> "web-js"
            "css"                    -> "web"
            "markdown","md"          -> "web"
            else                     -> "unsupported"
        }
    }
    val activeMode = if (previewMode == "auto") detectedMode else previewMode

    Column(Modifier.fillMaxSize()) {
        // Preview toolbar
        Surface(tonalElevation = 2.dp) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.Visibility, null,
                    tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Preview", fontSize = 15.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f))
                Text(fileName, fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                Spacer(Modifier.width(8.dp))
                // Mode chips
                listOf("web" to "🌐", "python" to "🐍").forEach { (mode, icon) ->
                    FilterChip(
                        selected = activeMode == mode || (activeMode == "web-js" && mode == "web"),
                        onClick  = { previewMode = mode },
                        label    = { Text(icon + " " + mode.replaceFirstChar { it.uppercase() }, fontSize = 10.sp) },
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
            }
        }
        HorizontalDivider()

        when {
            activeMode == "web" || activeMode == "web-js" -> {
                WebPreview(
                    content  = buildHtmlContent(content, language),
                    modifier = Modifier.fillMaxSize()
                )
            }
            activeMode == "python" -> {
                PythonRunner(
                    content     = content,
                    fileName    = fileName,
                    filePath    = currentFilePath,
                    isRunning   = isRunning,
                    output      = runOutput,
                    onRun       = {
                        isRunning = true
                        runOutput = emptyList()
                        runPythonCode(context, content, fileName, currentFilePath) { lines ->
                            runOutput = lines
                            isRunning = false
                        }
                    }
                )
            }
            else -> {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.Code, null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(0.3f),
                            modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("Preview tidak tersedia untuk $language",
                            color = MaterialTheme.colorScheme.onSurface.copy(0.4f))
                        Spacer(Modifier.height(8.dp))
                        Text("Tersedia: HTML, CSS, JavaScript, Python, Markdown",
                            fontSize = 11.sp,
                            color    = MaterialTheme.colorScheme.onSurface.copy(0.3f))
                    }
                }
            }
        }
    }
}

// ── Web Preview ───────────────────────────────────────────────────────────────

@Composable
private fun WebPreview(content: String, modifier: Modifier = Modifier) {
    AndroidView(
        factory  = { ctx ->
            WebView(ctx).apply {
                settings.apply {
                    javaScriptEnabled      = true
                    domStorageEnabled      = true
                    allowFileAccess        = true
                    mixedContentMode       = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    useWideViewPort        = true
                    loadWithOverviewMode   = true
                    setSupportZoom(true)
                    builtInZoomControls    = true
                    displayZoomControls    = false
                }
                webViewClient = WebViewClient()
            }
        },
        update   = { wv -> wv.loadDataWithBaseURL(null, content, "text/html", "UTF-8", null) },
        modifier = modifier
    )
}

// ── Python Runner ─────────────────────────────────────────────────────────────

@Composable
private fun PythonRunner(
    content  : String,
    fileName : String,
    filePath : String,
    isRunning: Boolean,
    output   : List<String>,
    onRun    : () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        // Run toolbar
        Surface(color = Color(0xFF0D1117)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.Computer, null, tint = Color(0xFF00FF41),
                    modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Python Runner", fontSize = 13.sp, color = Color(0xFFA6E3A1),
                    fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                if (isRunning) {
                    CircularProgressIndicator(
                        modifier  = Modifier.size(20.dp),
                        color     = Color(0xFF00FF41),
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Berjalan...", fontSize = 12.sp, color = Color(0xFFA6E3A1))
                } else {
                    FilledIconButton(
                        onClick = onRun,
                        modifier = Modifier.size(36.dp),
                        colors   = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color(0xFF00FF41).copy(0.2f),
                            contentColor   = Color(0xFF00FF41)
                        )
                    ) { Icon(Icons.Rounded.PlayArrow, "Run", Modifier.size(20.dp)) }
                }
            }
        }
        // Output console
        Column(
            modifier = Modifier.fillMaxSize().background(Color(0xFF0C0C0C))
                .padding(12.dp)
        ) {
            if (output.isEmpty() && !isRunning) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("▶ Tekan tombol Run untuk menjalankan kode",
                            color = Color(0xFF4C566A), fontSize = 13.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("Library yang tidak ada akan otomatis diinstall",
                            color = Color(0xFF3B4252), fontSize = 11.sp)
                    }
                }
            } else {
                output.forEach { line ->
                    val color = when {
                        line.startsWith("❌") || line.lowercase().contains("error") -> Color(0xFFF38BA8)
                        line.startsWith("✅")                                       -> Color(0xFFA6E3A1)
                        line.startsWith("⬇️") || line.startsWith("⚠️")            -> Color(0xFFF9E2AF)
                        line.startsWith("▶")                                        -> Color(0xFF89B4FA)
                        line.startsWith("💡")                                       -> Color(0xFFF9E2AF)
                        else                                                         -> Color(0xFFCDD6F4)
                    }
                    Text(line, color = color, fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace, lineHeight = 18.sp,
                        modifier   = Modifier.padding(vertical = 1.dp))
                }
            }
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun buildHtmlContent(content: String, language: String): String {
    return when (language.lowercase()) {
        "html","htm" -> content
        "css" -> """
            <!DOCTYPE html><html><head>
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <style>$content</style>
            </head><body>
            <h1>CSS Preview</h1><p>Lorem ipsum dolor sit amet</p>
            <button>Button</button><div class="box">Box</div>
            </body></html>
        """.trimIndent()
        "javascript","js" -> """
            <!DOCTYPE html><html><head>
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <style>body{font-family:monospace;background:#0d1117;color:#e6edf3;padding:16px}
            #output{background:#161b22;padding:12px;border-radius:8px;min-height:100px;white-space:pre-wrap}
            </style></head><body>
            <h3 style="color:#64b5f6">JavaScript Output</h3>
            <div id="output"></div>
            <script>
            const out = document.getElementById('output');
            const origLog = console.log;
            console.log = (...args) => {
                out.innerHTML += args.join(' ') + '\n';
                origLog(...args);
            };
            try { $content }
            catch(e) { out.innerHTML += '❌ ' + e.message; }
            </script></body></html>
        """.trimIndent()
        "markdown","md" -> {
            val html = content
                .replace(Regex("^### (.+)$", RegexOption.MULTILINE), "<h3>$1</h3>")
                .replace(Regex("^## (.+)$", RegexOption.MULTILINE), "<h2>$1</h2>")
                .replace(Regex("^# (.+)$", RegexOption.MULTILINE), "<h1>$1</h1>")
                .replace(Regex("\\*\\*(.+?)\\*\\*"), "<strong>$1</strong>")
                .replace(Regex("\\*(.+?)\\*"), "<em>$1</em>")
                .replace(Regex("`(.+?)`"), "<code>$1</code>")
                .replace(Regex("^- (.+)$", RegexOption.MULTILINE), "<li>$1</li>")
                .replace("\n", "<br>")
            """
            <!DOCTYPE html><html><head>
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <style>body{font-family:sans-serif;max-width:800px;margin:0 auto;padding:16px;line-height:1.6}
            code{background:#f0f0f0;padding:2px 4px;border-radius:4px;font-family:monospace}
            h1,h2,h3{border-bottom:1px solid #eee;padding-bottom:4px}
            </style></head><body>$html</body></html>
            """.trimIndent()
        }
        else -> "<html><body><pre style='font-family:monospace'>${content.replace("<","<")}</pre></body></html>"
    }
}

private fun runPythonCode(
    context    : android.content.Context,
    content    : String,
    fileName   : String,
    existingPath: String,
    onResult   : (List<String>) -> Unit
) {
    Thread {
        val results = mutableListOf<String>()
        try {
            // Cek python
            val checkPy = ProcessBuilder("python3", "--version").redirectErrorStream(true).start()
            val pyVer   = checkPy.inputStream.bufferedReader().readText().trim()
            checkPy.waitFor()

            if (checkPy.exitValue() != 0) {
                onResult(listOf(
                    "❌ Python tidak tersedia di perangkat ini",
                    "💡 Android tidak mendukung Python secara native.",
                    "💡 Untuk menjalankan Python di Android:",
                    "   1. Install Termux dari F-Droid (bukan Play Store)",
                    "   2. Buka Termux → ketik: pkg install python",
                    "   3. Salin file ke /storage/emulated/0/",
                    "   4. Jalankan: python3 namafile.py"
                ))
                return@Thread
            }

            results += "▶ Python: $pyVer"

            // Simpan ke file sementara atau pakai yang sudah ada
            val scriptFile = if (existingPath.isNotBlank() && File(existingPath).exists()) {
                File(existingPath)
            } else {
                val tmp = File(context.cacheDir, fileName.ifBlank { "script.py" })
                tmp.writeText(content)
                tmp
            }

            results += "▶ File: ${scriptFile.absolutePath}"
            results += "--- OUTPUT ---"

            // Cek import dan auto-install
            val importRegex = Regex("^(?:import|from)\\s+([\\w.]+)", RegexOption.MULTILINE)
            val stdlibs = setOf("os","sys","re","json","math","time","datetime","random",
                "collections","itertools","functools","pathlib","io","abc","typing",
                "string","struct","copy","enum","dataclasses","threading","subprocess",
                "socket","http","urllib","hashlib","base64","csv","sqlite3","logging","unittest")
            val imports = importRegex.findAll(content)
                .map { it.groupValues[1].split(".").first() }
                .filter { it !in stdlibs }
                .distinct()

            imports.forEach { pkg ->
                val checkPkg = ProcessBuilder("pip3","show",pkg).redirectErrorStream(true).start()
                checkPkg.waitFor()
                if (checkPkg.exitValue() != 0) {
                    results += "⬇️ Auto-install: $pkg..."
                    val installProc = ProcessBuilder("pip3","install",pkg)
                        .redirectErrorStream(true).start()
                    val installOut  = installProc.inputStream.bufferedReader().readLines()
                    installProc.waitFor()
                    if (installProc.exitValue() == 0) {
                        results += "✅ $pkg berhasil diinstall"
                    } else {
                        results += "❌ Gagal install $pkg:"
                        results += installOut.take(5)
                        results += "💡 Kemungkinan penyebab:"
                        results += "   - Tidak ada koneksi internet"
                        results += "   - Nama package salah (cek di pypi.org)"
                        results += "   - Coba: pip install $pkg --upgrade"
                    }
                }
            }

            // Run
            val proc = ProcessBuilder("python3", scriptFile.absolutePath)
                .apply { redirectErrorStream(true); directory(scriptFile.parentFile) }
                .start()

            val output = proc.inputStream.bufferedReader().readLines()
            proc.waitFor()
            results += output

            if (proc.exitValue() == 0) results += "✅ Selesai (exit 0)"
            else {
                results += "❌ Keluar dengan exit code: ${proc.exitValue()}"
                // Tambahkan hint berdasarkan error terakhir
                val lastErr = output.lastOrNull { it.contains("Error", ignoreCase = true) } ?: ""
                if (lastErr.isNotBlank()) {
                    val hint = getErrorHintStatic(lastErr)
                    if (hint != null) results += "💡 $hint"
                }
            }
        } catch (e: Exception) {
            results += "❌ ${e.message}"
        }
        onResult(results)
    }.start()
}

private fun getErrorHintStatic(error: String): String? {
    val e = error.lowercase()
    return when {
        e.contains("modulenotfounderror") || e.contains("no module named") ->
            "Library tidak ada. Cek nama import dan pastikan sudah diinstall."
        e.contains("syntaxerror")      -> "Syntax salah. Periksa tanda kurung, titik dua, atau indentasi."
        e.contains("indentationerror") -> "Indentasi salah. Gunakan 4 spasi konsisten."
        e.contains("nameerror")        -> "Variabel belum didefinisikan."
        e.contains("typeerror")        -> "Tipe data tidak sesuai."
        e.contains("zerodivisionerror") -> "Pembagian dengan nol!"
        e.contains("indexerror")       -> "Index list di luar batas."
        e.contains("keyerror")         -> "Key tidak ada di dictionary."
        else                            -> null
    }
}