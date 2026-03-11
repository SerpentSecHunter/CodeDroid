package com.example.codedroid.terminal

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

class TerminalManager(private val context: Context? = null) {
    private val _output  = MutableSharedFlow<String>(replay = 300)
    val output: SharedFlow<String> = _output

    private val scope      = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning
    private var currentDir = "/storage/emulated/0"
    private val history    = mutableListOf<String>()

    fun startSession() {
        scope.launch {
            _output.emit("╔══════════════════════════════════════╗")
            _output.emit("║     CodeDroid Terminal v2.1          ║")
            _output.emit("║  Termux-style • CMD • PowerShell     ║")
            _output.emit("╚══════════════════════════════════════╝")
            _output.emit("ℹ️ Ketik 'help' untuk daftar perintah")
            _output.emit("---")
            _isRunning.value = true
        }
    }

    fun sendCommand(command: String) {
        scope.launch {
            val cmd = command.trim()
            history.add(cmd)
            _output.emit("$ $cmd")
            if (cmd.isBlank()) return@launch
            val result = execute(cmd)
            result.forEach { _output.emit(it) }
        }
    }

    private fun execute(cmd: String): List<String> {
        val parts = cmd.split(" ").filter { it.isNotBlank() }
        return when (parts[0].lowercase()) {
            // ── HELP ──────────────────────────────────────────────
            "help" -> listOf(
                "┌─ PERINTAH TERSEDIA ────────────────────────────",
                "│ File    : ls [path], cd <path>, pwd, mkdir, rm, cat, cp, mv",
                "│ Python  : python <file.py>, pip install <lib>",
                "│ Info    : echo, date, whoami, uname, env",
                "│ Proses  : ps, kill, clear, history",
                "│ Net     : ping <host>, curl <url>",
                "└────────────────────────────────────────────────",
                "ℹ️ Tekan tombol ▲▼ untuk navigasi riwayat perintah"
            )
            // ── LS ────────────────────────────────────────────────
            "ls","dir" -> {
                val path = if (parts.size > 1) parts[1] else currentDir
                try {
                    val f = File(path)
                    if (!f.exists()) return listOf("❌ ls: $path: No such file or directory")
                    val items = f.listFiles()?.sortedWith(compareBy({ !it.isDirectory }, { it.name })) ?: emptyList()
                    if (items.isEmpty()) return listOf("(kosong)")
                    items.map { item ->
                        val type  = if (item.isDirectory) "d" else "-"
                        val size  = if (item.isDirectory) "<DIR>" else formatSize(item.length())
                        val color = if (item.isDirectory) "📁" else "📄"
                        "$color  $type  ${size.padStart(8)}  ${item.name}"
                    }
                } catch (e: Exception) { listOf("❌ ls: ${e.message}") }
            }
            // ── CD ────────────────────────────────────────────────
            "cd" -> {
                val target = parts.getOrNull(1) ?: "/storage/emulated/0"
                val newPath = when {
                    target == "~"   -> "/storage/emulated/0"
                    target == ".."  -> File(currentDir).parent ?: currentDir
                    target.startsWith("/") -> target
                    else -> "$currentDir/$target"
                }
                val f = File(newPath)
                if (f.exists() && f.isDirectory) {
                    currentDir = f.canonicalPath
                    listOf("✅ $currentDir")
                } else listOf("❌ cd: $target: No such directory")
            }
            // ── PWD ───────────────────────────────────────────────
            "pwd" -> listOf(currentDir)
            // ── MKDIR ─────────────────────────────────────────────
            "mkdir" -> {
                val name = parts.getOrNull(1) ?: return listOf("❌ mkdir: nama folder diperlukan")
                val f    = File(currentDir, name)
                if (f.mkdirs()) listOf("✅ Folder dibuat: ${f.absolutePath}")
                else listOf("❌ Gagal membuat folder (mungkin sudah ada / tidak ada izin)")
            }
            // ── RM ────────────────────────────────────────────────
            "rm" -> {
                val name = parts.getOrNull(1) ?: return listOf("❌ rm: nama file diperlukan")
                val f    = File(if (name.startsWith("/")) name else "$currentDir/$name")
                if (f.exists() && f.delete()) listOf("✅ Dihapus: ${f.name}")
                else listOf("❌ rm: gagal hapus ${f.name}")
            }
            // ── CAT ───────────────────────────────────────────────
            "cat","type" -> {
                val name = parts.getOrNull(1) ?: return listOf("❌ cat: nama file diperlukan")
                val f    = File(if (name.startsWith("/")) name else "$currentDir/$name")
                if (!f.exists()) return listOf("❌ cat: $name: No such file")
                if (f.length() > 50_000) return listOf("❌ File terlalu besar (>50KB). Buka di editor.")
                try { f.readLines().take(200) } catch (e: Exception) { listOf("❌ cat: ${e.message}") }
            }
            // ── ECHO ──────────────────────────────────────────────
            "echo" -> listOf(parts.drop(1).joinToString(" "))
            // ── DATE ──────────────────────────────────────────────
            "date" -> listOf(java.util.Date().toString())
            // ── WHOAMI ────────────────────────────────────────────
            "whoami" -> listOf("android (u0_a0)")
            // ── UNAME ─────────────────────────────────────────────
            "uname" -> {
                val flag = parts.getOrNull(1) ?: ""
                when (flag) {
                    "-a" -> listOf("Linux Android ${android.os.Build.VERSION.RELEASE} " +
                            "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}")
                    "-r" -> listOf(android.os.Build.VERSION.RELEASE)
                    else -> listOf("Linux")
                }
            }
            // ── CLEAR ─────────────────────────────────────────────
            "clear","cls" -> listOf("\u0000CLEAR")
            // ── ENV ───────────────────────────────────────────────
            "env" -> listOf(
                "HOME=/storage/emulated/0",
                "SHELL=bash",
                "PATH=/usr/bin:/bin:/usr/local/bin",
                "ANDROID_VERSION=${android.os.Build.VERSION.RELEASE}",
                "DEVICE=${android.os.Build.MODEL}"
            )
            // ── PYTHON ────────────────────────────────────────────
            "python","python3" -> {
                val file = parts.getOrNull(1) ?: return listOf(
                    "❌ python: nama file diperlukan",
                    "💡 Contoh: python script.py"
                )
                runPython(file)
            }
            // ── PIP ───────────────────────────────────────────────
            "pip","pip3" -> {
                val sub = parts.getOrNull(1) ?: "help"
                val pkg = parts.getOrNull(2)
                when (sub) {
                    "install" -> {
                        if (pkg == null) listOf("❌ pip install: nama package diperlukan")
                        else pipInstall(pkg)
                    }
                    "list"    -> listOf("ℹ️ pip list: tidak tersedia di lingkungan ini")
                    else      -> listOf("pip [install|list|uninstall] <package>")
                }
            }
            // ── HISTORY ───────────────────────────────────────────
            "history" -> history.takeLast(20).mapIndexed { i, h -> "${i + 1}  $h" }
            // ── CP ────────────────────────────────────────────────
            "cp" -> {
                val src = parts.getOrNull(1); val dst = parts.getOrNull(2)
                if (src == null || dst == null) return listOf("❌ cp: cp <sumber> <tujuan>")
                try {
                    val s = File(if (src.startsWith("/")) src else "$currentDir/$src")
                    val d = File(if (dst.startsWith("/")) dst else "$currentDir/$dst")
                    s.copyTo(d, overwrite = true)
                    listOf("✅ Disalin: ${s.name} → ${d.absolutePath}")
                } catch (e: Exception) { listOf("❌ cp: ${e.message}") }
            }
            // ── MV ────────────────────────────────────────────────
            "mv" -> {
                val src = parts.getOrNull(1); val dst = parts.getOrNull(2)
                if (src == null || dst == null) return listOf("❌ mv: mv <sumber> <tujuan>")
                try {
                    val s = File(if (src.startsWith("/")) src else "$currentDir/$src")
                    val d = File(if (dst.startsWith("/")) dst else "$currentDir/$dst")
                    s.copyTo(d, overwrite = true); s.delete()
                    listOf("✅ Dipindahkan: ${s.name} → ${d.absolutePath}")
                } catch (e: Exception) { listOf("❌ mv: ${e.message}") }
            }
            // ── PING ──────────────────────────────────────────────
            "ping" -> {
                val host = parts.getOrNull(1) ?: return listOf("❌ ping: host diperlukan")
                try {
                    val addr = java.net.InetAddress.getByName(host)
                    val ms   = measureMs { addr.isReachable(3000) }
                    listOf("PING $host (${addr.hostAddress})",
                        "Response time: ${ms}ms",
                        "✅ Host reachable")
                } catch (e: Exception) { listOf("❌ ping: $host unreachable — ${e.message}") }
            }
            // ── DEFAULT — try system shell ─────────────────────────
            else -> {
                try {
                    val pb = ProcessBuilder(parts).apply {
                        directory(File(currentDir))
                        redirectErrorStream(true)
                    }
                    val proc = pb.start()
                    val out  = proc.inputStream.bufferedReader().readLines()
                    proc.waitFor()
                    out.ifEmpty { listOf("(no output)") }
                } catch (e: Exception) {
                    listOf(
                        "❌ ${parts[0]}: command not found",
                        "💡 Ketik 'help' untuk melihat perintah yang tersedia"
                    )
                }
            }
        }
    }

