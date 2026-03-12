package com.example.codedroid.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codedroid.terminal.TerminalManager
import kotlinx.coroutines.launch

// Catppuccin Mocha colors
private val BG       = Color(0xFF1E1E2E)
private val SURFACE  = Color(0xFF181825)
private val OVERLAY  = Color(0xFF313244)
private val TEXT     = Color(0xFFCDD6F4)
private val SUBTEXT  = Color(0xFF6C7086)
private val BLUE     = Color(0xFF89B4FA)
private val GREEN    = Color(0xFFA6E3A1)
private val RED      = Color(0xFFF38BA8)
private val YELLOW   = Color(0xFFF9E2AF)
private val MAUVE    = Color(0xFFCBA6F7)
private val TEAL     = Color(0xFF94E2D5)
private val FLAMINGO = Color(0xFFF2CDCD)

data class TermLine(val text: String, val color: Color)

@Composable
fun TerminalScreen(terminalManager: TerminalManager) {
    val scope        = rememberCoroutineScope()
    val listState    = rememberLazyListState()
    val focusReq     = remember { FocusRequester() }
    var input        by remember { mutableStateOf("") }
    val lines        = remember { mutableStateListOf<TermLine>() }
    val cmdHistory   = remember { mutableStateListOf<String>() }
    var histIdx      by remember { mutableStateOf(-1) }
    var showShortcuts by remember { mutableStateOf(true) }

    val shortcuts = listOf(
        "ls" to "ls", "cd ~" to "cd ~", "pwd" to "pwd",
        "clear" to "clear", "help" to "help",
        "python3" to "python3 ", "pip" to "pip install ",
        "cat" to "cat ", "mkdir" to "mkdir ",
        "rm" to "rm ", "cp" to "cp ", "mv" to "mv ",
        "grep" to "grep ", "echo" to "echo ",
        "history" to "history"
    )

    LaunchedEffect(Unit) {
        terminalManager.startSession()
        terminalManager.output.collect { raw ->
            when {
                raw == "\u0000CLEAR" -> lines.clear()
                raw.startsWith("$ ") -> lines.add(TermLine(raw, MAUVE))
                raw.startsWith("✅") -> lines.add(TermLine(raw, GREEN))
                raw.startsWith("❌") || raw.lowercase().let {
                    it.contains("error") || it.contains("failed") || it.contains("exception")
                } -> lines.add(TermLine(raw, RED))
                raw.startsWith("⚠️") || raw.lowercase().contains("warning")
                    -> lines.add(TermLine(raw, YELLOW))
                raw.startsWith("⬇️") -> lines.add(TermLine(raw, TEAL))
                raw.startsWith("▶")  -> lines.add(TermLine(raw, BLUE))
                raw.startsWith("💡") -> lines.add(TermLine(raw, FLAMINGO))
                raw.startsWith("ℹ️") || raw.startsWith("---") || raw.startsWith("╔") ||
                raw.startsWith("║") || raw.startsWith("╚")
                    -> lines.add(TermLine(raw, SUBTEXT))
                else -> lines.add(TermLine(raw, TEXT))
            }
            if (lines.size > 1000) lines.removeAt(0)
            scope.launch {
                if (lines.isNotEmpty()) listState.animateScrollToItem(lines.size - 1)
            }
        }
    }

    Column(Modifier.fillMaxSize().background(BG)) {

        // ── Title bar ─────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth()
                .background(SURFACE)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Traffic lights
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(Modifier.size(12.dp).clip(CircleShape).background(RED))
                Box(Modifier.size(12.dp).clip(CircleShape).background(YELLOW))
                Box(Modifier.size(12.dp).clip(CircleShape).background(GREEN))
            }
            Spacer(Modifier.width(12.dp))
            Text("bash — CodeDroid Terminal",
                fontSize   = 13.sp,
                fontFamily = FontFamily.Monospace,
                color      = SUBTEXT,
                modifier   = Modifier.weight(1f))
            // Toggle shortcut bar
            IconButton(onClick = { showShortcuts = !showShortcuts },
                modifier = Modifier.size(30.dp)) {
                Icon(Icons.Rounded.SpaceBar, null,
                    tint = if (showShortcuts) GREEN else SUBTEXT,
                    modifier = Modifier.size(16.dp))
            }
            // Clear
            IconButton(onClick = { lines.clear() },
                modifier = Modifier.size(30.dp)) {
                Icon(Icons.Rounded.Clear, null, tint = SUBTEXT,
                    modifier = Modifier.size(16.dp))
            }
        }

        // ── Shortcut bar ──────────────────────────────────────────
        if (showShortcuts) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SURFACE.copy(0.7f))
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                shortcuts.forEach { (label, insert) ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(OVERLAY)
                            .pointerInput(Unit) {
                                detectTapGestures { input += insert }
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(label, fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace, color = MAUVE)
                    }
                }
            }
        }

        HorizontalDivider(color = OVERLAY, thickness = 0.5.dp)

        // ── Output ────────────────────────────────────────────────
        LazyColumn(
            state    = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            items(lines) { line ->
                // Error: tampilkan hint
                if (line.color == RED) {
                    Column(Modifier.padding(vertical = 2.dp)) {
                        Text(line.text, color = RED, fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace)
                        val hint = getErrorHint(line.text)
                        if (hint != null) {
                            Row(
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(OVERLAY)
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("💡", fontSize = 12.sp)
                                Text(hint, color = FLAMINGO, fontSize = 11.sp,
                                    lineHeight = 16.sp)
                            }
                        }
                    }
                } else {
                    Text(line.text, color = line.color, fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace, lineHeight = 17.sp)
                }
            }
        }

        HorizontalDivider(color = OVERLAY, thickness = 0.5.dp)

        // ── Input area ────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SURFACE)
                .padding(horizontal = 10.dp, vertical = 8.dp)
                .imePadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Prompt
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(color = BLUE, fontWeight = FontWeight.Bold))  { append("user") }
                    withStyle(SpanStyle(color = SUBTEXT))  { append("@") }
                    withStyle(SpanStyle(color = GREEN, fontWeight = FontWeight.Bold))  { append("codedroid") }
                    withStyle(SpanStyle(color = MAUVE, fontWeight = FontWeight.Bold))  { append(" \$ ") }
                },
                fontSize   = 12.sp,
                fontFamily = FontFamily.Monospace
            )

            // Input field
            BasicTextField(
                value         = input,
                onValueChange = { input = it; histIdx = -1 },
                textStyle     = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize   = 13.sp,
                    color      = TEXT
                ),
                singleLine    = true,
                cursorBrush   = SolidColor(MAUVE),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    if (input.isNotBlank()) {
                        cmdHistory.add(0, input)
                        histIdx = -1
                        val cmd = input; input = ""
                        terminalManager.sendCommand(cmd)
                    }
                }),
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusReq)
                    .padding(vertical = 4.dp)
            )

            // History navigation
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = {
                    if (cmdHistory.isNotEmpty() && histIdx < cmdHistory.size - 1) {
                        histIdx++
                        input = cmdHistory[histIdx]
                    }
                }, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Rounded.ArrowUpward, null,
                        tint = SUBTEXT, modifier = Modifier.size(14.dp))
                }
                IconButton(onClick = {
                    if (histIdx > 0) { histIdx--; input = cmdHistory[histIdx] }
                    else { histIdx = -1; input = "" }
                }, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Rounded.ArrowDownward, null,
                        tint = SUBTEXT, modifier = Modifier.size(14.dp))
                }
            }

            // Send button
            IconButton(
                onClick = {
                    if (input.isNotBlank()) {
                        cmdHistory.add(0, input)
                        histIdx = -1
                        val cmd = input; input = ""
                        terminalManager.sendCommand(cmd)
                    }
                },
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(GREEN.copy(0.15f))
            ) {
                Icon(Icons.Rounded.Send, null, tint = GREEN,
                    modifier = Modifier.size(18.dp))
            }
        }
    }

    LaunchedEffect(Unit) {
        runCatching { focusReq.requestFocus() }
    }
}

