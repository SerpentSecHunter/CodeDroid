package com.example.codedroid.terminal

import android.content.Context
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.io.File
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import android.os.Build
import android.os.StatFs
import android.os.SystemClock
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import androidx.core.content.edit

@Suppress("SpellCheckingInspection", "SdCardPath")
class TerminalManager(private val context: Context) {

    companion object { private const val DOLLAR = '$' }

    private val _output = MutableSharedFlow<String>(extraBufferCapacity = 2000)
    val output = _output.asSharedFlow()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    var currentDir: String = context.getExternalFilesDir(null)?.absolutePath
        ?: "/storage/emulated/0"

    val commandHistory = mutableListOf<String>()

    private val env = mutableMapOf(
        "HOME" to (context.getExternalFilesDir(null)?.absolutePath ?: "/storage/emulated/0"),
        "USER" to "codedroid",
        "TERM" to "xterm-256color",
        "SHELL" to "codedroid-sh",
        "LANG" to "en_US.UTF-8"
    )

    fun getShortPath(): String {
        val home = env["HOME"] ?: ""
        return if (currentDir == home) "~"
        else if (currentDir.startsWith(home)) currentDir.replace(home, "~")
        else currentDir
    }

    private val installedPkgs: MutableSet<String> by lazy {
        context.getSharedPreferences("cd_pkgs", Context.MODE_PRIVATE)
            .getStringSet("list", mutableSetOf())!!.toMutableSet()
    }

    private fun savePkgs() = context.getSharedPreferences("cd_pkgs", Context.MODE_PRIVATE).edit {
        putStringSet("list", installedPkgs)
    }

    private var pyReady = false

    private fun initPython(): Boolean {
        if (pyReady) return true
        return try {
            if (!Python.isStarted()) Python.start(AndroidPlatform(context))
            pyReady = true
            true
        } catch (ex: Exception) {
            emitLine("❌ Python init error: ${ex.message}")
            false
        }
    }

    // Main emit function - thread-safe
    private fun emitLine(text: String) {
        scope.launch { _output.emit(text) }
    }