    private fun runPython(fileName: String): List<String> {
        val file = File(if (fileName.startsWith("/")) fileName else "$currentDir/$fileName")
        if (!file.exists()) return listOf(
            "❌ python: $fileName: File tidak ditemukan",
            "💡 Pastikan path file benar. Gunakan 'ls' untuk melihat isi folder."
        )
        return try {
            // Cek apakah ada python di sistem
            val checkPy = ProcessBuilder("python3", "--version").redirectErrorStream(true).start()
            val pyVer   = checkPy.inputStream.bufferedReader().readText().trim()
            checkPy.waitFor()

            if (checkPy.exitValue() != 0) {
                return listOf(
                    "❌ Python tidak tersedia di perangkat ini",
                    "💡 Android tidak mendukung Python secara native.",
                    "💡 Gunakan Termux (gratis di F-Droid) untuk instalasi Python penuh:",
                    "   1. Install Termux dari F-Droid",
                    "   2. Buka Termux → pkg install python",
                    "   3. Jalankan script di Termux"
                )
            }
            val result = mutableListOf("▶ Menjalankan: $fileName ($pyVer)")
            val pb     = ProcessBuilder("python3", file.absolutePath).apply {
                directory(file.parentFile)
                redirectErrorStream(true)
            }
            val proc   = pb.start()
            val output = proc.inputStream.bufferedReader().readLines()
            proc.waitFor()
            result += output
            if (proc.exitValue() == 0) result += listOf("✅ Selesai (exit code 0)")
            else result += listOf("❌ Keluar dengan exit code: ${proc.exitValue()}")
            result
        } catch (e: Exception) {
            listOf(
                "❌ Gagal menjalankan Python: ${e.message}",
                "💡 Python mungkin tidak tersedia. Gunakan Termux untuk Python penuh."
            )
        }
    }

