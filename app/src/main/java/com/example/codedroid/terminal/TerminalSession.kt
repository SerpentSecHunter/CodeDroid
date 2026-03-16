package com.example.codedroid.terminal

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

@Suppress("SpellCheckingInspection")
class TerminalSession {
    private val _output = MutableSharedFlow<String>(replay = 200)
    val output: SharedFlow<String> = _output
    private var process: Process? = null

    suspend fun start() {
        try {
            _output.emit("CodeDroid Terminal v2.0")
            _output.emit("Type 'help' for a list of available commands")
            _output.emit("---")
        } catch (_: Exception) {}
    }

    suspend fun sendCommand(command: String) {
        _output.emit("$ $command")
        val result = executeCommand(command.trim())
        if (result.isNotEmpty()) _output.emit(result)
    }

    private fun executeCommand(cmd: String): String {
        if (cmd.isBlank()) return ""
        val parts = cmd.split(" ")
        return when (parts[0]) {
            "help"  -> "Commands: help, echo, pwd, ls, date, clear, whoami, uname"
            "echo"  -> parts.drop(1).joinToString(" ")
            "pwd"   -> "/storage/emulated/0"
            "ls"    -> try {
                java.io.File("/storage/emulated/0").listFiles()
                    ?.take(20)?.joinToString("\n") { it.name } ?: "Unable to access directory"
            } catch (_: Exception) { "Permission denied" }
            "date"  -> java.util.Date().toString()
            "whoami"-> "android"
            "uname" -> "Linux Android"
            "clear" -> "\u001b[2J"
            else    -> try {
                val pb = ProcessBuilder(parts).redirectErrorStream(true)
                val p  = pb.start()
                val out = p.inputStream.bufferedReader().readText().trim()
                p.waitFor()
                out.ifEmpty { "(no output)" }
            } catch (_: Exception) { "Command not found: ${parts[0]}" }
        }
    }

    fun stop() {
        process?.destroy()
        process = null
    }
}