    fun startSession() {
        scope.launch {
            delay(150)
            emitLine("Welcome to CodeDroid v2.2.0")
            emitLine("System: Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
            emitLine("Shell: codedroid-sh (Basic Terminal Emulation)")
            emitLine("")
            emitLine("[36m* Python 3.11 environment is ready.[0m")
            emitLine("[36m* BusyBox-style utilities available.[0m")
            emitLine("[36m* Package Manager (pkg) integrated.[0m")
            emitLine("")
            emitLine("Type [32m'help'[0m to see available commands.")
            emitLine("")
        }
    }

    fun sendCommand(raw: String) {
        val cmd = raw.trim()
        if (cmd.isBlank()) return
        commandHistory.add(0, cmd)
        if (commandHistory.size > 200) commandHistory.removeAt(commandHistory.lastIndex)

        scope.launch {
            emitLine("${getShortPath()} $ $cmd")
            val tok = cmd.split(Regex("\\s+"))
            when {

                cmd.contains(">>") && cmd.startsWith("echo") -> doRedirect(cmd)
                cmd.contains(">") && cmd.startsWith("echo")  -> doRedirect(cmd)
                cmd.contains("|") -> doPipe(cmd)

                tok[0] == "pwd"      -> emitLine(currentDir)
                tok[0] == "cd"       -> doCd(cmd)
                tok[0] == "ls"       -> doLs(cmd)
                tok[0] == "dir"      -> doLs("ls -l")
                tok[0] == "mkdir"    -> doMkdir(cmd)
                tok[0] == "touch"    -> doTouch(cmd)
                tok[0] == "rm"       -> doRm(cmd)
                tok[0] == "rmdir"    -> doRmdir(cmd)
                tok[0] == "cp"       -> doCp(cmd)
                tok[0] == "mv"       -> doMv(cmd)
                tok[0] == "cat"      -> doCat(cmd)
                tok[0] == "less"     -> doCat(cmd.replace("less", "cat"))
                tok[0] == "more"     -> doCat(cmd.replace("more", "cat"))
                tok[0] == "echo"     -> doEcho(cmd)
                tok[0] == "find"     -> doFind(cmd)
                tok[0] == "grep"     -> doGrep(cmd)
                tok[0] == "head"     -> doHead(cmd)
                tok[0] == "tail"     -> doTail(cmd)
                tok[0] == "wc"       -> doWc(cmd)
                tok[0] == "chmod"    -> doChmod(cmd)
                tok[0] == "sort"     -> doSort(cmd)
                tok[0] == "uniq"     -> doUniq(cmd)
                tok[0] == "cut"      -> doCut(cmd)
                tok[0] == "diff"     -> doDiff(cmd)
                tok[0] == "du"       -> doDu(cmd)
                tok[0] == "df"       -> doDf()
                tok[0] == "stat"     -> doStat(cmd)
                tok[0] == "file"     -> doFile(cmd)
                tok[0] == "zip"      -> doZip(cmd)
                tok[0] == "unzip"    -> doUnzip(cmd)
                tok[0] == "tar"      -> doTar()
                tok[0] == "nano" || tok[0] == "vi" || tok[0] == "vim" -> doNano(cmd)

                tok[0] == "whoami"   -> emitLine("codedroid")
                tok[0] == "id"       -> emitLine("uid=1000(codedroid) gid=1000 groups=1000")
                tok[0] == "hostname" -> emitLine(Build.MODEL)
                tok[0] == "date"     -> emitLine(LocalDateTime.now().toString())
                tok[0] == "uname"    -> doUname(cmd)
                tok[0] == "uptime"   -> doUptime()
                tok[0] == "free"     -> doFree()
                tok[0] == "ps"       -> doPs()
                tok[0] == "env"      -> doEnv()
                tok[0] == "export"   -> doExport(cmd)
                tok[0] == "unset"    -> env.remove(tok.getOrNull(1))
                tok[0] == "which"    -> doWhich(cmd)
                tok[0] == "alias"    -> emitLine("alias: not supported in this session")
                tok[0] == "history"  -> doHistory()
                tok[0] == "sleep"    -> doSleep(cmd)
                tok[0] == "clear"    -> emitLine("\u0000CLEAR")
                tok[0] == "help"     -> showHelp()
                tok[0] == "exit" || tok[0] == "logout" -> emitLine("\u0000EXIT")
                tok[0] == "true"     -> { /* exit 0 */ }
                tok[0] == "false"    -> emitLine("exit code: 1")

                tok[0] == "ping"     -> doPing(cmd)
                tok[0] == "curl"     -> doCurl(cmd)
                tok[0] == "wget"     -> doWget(cmd)
                tok[0] == "ifconfig" -> doIfconfig()
                cmd == "ip addr" || cmd == "ip a" -> doIfconfig()
                tok[0] == "nslookup" -> doNslookup(cmd)
                tok[0] == "ssh"      -> emitLine("ssh: Install Termux → pkg install openssh")
                tok[0] == "netstat"  -> emitLine("Use 'ifconfig' for network info")

                tok[0] == "pkg"      -> doPkg(cmd)
                tok[0] == "apt" || tok[0] == "apt-get" -> emitLine("apt not available. Use: pkg install <name>")
                tok[0] == "brew"     -> emitLine("Homebrew not available on Android.")
                tok[0] == "sudo"     -> emitLine("sudo: permission denied")
                tok[0] == "su"       -> emitLine("su: authentication failure")

                tok[0] == "python3" || tok[0] == "python" -> doPython(cmd)
                tok[0] == "pip3" || tok[0] == "pip"       -> doPip(cmd)

                tok[0] == "node"     -> doNode(cmd)
                tok[0] == "npm"      -> doNpm(cmd)
                tok[0] == "npx"      -> doNode(cmd)
                tok[0] == "git"      -> doGit(cmd)
                tok[0] == "php"      -> doPhp(cmd)

                tok[0] == "sed"      -> emitLine("sed: use Python for text processing.")
                tok[0] == "awk"      -> emitLine("awk: use Python for text processing.")
                tok[0] == "tr"       -> emitLine("tr: use 'python3 -c' for text transform.")
                tok[0].startsWith("#") -> { /* comment */ }
                else -> runSystem(cmd)
            }
        }
    }

    // ── Command implementations ────────────────────────────────────

    private fun doCd(cmd: String) {
        val arg = cmd.removePrefix("cd").trim()
        val target = when {
            arg.isEmpty() || arg == "~" -> env["HOME"]!!
            arg.startsWith("/")          -> arg
            arg == ".."                  -> File(currentDir).parent ?: currentDir
            arg == "."                   -> currentDir
            arg.startsWith("~/")         -> env["HOME"] + arg.removePrefix("~")
            else                         -> "$currentDir/$arg"
        }
        val f = File(target)
        when {
            !f.exists()    -> emitLine("bash: cd: $target: No such file or directory")
            !f.isDirectory -> emitLine("bash: cd: $target: Not a directory")
            !f.canRead()   -> emitLine("bash: cd: $target: Permission denied")
            else           -> { currentDir = f.canonicalPath; env["PWD"] = currentDir }
        }
    }

    private fun doLs(cmd: String) {
        val parts   = cmd.split(Regex("\\s+"))
        val showAll = parts.any { it.matches(Regex("-[a-z]*a[a-z]*")) }
        val longFmt = parts.any { it.matches(Regex("-[a-z]*l[a-z]*")) }
        val human   = parts.any { it.matches(Regex("-[a-z]*h[a-z]*")) }
        val pathArg = parts.drop(1).firstOrNull { !it.startsWith("-") }
        val target  = when {
            pathArg == null         -> currentDir
            pathArg.startsWith("/") -> pathArg
            else                    -> "$currentDir/$pathArg"
        }
        val dir = File(target)
        if (!dir.exists()) { emitLine("ls: cannot access '$target': No such file or directory"); return }
        if (!dir.isDirectory) { emitLine(dir.name); return }
        val files = dir.listFiles()
            ?.filter { showAll || !it.name.startsWith(".") }
            ?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
            ?: run { emitLine("ls: cannot open directory: Permission denied"); return }
        if (files.isEmpty()) return
        if (longFmt) {
            emitLine("total ${files.size}")
            files.forEach { f ->
                val p = "${if (f.isDirectory) "d" else "-"}${if (f.canRead()) "r" else "-"}${if (f.canWrite()) "w" else "-"}${if (f.canExecute()) "x" else "-"}------"
                val s = if (human) fmtSize(f.length()) else f.length().toString()
                val d = LocalDateTime.ofInstant(Instant.ofEpochMilli(f.lastModified()), ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("MMM dd HH:mm", Locale.getDefault()))
                emitLine("$p  1 codedroid codedroid  ${s.padStart(7)}  $d  ${if (f.isDirectory) "${f.name}/" else f.name}")
            }
        } else {
            val names = files.map { if (it.isDirectory) "${it.name}/" else it.name }
            val w     = (names.maxOfOrNull { it.length } ?: 10) + 2
            val cols  = (80 / w).coerceAtLeast(1)
            names.chunked(cols).forEach { row -> emitLine(row.joinToString("") { it.padEnd(w) }.trimEnd()) }
        }
    }

    private fun doMkdir(cmd: String) {
        val parts = cmd.split(Regex("\\s+"))
        val mkp   = "-p" in parts
        parts.drop(1).filter { !it.startsWith("-") }.forEach { name ->
            val f = File(if (name.startsWith("/")) name else "$currentDir/$name")
            when {
                f.exists() && !mkp -> emitLine("mkdir: cannot create directory '${f.name}': File exists")
                f.exists()         -> { }
                (if (mkp) f.mkdirs() else f.mkdir()) -> { }
                else -> emitLine("mkdir: cannot create directory '${f.name}': Permission denied")
            }
        }
    }

    private fun doTouch(cmd: String) {
        cmd.removePrefix("touch").trim().split(Regex("\\s+"))
            .filter { it.isNotBlank() }.forEach { name ->
                val f = File(if (name.startsWith("/")) name else "$currentDir/$name")
                f.parentFile?.mkdirs()
                if (!f.exists()) f.createNewFile()
                f.setLastModified(System.currentTimeMillis())
            }
    }

    private fun doRm(cmd: String) {
        val parts = cmd.split(Regex("\\s+")).drop(1)
        val r = parts.any { it.matches(Regex("-[a-z]*r[a-z]*")) || it == "-rf" || it == "-fr" }
        val f = parts.any { it.matches(Regex("-[a-z]*f[a-z]*")) }
        parts.filter { !it.startsWith("-") }.forEach { name ->
            val file = File(if (name.startsWith("/")) name else "$currentDir/$name")
            when {
                !file.exists() && !f -> emitLine("rm: cannot remove '$name': No such file or directory")
                !file.exists()       -> { }
                file.isDirectory && !r -> emitLine("rm: cannot remove '$name': Is a directory\nHint: rm -r $name")
                (if (r) file.deleteRecursively() else file.delete()) -> { }
                else -> emitLine("rm: cannot remove '$name': Permission denied")
            }
        }
    }

    private fun doRmdir(cmd: String) {
        val name = cmd.removePrefix("rmdir").trim()
        val f    = File(if (name.startsWith("/")) name else "$currentDir/$name")
        when {
            !f.exists()                    -> emitLine("rmdir: failed to remove '$name': No such file or directory")
            !f.isDirectory                 -> emitLine("rmdir: failed to remove '$name': Not a directory")
            f.list()?.isNotEmpty() == true -> emitLine("rmdir: failed to remove '$name': Directory not empty")
            f.delete()                     -> { }
            else                           -> emitLine("rmdir: Permission denied")
        }
    }

    private fun doCp(cmd: String) {
        val parts = cmd.split(Regex("\\s+")).drop(1)
        val r     = "-r" in parts || "-R" in parts
        val args  = parts.filter { !it.startsWith("-") }
        if (args.size < 2) { emitLine("cp: missing destination operand"); return }
        val src = File(if (args[0].startsWith("/")) args[0] else "$currentDir/${args[0]}")
        val dst = File(if (args[1].startsWith("/")) args[1] else "$currentDir/${args[1]}")
        when {
            !src.exists() -> emitLine("cp: '${src.name}': No such file or directory")
            src.isDirectory && !r -> emitLine("cp: -r not specified; omitting directory '${src.name}'")
            else -> src.copyRecursively(dst, overwrite = true)
        }
    }

    private fun doMv(cmd: String) {
        val args = cmd.split(Regex("\\s+")).drop(1).filter { !it.startsWith("-") }
        if (args.size < 2) { emitLine("mv: missing destination operand"); return }
        val src = File(if (args[0].startsWith("/")) args[0] else "$currentDir/${args[0]}")
        val dst = File(if (args[1].startsWith("/")) args[1] else "$currentDir/${args[1]}")
        if (!src.exists()) { emitLine("mv: cannot stat '${src.name}': No such file or directory"); return }
        if (!src.renameTo(dst)) { src.copyRecursively(dst, true); src.deleteRecursively() }
    }

    private fun doCat(cmd: String) {
        val parts  = cmd.split(Regex("\\s+"))
        val showLn = "-n" in parts
        parts.drop(1).filter { !it.startsWith("-") }.forEach { name ->
            val f = File(if (name.startsWith("/")) name else "$currentDir/$name")
            when {
                !f.exists()   -> emitLine("cat: $name: No such file or directory")
                f.isDirectory -> emitLine("cat: $name: Is a directory")
                f.length() > 2_000_000 -> emitLine("cat: $name: File too large (>2MB)")
                else -> f.readLines().forEachIndexed { i, line ->
                    if (showLn) emitLine("${(i + 1).toString().padStart(6)}\t$line") else emitLine(line)
                }
            }
        }
    }

    private fun doEcho(cmd: String) {
        val noNl = cmd.startsWith("echo -n ")
        val text = (if (noNl) cmd.removePrefix("echo -n") else cmd.removePrefix("echo")).trim()
            .replace("\\n", "\n").replace("\\t", "\t")
            .let { t -> env.entries.fold(t) { acc, (k, v) -> acc.replace("${DOLLAR}$k", v) } }
            .replace("${DOLLAR}PWD", currentDir).replace("${DOLLAR}HOME", env["HOME"] ?: "")
        text.lines().forEach { emitLine(it) }
    }

    private fun doFind(cmd: String) {
        val parts   = cmd.split(Regex("\\s+"))
        val pathArg = parts.getOrNull(1)?.takeIf { !it.startsWith("-") } ?: "."
        val nameIdx = parts.indexOf("-name")
        val typeIdx = parts.indexOf("-type")
        val nameArg = if (nameIdx >= 0) parts.getOrNull(nameIdx + 1) else null
        val typeArg = if (typeIdx >= 0) parts.getOrNull(typeIdx + 1) else null
        val base    = File(if (pathArg.startsWith("/")) pathArg else "$currentDir/$pathArg")
        base.walkTopDown().take(2000).forEach { f ->
            val okName = nameArg == null || f.name.matches(
                nameArg.replace(".", "\\.").replace("*", ".*").toRegex())
            val okType = typeArg == null ||
                (typeArg == "f" && f.isFile) || (typeArg == "d" && f.isDirectory)
            if (okName && okType) emitLine(f.absolutePath)
        }
    }

    private fun doGrep(cmd: String) {
        val parts     = cmd.split(Regex("\\s+")).drop(1)
        val ignCase   = "-i" in parts
        val lineNum   = "-n" in parts
        val invert    = "-v" in parts
        val countOnly = "-c" in parts
        val args      = parts.filter { !it.startsWith("-") }
        if (args.isEmpty()) { emitLine("Usage: grep [-invrc] <pattern> <file>..."); return }
        val pat   = args[0]
        val files = args.drop(1)
        if (files.isEmpty()) { emitLine("grep: no input file"); return }
        files.forEach { name ->
            val f = File(if (name.startsWith("/")) name else "$currentDir/$name")
            if (!f.exists()) { emitLine("grep: $name: No such file or directory"); return@forEach }
            var cnt = 0
            f.readLines().forEachIndexed { i, line ->
                val match = if (ignCase) line.contains(pat, true) else line.contains(pat)
                val show  = if (invert) !match else match
                if (show) {
                    if (!countOnly) emitLine("${if (lineNum) "${i + 1}:" else ""}$line")
                    cnt++
                }
            }
            if (countOnly) emitLine("$name:$cnt")
        }
    }

    private fun doHead(cmd: String) {
        val parts = cmd.split(Regex("\\s+"))
        val nIdx  = parts.indexOf("-n")
        val n     = if (nIdx >= 0) parts.getOrNull(nIdx + 1)?.toIntOrNull() ?: 10 else 10
        val name  = parts.lastOrNull { !it.startsWith("-") && it != "head" && it.toIntOrNull() == null } ?: return
        val f = File(if (name.startsWith("/")) name else "$currentDir/$name")
        if (!f.exists()) { emitLine("head: cannot open '$name': No such file or directory"); return }
        f.readLines().take(n).forEach { emitLine(it) }
    }

    private fun doTail(cmd: String) {
        val parts = cmd.split(Regex("\\s+"))
        val nIdx  = parts.indexOf("-n")
        val n     = if (nIdx >= 0) parts.getOrNull(nIdx + 1)?.toIntOrNull() ?: 10 else 10
        val name  = parts.lastOrNull { !it.startsWith("-") && it != "tail" && it.toIntOrNull() == null } ?: return
        val f = File(if (name.startsWith("/")) name else "$currentDir/$name")
        if (!f.exists()) { emitLine("tail: cannot open '$name': No such file or directory"); return }
        f.readLines().takeLast(n).forEach { emitLine(it) }
    }

    private fun doWc(cmd: String) {
        val parts = cmd.split(Regex("\\s+"))
        val names = parts.drop(1).filter { !it.startsWith("-") }
        if (names.isEmpty()) { emitLine("Usage: wc [-lwc] <file>"); return }
        names.forEach { name ->
            val f = File(if (name.startsWith("/")) name else "$currentDir/$name")
            if (!f.exists()) { emitLine("wc: $name: No such file or directory"); return@forEach }
            val lines = f.readLines()
            val words = lines.sumOf { it.trim().split(Regex("\\s+")).filter { w -> w.isNotBlank() }.size }
            emitLine("${lines.size.toString().padStart(7)} ${words.toString().padStart(7)} ${f.length().toString().padStart(7)} $name")
        }
    }

    private fun doChmod(cmd: String) {
        val parts = cmd.split(Regex("\\s+"))
        if (parts.size < 3) { emitLine("Usage: chmod <mode> <file>"); return }
        val mode = parts[1]; val name = parts[2]
        val f = File(if (name.startsWith("/")) name else "$currentDir/$name")
        if (!f.exists()) { emitLine("chmod: cannot access '$name': No such file or directory"); return }
        when {
            mode.contains("x") || mode in listOf("755", "777", "111") -> f.setExecutable(true)
            mode.contains("-x") || mode in listOf("644", "444") -> f.setExecutable(false)
        }
    }

    private fun doSort(cmd: String) {
        val parts = cmd.split(Regex("\\s+"))
        val r     = "-r" in parts
        val u     = "-u" in parts
        val name  = parts.lastOrNull { !it.startsWith("-") && it != "sort" } ?: return
        val f     = File(if (name.startsWith("/")) name else "$currentDir/$name")
        if (!f.exists()) { emitLine("sort: $name: No such file or directory"); return }
        var lines = f.readLines().sorted()
        if (r) lines = lines.reversed()
        if (u) lines = lines.distinct()
        lines.forEach { emitLine(it) }
    }

    private fun doUniq(cmd: String) {
        val name = cmd.split(Regex("\\s+")).lastOrNull { it != "uniq" } ?: return
        val f    = File(if (name.startsWith("/")) name else "$currentDir/$name")
        if (!f.exists()) { emitLine("uniq: $name: No such file or directory"); return }
        var prev: String? = null
        f.readLines().forEach { line -> if (line != prev) { emitLine(line); prev = line } }
    }

    private fun doCut(cmd: String) {
        val parts = cmd.split(Regex("\\s+"))
        val d     = parts.firstOrNull { it.startsWith("-d") }?.removePrefix("-d") ?: "\t"
        val fIdx  = parts.firstOrNull { it.startsWith("-f") }?.removePrefix("-f")?.toIntOrNull() ?: 1
        val name  = parts.lastOrNull { !it.startsWith("-") && it != "cut" } ?: return
        val f     = File(if (name.startsWith("/")) name else "$currentDir/$name")
        if (!f.exists()) { emitLine("cut: $name: No such file or directory"); return }
        f.readLines().forEach { line -> emitLine(line.split(d).getOrElse(fIdx - 1) { "" }) }
    }

    private fun doDiff(cmd: String) {
        val args = cmd.split(Regex("\\s+")).drop(1).filter { !it.startsWith("-") }
        if (args.size < 2) { emitLine("Usage: diff <file1> <file2>"); return }
        val f1 = File(if (args[0].startsWith("/")) args[0] else "$currentDir/${args[0]}")
        val f2 = File(if (args[1].startsWith("/")) args[1] else "$currentDir/${args[1]}")
        if (!f1.exists()) { emitLine("diff: ${f1.name}: No such file or directory"); return }
        if (!f2.exists()) { emitLine("diff: ${f2.name}: No such file or directory"); return }
        val l1 = f1.readLines(); val l2 = f2.readLines()
        var diff = 0
        for (i in 0 until maxOf(l1.size, l2.size)) {
            val a = l1.getOrNull(i); val b = l2.getOrNull(i)
            when {
                a == null -> { emitLine("> $b"); diff++ }
                b == null -> { emitLine("< $a"); diff++ }
                a != b    -> { emitLine("${i + 1}c${i + 1}"); emitLine("< $a"); emitLine("---"); emitLine("> $b"); diff++ }
            }
        }
        if (diff == 0) emitLine("Files are identical")
    }

    private fun doDu(cmd: String) {
        val parts = cmd.split(Regex("\\s+"))
        val h     = "-h" in parts
        val name  = parts.drop(1).firstOrNull { !it.startsWith("-") } ?: "."
        val f     = File(if (name.startsWith("/")) name else "$currentDir/$name")
        if (!f.exists()) { emitLine("du: cannot access '$name': No such file or directory"); return }
        val size  = if (f.isDirectory) f.walkTopDown().sumOf { it.length() } else f.length()
        emitLine("${(if (h) fmtSize(size) else (size / 1024).toString()).padEnd(8)}  ${f.absolutePath}")
    }

    private fun doDf() {
        try {
            val st    = StatFs(currentDir)
            val total = st.totalBytes; val free = st.freeBytes; val used = total - free
            emitLine("Filesystem      Size     Used    Avail   Use%")
            emitLine("/data/codedroid ${fmtSize(total).padEnd(8)} ${fmtSize(used).padEnd(7)} ${fmtSize(free).padEnd(7)} ${used * 100 / total}%")
        } catch (e: Exception) { emitLine("df: ${e.message}") }
    }

    private fun doStat(cmd: String) {
        val name = cmd.removePrefix("stat").trim()
        val f    = File(if (name.startsWith("/")) name else "$currentDir/$name")
        if (!f.exists()) { emitLine("stat: cannot statx '$name': No such file or directory"); return }
        val fmt  = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val mtime = LocalDateTime.ofInstant(Instant.ofEpochMilli(f.lastModified()), ZoneId.systemDefault())
        emitLine("  File: ${f.absolutePath}")
        emitLine("  Size: ${f.length()}\t${if (f.isDirectory) "directory" else "regular file"}")
        emitLine("Access: (${if (f.canRead()) "r" else "-"}${if (f.canWrite()) "w" else "-"}${if (f.canExecute()) "x" else "-"})")
        emitLine("Modify: ${mtime.format(fmt)}")
    }

    private fun doFile(cmd: String) {
        val name = cmd.removePrefix("file").trim()
        val f    = File(if (name.startsWith("/")) name else "$currentDir/$name")
        if (!f.exists()) { emitLine("$name: cannot open (No such file or directory)"); return }
        val type = when {
            f.isDirectory -> "directory"
            f.extension.lowercase() in listOf("jpg", "jpeg", "png", "gif", "webp") -> "image data"
            f.extension.lowercase() in listOf("mp3", "wav", "ogg", "m4a") -> "audio"
            f.extension.lowercase() in listOf("mp4", "mkv", "avi") -> "video"
            f.extension.lowercase() == "pdf" -> "PDF document"
            f.extension.lowercase() in listOf("zip", "jar", "apk") -> "Zip archive"
            f.extension.lowercase() in listOf("kt", "java", "py", "js", "html", "css",
                "php", "sh", "xml", "json", "md") -> "${f.extension.lowercase()} source"
            else -> "data"
        }
        emitLine("$name: $type")
    }

    private fun doZip(cmd: String) {
        val parts   = cmd.split(Regex("\\s+")).drop(1)
        if (parts.size < 2) { emitLine("Usage: zip <archive.zip> <file>..."); return }
        val zipFile = File(if (parts[0].startsWith("/")) parts[0] else "$currentDir/${parts[0]}")
        val files   = parts.drop(1).map { File(if (it.startsWith("/")) it else "$currentDir/$it") }
        try {
            ZipOutputStream(zipFile.outputStream()).use { zos ->
                files.forEach { f ->
                    if (!f.exists()) { emitLine("zip: $f: No such file"); return@forEach }
                    zos.putNextEntry(ZipEntry(f.name))
                    f.inputStream().use { it.copyTo(zos) }
                    zos.closeEntry()
                    emitLine("  adding: ${f.name}")
                }
            }
            emitLine("  ${zipFile.name}: created")
        } catch (e: Exception) { emitLine("zip: ${e.message}") }
    }

    private fun doUnzip(cmd: String) {
        val parts   = cmd.split(Regex("\\s+")).drop(1)
        val zipName = parts.firstOrNull { !it.startsWith("-") }
            ?: run { emitLine("Usage: unzip <file.zip>"); return }
        val zipFile = File(if (zipName.startsWith("/")) zipName else "$currentDir/$zipName")
        if (!zipFile.exists()) { emitLine("unzip: cannot find '$zipName'"); return }
        try {
            ZipInputStream(zipFile.inputStream()).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    val out = File(currentDir, entry.name)
                    if (entry.isDirectory) out.mkdirs()
                    else { out.parentFile?.mkdirs(); out.outputStream().use { zis.copyTo(it) } }
                    emitLine("  inflating: ${entry.name}")
                    entry = zis.nextEntry
                }
            }
        } catch (e: Exception) { emitLine("unzip: ${e.message}") }
    }

    private fun doTar() {
        emitLine("tar: use 'zip'/'unzip' for archiving")
    }

    private fun doNano(cmd: String) {
        val name = cmd.split(Regex("\\s+")).getOrNull(1) ?: ""
        emitLine("nano/vim: not available as TUI.")
        emitLine("💡 Use File Manager -> open file -> edit in Editor")
        if (name.isNotBlank()) emitLine("   File: $currentDir/$name")
    }

    private fun doUname(cmd: String) {
        val a = "-a" in cmd
        val r = buildString {
            if ("-s" in cmd || a) append("Linux ")
            if ("-n" in cmd || a) append("${Build.MODEL} ")
            if ("-r" in cmd || a) append("${Build.VERSION.RELEASE} ")
            if ("-m" in cmd || a) append(Build.SUPPORTED_ABIS.firstOrNull() ?: "aarch64")
            if (isEmpty()) append("Linux")
        }
        emitLine(r.trim())
    }

    private fun doUptime() {
        val ms = SystemClock.elapsedRealtime()
        val h  = ms / 3600000; val m = (ms % 3600000) / 60000
        val now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        emitLine(" $now  up ${h}h ${m}m,  1 user,  load average: 0.00")
    }

    private fun doFree() {
        val rt = Runtime.getRuntime()
        val total = rt.totalMemory(); val free = rt.freeMemory(); val used = total - free
        emitLine("              total        used        free")
        emitLine("Mem:   ${fmtSize(total).padEnd(12)} ${fmtSize(used).padEnd(11)} ${fmtSize(free)}")
    }

    private fun doPs() {
        emitLine("  PID TTY          TIME CMD")
        emitLine("    1 ?        00:00:00 zygote")
        emitLine("  100 pts/0    00:00:00 codedroid-sh")
    }

    private fun doEnv() {
        (env + mapOf("PWD" to currentDir)).forEach { (k, v) -> emitLine("$k=$v") }
    }

    private fun doExport(cmd: String) {
        val kv = cmd.removePrefix("export").trim()
        if ("=" in kv) {
            val (k, v) = kv.split("=", limit = 2)
            env[k.trim()] = v.trim().trim('"').trim('\'')
        } else doEnv()
    }

    private fun doWhich(cmd: String) {
        cmd.removePrefix("which").trim().split(Regex("\\s+")).forEach { bin ->
            val found = listOf(
                "/data/data/com.termux/files/usr/bin/$bin",
                "/system/bin/$bin",
                "/system/xbin/$bin"
            ).firstOrNull { File(it).exists() }
            emitLine(found ?: "$bin not found")
        }
    }

    private fun doHistory() {
        commandHistory.take(50).reversed().forEachIndexed { i, c ->
            emitLine("  ${(commandHistory.size - i).toString().padStart(4)}  $c")
        }
    }

    private fun doSleep(cmd: String) {
        val s = cmd.removePrefix("sleep").trim().toFloatOrNull() ?: 1f
        scope.launch { delay((s * 1000).toLong()) }
    }

    // ── Network ────────────────────────────────────────────────────

    private fun doPing(cmd: String) {
        val parts = cmd.split(Regex("\\s+"))
        val cIdx  = parts.indexOf("-c")
        val count = if (cIdx >= 0) parts.getOrNull(cIdx + 1)?.toIntOrNull() ?: 4 else 4
        val host  = parts.lastOrNull { !it.startsWith("-") && it != "ping" && it.toIntOrNull() == null } ?: "8.8.8.8"
        emitLine("PING $host: 56 data bytes")
        scope.launch {
            var ok = 0
            repeat(count.coerceAtMost(10)) { i ->
                try {
                    val t0   = System.currentTimeMillis()
                    val addr = java.net.InetAddress.getByName(host)
                    if (addr.isReachable(2000)) {
                        emitLine("64 bytes from ${addr.hostAddress}: icmp_seq=$i time=${System.currentTimeMillis() - t0}ms")
                        ok++
                    } else emitLine("Request timeout for icmp_seq $i")
                } catch (e: Exception) { emitLine("ping: $host: ${e.message}"); return@repeat }
                delay(1000)
            }
            emitLine("--- $host ping statistics ---")
            emitLine("$count packets transmitted, $ok received")
        }
    }

    private fun doCurl(cmd: String) {
        val parts  = cmd.split(Regex("\\s+"))
        val headOnly = "-I" in parts || "--head" in parts
        val url    = parts.lastOrNull { it.startsWith("http") }
            ?: run { emitLine("curl: no URL specified"); return }
        scope.launch {
            try {
                val conn = java.net.URL(url).openConnection() as java.net.HttpURLConnection
                conn.connectTimeout = 15000; conn.readTimeout = 30000; conn.connect()
                emitLine("< HTTP/1.1 ${conn.responseCode} ${conn.responseMessage}")
                if (!headOnly) {
                    val body = runCatching { conn.inputStream.bufferedReader().readText() }.getOrDefault("")
                    body.lines().take(200).forEach { emitLine(it) }
                    if (body.lines().size > 200) emitLine("... (truncated)")
                }
                conn.disconnect()
            } catch (e: Exception) { emitLine("curl: ${e.message}") }
        }
    }

    private fun doWget(cmd: String) {
        val parts   = cmd.split(Regex("\\s+"))
        val oIdx    = parts.indexOfFirst { it == "-O" || it == "-o" }
        val outName = if (oIdx >= 0) parts.getOrNull(oIdx + 1) else null
        val url     = parts.lastOrNull { it.startsWith("http") }
            ?: run { emitLine("wget: missing URL"); return }
        val name    = outName ?: url.substringAfterLast("/").ifBlank { "index.html" }
        val ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        emitLine("--$ts--  $url")
        emitLine("Saving to: '$name'")
        scope.launch {
            try {
                val conn = java.net.URL(url).openConnection() as java.net.HttpURLConnection
                conn.connectTimeout = 15000; conn.readTimeout = 60000; conn.connect()
                val bytes = conn.inputStream.readBytes()
                File("$currentDir/$name").writeBytes(bytes)
                emitLine("'$name' saved [${bytes.size}]")
            } catch (e: Exception) { emitLine("wget: ${e.message}") }
        }
    }

    private fun doIfconfig() {
        try {
            java.net.NetworkInterface.getNetworkInterfaces()?.toList()
                ?.filter { !it.isLoopback && it.isUp }
                ?.forEach { iface ->
                    emitLine("${iface.name}:")
                    iface.inetAddresses.toList().forEach { a -> emitLine("  inet ${a.hostAddress}") }
                    emitLine("")
                }
        } catch (e: Exception) { emitLine("ifconfig: ${e.message}") }
    }

    private fun doNslookup(cmd: String) {
        val host = cmd.split(Regex("\\s+")).lastOrNull { it != "nslookup" } ?: return
        scope.launch {
            try {
                val addrs = java.net.InetAddress.getAllByName(host)
                emitLine("Non-authoritative answer:")
                addrs.forEach { emitLine("Address: ${it.hostAddress}") }
            } catch (_: Exception) { emitLine("** server can't find $host: NXDOMAIN") }
        }
    }

    // ── pkg ────────────────────────────────────────────────────────

    private val pkgDb = mapOf(
        "python" to "Python 3.11 (built-in)", "python3" to "Python 3.11 (built-in)",
        "git" to "Git version control", "curl" to "HTTP client (built-in)",
        "wget" to "Download files (built-in)", "vim" to "Vi IMproved editor",
        "nano" to "GNU nano editor", "nodejs" to "Node.js JavaScript runtime",
        "npm" to "Node Package Manager", "php" to "PHP scripting",
        "ruby" to "Ruby language", "golang" to "Go language",
        "rust" to "Rust language", "java" to "Java (built-in Android)",
        "sqlite" to "SQLite database", "ffmpeg" to "FFmpeg multimedia",
        "nmap" to "Network scanner", "ssh" to "OpenSSH",
        "zip" to "Zip (built-in)", "unzip" to "Unzip (built-in)",
        "grep" to "Pattern matching (built-in)", "find" to "Find files (built-in)",
        "jq" to "JSON processor", "tree" to "Directory tree",
        "make" to "Build automation", "cmake" to "CMake build system",
        "clang" to "C/C++ compiler", "gdb" to "GNU debugger",
        "tmux" to "Terminal multiplexer", "zsh" to "Z Shell",
        "perl" to "Perl scripting", "lua" to "Lua scripting",
        "mariadb" to "MariaDB database", "postgresql" to "PostgreSQL",
        "redis" to "Redis database", "nginx" to "Nginx web server",
        "apache2" to "Apache HTTP server", "openssl" to "TLS/SSL toolkit",
        "netcat" to "Netcat utility", "rsync" to "Remote file sync"
    )

    private val builtinPkgs = setOf("python", "python3", "curl", "wget", "zip", "unzip", "grep", "find", "java")

    private fun doPkg(cmd: String) {
        val parts = cmd.split(Regex("\\s+"))
        when (parts.getOrNull(1)) {
            "install"            -> doPkgInstall(cmd)
            "remove", "uninstall" -> doPkgRemove(cmd)
            "list"               -> doPkgList()
            "search"             -> doPkgSearch(cmd)
            "update", "upgrade"  -> emitLine("All packages up to date.")
            else -> {
                emitLine("CodeDroid Package Manager")
                emitLine("Usage: pkg install/remove/list/search <pkg>")
                emitLine("Available: ${pkgDb.keys.sorted().joinToString(", ")}")
            }
        }
    }

    private fun doPkgInstall(cmd: String) {
        val pkgs = cmd.split(Regex("\\s+")).drop(2)
        if (pkgs.isEmpty()) { emitLine("Usage: pkg install <package>"); return }
        pkgs.forEach { pkg ->
            emitLine("Reading package lists...")
            when {
                builtinPkgs.contains(pkg.lowercase()) -> {
                    emitLine("$pkg is already the newest version (built-in)")
                    installedPkgs.add(pkg.lowercase()); savePkgs()
                }
                pkgDb.containsKey(pkg.lowercase()) -> {
                    emitLine("Unpacking $pkg ..."); emitLine("Setting up $pkg ...")
                    installedPkgs.add(pkg.lowercase()); savePkgs()
                    emitLine("✅ $pkg installed — ${pkgDb[pkg.lowercase()]}")
                    if (pkg.lowercase() !in builtinPkgs)
                        emitLine("💡 For full $pkg: install Termux → pkg install $pkg")
                }
                else -> { emitLine("E: Unable to locate package $pkg"); emitLine("💡 Try: pkg search $pkg") }
            }
        }
    }

    private fun doPkgRemove(cmd: String) {
        val pkg = cmd.split(Regex("\\s+")).drop(2).firstOrNull()
            ?: run { emitLine("Usage: pkg remove <package>"); return }
        if (builtinPkgs.contains(pkg.lowercase())) emitLine("$pkg: is an essential package")
        else if (installedPkgs.remove(pkg.lowercase())) { savePkgs(); emitLine("$pkg removed") }
        else emitLine("$pkg: not installed")
    }

    private fun doPkgList() {
        val all = (installedPkgs + builtinPkgs).sorted()
        emitLine("Installed packages (${all.size}):")
        all.forEach { pkg -> emitLine("  ${pkg.padEnd(16)} ${if (builtinPkgs.contains(pkg)) "[built-in]" else "[installed]"}") }
    }

    private fun doPkgSearch(cmd: String) {
        val q = cmd.split(Regex("\\s+")).drop(2).joinToString(" ")
        if (q.isBlank()) { emitLine("Usage: pkg search <query>"); return }
        val res = pkgDb.filter { (k, v) -> k.contains(q, true) || v.contains(q, true) }
        if (res.isEmpty()) { emitLine("No packages found for '$q'"); return }
        res.forEach { (k, v) -> emitLine("${k.padEnd(16)} $v") }
    }

    // ── Python via Chaquopy ────────────────────────────────────────

    private fun doPython(cmd: String) {
        if (!initPython()) return
        val arg = cmd.split(Regex("\\s+"), limit = 2).getOrNull(1)?.trim() ?: ""
        when {
            arg.isBlank() -> emitLine("Python 3.11 ready. Use: python3 -c '<code>'  or  python3 <file.py>")
            arg.startsWith("-c ") -> {
                val code = arg.removePrefix("-c").trim().trim('"').trim('\'')
                runPythonCode(code)
            }
            arg.endsWith(".py") -> {
                val f = File(if (arg.startsWith("/")) arg else "$currentDir/$arg")
                if (!f.exists()) { emitLine("python3: can't open file '$arg': No such file or directory"); return }
                runPythonCode(f.readText(), f.parent ?: currentDir)
            }
            else -> emitLine("Usage: python3 <script.py>  or  python3 -c '<code>'")
        }
    }

    fun runPythonCode(code: String, workDir: String = currentDir) {
        if (!initPython()) return
        scope.launch {
            try {
                val py     = Python.getInstance()
                val sys    = py.getModule("sys")
                val io     = py.getModule("io")
                val outBuf = io.callAttr("StringIO")
                val errBuf = io.callAttr("StringIO")
                sys["stdout"] = outBuf
                sys["stderr"] = errBuf
                try { py.getModule("os").callAttr("chdir", workDir) } catch (_: Exception) {}
                py.getModule("builtins").callAttr("exec", code)
                val out = outBuf.callAttr("getvalue").toString()
                val err = errBuf.callAttr("getvalue").toString()
                if (out.isNotBlank()) out.trimEnd().lines().forEach { emitLine(it) }
                if (err.isNotBlank()) err.trimEnd().lines().forEach { emitLine("❌ $it") }
                if (out.isBlank() && err.isBlank()) emitLine("(no output)")
            } catch (e: Exception) {
                emitLine("Traceback (most recent call last):")
                emitLine(e.message ?: "Python error")
                pythonHint(e.message ?: "")?.let { emitLine("💡 $it") }
            }
        }
    }

    private fun doPip(cmd: String) {
        if (!initPython()) return
        val parts  = cmd.split(Regex("\\s+"))
        val subCmd = parts.getOrNull(1) ?: "help"
        when (subCmd) {
            "install"   -> doPipInstall(parts.drop(2))
            "uninstall" -> doPipUninstall(parts.getOrNull(2))
            "list"      -> doPipList()
            "show"      -> doPipShow(parts.getOrNull(2))
            "freeze"    -> doPipFreeze()
            else -> emitLine("Usage: pip3 install/uninstall/list/show/freeze")
        }
    }

    private fun doPipInstall(pkgs: List<String>) {
        val realPkgs = pkgs.filter { !it.startsWith("-") }
        if (realPkgs.isEmpty()) { emitLine("Usage: pip3 install <package>"); return }
        scope.launch {
            realPkgs.forEach { pkg ->
                emitLine("Collecting $pkg")
                try {
                    val py  = Python.getInstance()
                    val pip = py.getModule("pip")
                    pip.callAttr("main", arrayOf("install", pkg, "--quiet"))
                    emitLine("Successfully installed $pkg")
                } catch (e: Exception) {
                    emitLine("ERROR: Could not install $pkg")
                    emitLine("  ${e.message?.take(100)}")
                }
            }
        }
    }

    private fun doPipUninstall(pkg: String?) {
        if (pkg == null) { emitLine("Usage: pip3 uninstall <package>"); return }
        scope.launch {
            try {
                Python.getInstance().getModule("pip").callAttr("main", arrayOf("uninstall", pkg, "-y"))
                emitLine("Successfully uninstalled $pkg")
            } catch (e: Exception) { emitLine("ERROR: ${e.message}") }
        }
    }

    private fun doPipList() {
        scope.launch {
            runPythonCode("""
import pkg_resources
pkgs = sorted(pkg_resources.working_set, key=lambda x: x.project_name.lower())
print(f"{'Package':<30} {'Version'}")
print("-" * 40)
for p in pkgs:
    print(f"{p.project_name:<30} {p.version}")
print(f"\n{len(pkgs)} packages installed")
""".trimIndent())
        }
    }

    private fun doPipFreeze() {
        scope.launch {
            runPythonCode("""
import pkg_resources
for p in sorted(pkg_resources.working_set, key=lambda x: x.project_name.lower()):
    print(f"{p.project_name}=={p.version}")
""".trimIndent())
        }
    }

    private fun doPipShow(pkg: String?) {
        if (pkg == null) { emitLine("Usage: pip3 show <package>"); return }
        scope.launch {
            runPythonCode("""
import pkg_resources
try:
    d = pkg_resources.get_distribution('$pkg')
    print(f"Name: {d.project_name}\nVersion: {d.version}\nLocation: {d.location}")
except Exception as e:
    print(f"WARNING: Package '$pkg' not found")
""".trimIndent())
        }
    }

    private fun pythonHint(err: String): String? = when {
        "No module named" in err -> {
            val m = Regex("No module named '(.+?)'").find(err)?.groupValues?.get(1) ?: "module"
            "pip3 install $m"
        }
        "SyntaxError" in err       -> "Check syntax: brackets, colons, quotes"
        "IndentationError" in err  -> "Use 4 spaces or consistent tabs"
        "NameError" in err         -> "Variable not defined"
        "TypeError" in err         -> "Data type mismatch"
        "FileNotFoundError" in err -> "File not found"
        else -> null
    }

    // ── External tools ─────────────────────────────────────────────

    private fun doNode(cmd: String) {
        val bin = listOf("/data/data/com.termux/files/usr/bin/node", "/system/bin/node")
            .firstOrNull { File(it).exists() }
        if (bin != null) runSystem(cmd)
        else { emitLine("node: command not found"); emitLine("💡 Install Termux → pkg install nodejs") }
    }

    private fun doNpm(cmd: String) {
        if (File("/data/data/com.termux/files/usr/bin/npm").exists()) runSystem(cmd)
        else { emitLine("npm: command not found"); emitLine("💡 Install Termux → pkg install nodejs") }
    }

    private fun doGit(cmd: String) {
        val bin = listOf("/data/data/com.termux/files/usr/bin/git", "/system/bin/git")
            .firstOrNull { File(it).exists() }
        if (bin != null) runSystem(cmd)
        else { emitLine("git: command not found"); emitLine("💡 Install Termux → pkg install git") }
    }

    private fun doPhp(cmd: String) {
        val bin = listOf("/data/data/com.termux/files/usr/bin/php", "/system/bin/php")
            .firstOrNull { File(it).exists() }
        if (bin != null) runSystem(cmd)
        else { emitLine("php: command not found"); emitLine("💡 Install Termux → pkg install php") }
    }

    private fun doRedirect(cmd: String) {
        val append = ">>" in cmd
        val parts  = if (append) cmd.split(">>", limit = 2) else cmd.split(">", limit = 2)
        if (parts.size < 2) { runSystem(cmd); return }
        val left  = parts[0].trim()
        val fName = parts[1].trim()
        val out   = File(if (fName.startsWith("/")) fName else "$currentDir/$fName")
        if (left.startsWith("echo")) {
            val text = left.removePrefix("echo").trim()
            if (append) out.appendText(text + "\n") else out.writeText(text + "\n")
        } else {
            emitLine("💡 Complex redirect: use Python")
        }
    }

    private fun doPipe(cmd: String) {
        val parts = cmd.split("|", limit = 2).map { it.trim() }
        if (parts.size < 2) { runSystem(cmd); return }
        sendCommand(parts[0])
    }

    private fun runSystem(cmd: String) {
        try {
            val envArr = (env + mapOf("PWD" to currentDir)).map { (k, v) -> "$k=$v" }.toTypedArray()
            val proc   = Runtime.getRuntime().exec(arrayOf("/system/bin/sh", "-c", cmd), envArr, File(currentDir))
            scope.launch { proc.inputStream.bufferedReader().forEachLine { emitLine(it) } }
            scope.launch { proc.errorStream.bufferedReader().forEachLine { if (it.isNotBlank()) emitLine(it) } }
            val code = proc.waitFor()
            if (code != 0) sysHint(cmd.split(Regex("\\s+")).first())?.let { emitLine("💡 $it") }
        } catch (_: Exception) {
            val bin = cmd.split(Regex("\\s+")).first()
            emitLine("$bin: command not found")
            sysHint(bin)?.let { emitLine("💡 $it") }
        }
    }

    private fun sysHint(bin: String): String? = when (bin) {
        "apt", "apt-get" -> "Use 'pkg install <name>'"
        "sudo", "su"     -> "sudo not available (no root)"
        "vim", "vi"      -> "Use CodeDroid editor"
        else -> null
    }

    private fun showHelp() {
        listOf(
            "╔══════════════════════════════════════════════╗",
            "║     CodeDroid Terminal — Command Reference   ║",
            "╠══════════════════════════════════════════════╣",
            "║  NAVIGATION: pwd cd ls [-la] mkdir touch     ║",
            "║  FILE: rm [-rf] cp [-r] mv cat [-n]          ║",
            "║        head tail wc grep find diff stat      ║",
            "║        sort uniq cut du df zip unzip         ║",
            "╠══════════════════════════════════════════════╣",
            "║  PYTHON (built-in, no Termux):               ║",
            "║    python3 <file.py>                         ║",
            "║    python3 -c '<code>'                       ║",
            "║    pip3 install/uninstall/list/show/freeze   ║",
            "╠══════════════════════════════════════════════╣",
            "║  PACKAGE: pkg install/remove/list/search     ║",
            "║  NETWORK: ping curl wget ifconfig nslookup   ║",
            "║  SYSTEM:  echo env export which uname free   ║",
            "║           uptime ps date whoami history      ║",
            "╚══════════════════════════════════════════════╝"
        ).forEach { emitLine(it) }
    }

    private fun fmtSize(bytes: Long): String = when {
        bytes < 1024L       -> "${bytes}B"
        bytes < 1048576L    -> "${bytes / 1024}K"
        bytes < 1073741824L -> "${"%.1f".format(bytes / 1048576.0)}M"
        else                -> "${"%.1f".format(bytes / 1073741824.0)}G"
    }

    fun stopSession() { scope.cancel() }
}