    private fun pipInstall(pkg: String): List<String> {
        return try {
            val checkPip = ProcessBuilder("pip3", "show", pkg).redirectErrorStream(true).start()
            checkPip.waitFor()
            if (checkPip.exitValue() == 0) {
                return listOf("ℹ️ Package '$pkg' sudah terinstall")
            }
            val result = mutableListOf("⬇️ Menginstall $pkg...")
            val pb     = ProcessBuilder("pip3", "install", pkg).redirectErrorStream(true)
            val proc   = pb.start()
            proc.inputStream.bufferedReader().forEachLine { result += it }
            proc.waitFor()
            if (proc.exitValue() == 0) result += "✅ $pkg berhasil diinstall"
            else {
                result += "❌ Gagal install $pkg"
                result += "💡 Penyebab umum:"
                result += "   - Tidak ada koneksi internet"
                result += "   - Nama package salah (cek di pypi.org)"
                result += "   - Python/pip tidak tersedia"
            }
            result
        } catch (e: Exception) {
            listOf(
                "❌ pip tidak tersedia: ${e.message}",
                "💡 Instal Termux dari F-Droid lalu jalankan: pkg install python"
            )
        }
    }

    fun stop() { _isRunning.value = false }
    private fun formatSize(bytes: Long): String = when {
        bytes < 1024      -> "${bytes}B"
        bytes < 1_048_576 -> "${"%.1f".format(bytes / 1024.0)}K"
        else              -> "${"%.1f".format(bytes / 1_048_576.0)}M"
    }

    private fun measureMs(block: () -> Unit): Long {
        val t = System.currentTimeMillis()
        block()
        return System.currentTimeMillis() - t
    }
}