private fun getErrorHint(error: String): String? {
    val e = error.lowercase()
    return when {
        e.contains("command not found") || e.contains("not recognized") ->
            "Perintah tidak dikenal. Ketik 'help' untuk daftar perintah tersedia."
        e.contains("permission denied") ->
            "Tidak ada izin akses. Coba 'chmod +x namafile' atau periksa izin file."
        e.contains("no such file") || e.contains("not found") ->
            "File/folder tidak ditemukan. Cek path dengan 'ls' atau 'pwd'."
        e.contains("modulenotfounderror") || e.contains("no module named") ->
            "Library Python tidak ada. Jalankan: pip install <namalibrary>"
        e.contains("syntaxerror") ->
            "Syntax Python salah. Periksa tanda kurung, titik dua (:), atau indentasi."
        e.contains("indentationerror") ->
            "Indentasi Python salah. Gunakan 4 spasi atau Tab secara konsisten."
        e.contains("nameerror") ->
            "Variabel belum didefinisikan. Periksa nama dan deklarasi variabel."
        e.contains("typeerror") ->
            "Tipe data tidak sesuai. Cek operasi yang dilakukan pada variabel."
        e.contains("connection refused") ->
            "Koneksi ditolak. Pastikan server aktif dan port benar."
        e.contains("out of memory") ->
            "Memori habis. Tutup aplikasi lain atau kurangi ukuran data."
        else -> null
    }
}