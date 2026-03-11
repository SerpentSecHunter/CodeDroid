package com.example.codedroid.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codedroid.terminal.TerminalManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class TerminalLine(
    val text  : String,
    val type  : LineType = LineType.OUTPUT
)

enum class LineType { PROMPT, OUTPUT, ERROR, INFO, SUCCESS, WARNING }

@Composable
fun TerminalScreen(terminalManager: TerminalManager) {
    val scope     = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var input     by remember { mutableStateOf("") }
    val lines     = remember { mutableStateListOf<TerminalLine>() }
    var history   = remember { mutableStateListOf<String>() }
    var histIdx   by remember { mutableStateOf(-1) }
    var showShortcuts by remember { mutableStateOf(false) }

    // Common shortcuts untuk termux-like experience
    val shortcuts = listOf("ls","cd /","pwd","clear","help","python3","pip install","cat ","echo ","grep ","chmod ","./")

    LaunchedEffect(Unit) {
        terminalManager.startSession()
        terminalManager.output.collectLatest { raw ->
            val line = parseTerminalLine(raw)
            lines.add(line)
            if (lines.size > 1000) lines.removeAt(0)
            if (lines.isNotEmpty()) {
                scope.launch { listState.animateScrollToItem(lines.size - 1) }
            }
        }
    }

    Column(Modifier.fillMaxSize().background(Color(0xFF0C0C0C))) {
        // Terminal header bar
        Row(
            modifier = Modifier.fillMaxWidth()
                .background(Color(0xFF1A1A2E))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Traffic lights
            Box(Modifier.size(10.dp).background(Color(0xFFFF5F57), RoundedCornerShape(50)))
            Spacer(Modifier.width(6.dp))
            Box(Modifier.size(10.dp).background(Color(0xFFFFBD2E), RoundedCornerShape(50)))
            Spacer(Modifier.width(6.dp))
            Box(Modifier.size(10.dp).background(Color(0xFF28CA41), RoundedCornerShape(50)))
            Spacer(Modifier.width(12.dp))

            Text("bash — CodeDroid Terminal",
                fontSize = 12.sp, fontFamily = FontFamily.Monospace,
                color = Color(0xFFCDD6F4), modifier = Modifier.weight(1f))

            IconButton(onClick = { showShortcuts = !showShortcuts }, Modifier.size(32.dp)) {
                Icon(Icons.Rounded.SpaceBar, null, modifier = Modifier.size(18.dp), tint = Color(0xFF6C7086))
            }
            IconButton(onClick = { lines.clear() }, Modifier.size(32.dp)) {
                Icon(Icons.Rounded.Clear, null, modifier = Modifier.size(18.dp), tint = Color(0xFF6C7086))
            }
        }

        // Shortcut bar (termux-style)
        if (showShortcuts) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .background(Color(0xFF11111B))
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                shortcuts.forEach { cmd ->
                    Surface(
                        modifier = Modifier.pointerInput(Unit) {
                            detectTapGestures { input += cmd }
                        },
                        color    = Color(0xFF1E1E2E),
                        shape    = RoundedCornerShape(6.dp)
                    ) {
                        Text(cmd, fontSize = 11.sp, fontFamily = FontFamily.Monospace,
                            color = Color(0xFFCBA6F7),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                    }
                }
            }
        }

        // Output area
        LazyColumn(
            state    = listState,
            modifier = Modifier.weight(1f).fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            items(lines) { line ->
                TerminalLineView(line)
            }
        }

        // Input area
        Row(
            modifier = Modifier.fillMaxWidth()
                .background(Color(0xFF181825))
                .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Prompt symbol
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(color = Color(0xFF89B4FA))) { append("user") }
                    withStyle(SpanStyle(color = Color(0xFF6C7086))) { append("@") }
                    withStyle(SpanStyle(color = Color(0xFFA6E3A1))) { append("codedroid") }
                    withStyle(SpanStyle(color = Color(0xFFCBA6F7))) { append(" $ ") }
                },
                fontSize   = 12.sp,
                fontFamily = FontFamily.Monospace
            )

            BasicTerminalInput(
                value         = input,
                onValueChange = { input = it },
                onSubmit      = {
                    if (input.isNotBlank()) {
                        history.add(0, input)
                        histIdx = -1
                        val cmd = input; input = ""
                        terminalManager.sendCommand(cmd)
                    }
                },
                modifier = Modifier.weight(1f)
            )

            // History nav
            Column {
                IconButton(onClick = {
                    if (histIdx < history.size - 1) { histIdx++; input = history[histIdx] }
                }, Modifier.size(28.dp)) {
                    Icon(Icons.Rounded.ArrowUpward, null,
                        tint = Color(0xFF6C7086), modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = {
                    if (histIdx > 0) { histIdx--; input = history[histIdx] }
                    else { histIdx = -1; input = "" }
                }, Modifier.size(28.dp)) {
                    Icon(Icons.Rounded.ArrowDownward, null,
                        tint = Color(0xFF6C7086), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun BasicTerminalInput(
    value: String,
    onValueChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.text.BasicTextField(
        value         = value,
        onValueChange = onValueChange,
        textStyle     = androidx.compose.ui.text.TextStyle(
            fontFamily = FontFamily.Monospace,
            fontSize   = 13.sp,
            color      = Color(0xFFCDD6F4)
        ),
        singleLine    = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onSubmit() }),
        cursorBrush   = androidx.compose.ui.graphics.SolidColor(Color(0xFFCBA6F7)),
        modifier      = modifier.padding(vertical = 6.dp)
    )
}

@Composable
private fun TerminalLineView(line: TerminalLine) {
    val color = when (line.type) {
        LineType.PROMPT  -> Color(0xFFCBA6F7)
        LineType.OUTPUT  -> Color(0xFFCDD6F4)
        LineType.ERROR   -> Color(0xFFF38BA8)
        LineType.INFO    -> Color(0xFF89DCEB)
        LineType.SUCCESS -> Color(0xFFA6E3A1)
        LineType.WARNING -> Color(0xFFF9E2AF)
    }
    // Error hints
    if (line.type == LineType.ERROR) {
        Column(Modifier.padding(vertical = 2.dp)) {
            Text(line.text, color = color, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
            val hint = getErrorHint(line.text)
            if (hint != null) {
                Surface(
                    color    = Color(0xFF313244),
                    shape    = RoundedCornerShape(6.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Row(Modifier.padding(8.dp), Arrangement.spacedBy(6.dp), Alignment.Top) {
                        Icon(Icons.Rounded.Lightbulb, null,
                            tint = Color(0xFFF9E2AF), modifier = Modifier.size(14.dp))
                        Text(hint, color = Color(0xFFF9E2AF), fontSize = 11.sp)
                    }
                }
            }
        }
    } else {
        Text(line.text, color = color, fontSize = 12.sp,
            fontFamily = FontFamily.Monospace, lineHeight = 18.sp,
            modifier   = Modifier.padding(vertical = 1.dp))
    }
}

private fun parseTerminalLine(raw: String): TerminalLine {
    return when {
        raw.startsWith("$ ")   -> TerminalLine(raw, LineType.PROMPT)
        raw.startsWith("✅")   -> TerminalLine(raw, LineType.SUCCESS)
        raw.startsWith("❌") ||
        raw.lowercase().contains("error") ||
        raw.lowercase().contains("failed") ||
        raw.lowercase().contains("exception") -> TerminalLine(raw, LineType.ERROR)
        raw.startsWith("⚠️") ||
        raw.lowercase().contains("warning")   -> TerminalLine(raw, LineType.WARNING)
        raw.startsWith("ℹ️") ||
        raw.startsWith("---")  -> TerminalLine(raw, LineType.INFO)
        else                   -> TerminalLine(raw, LineType.OUTPUT)
    }
}

private fun getErrorHint(error: String): String? {
    val e = error.lowercase()
    return when {
        e.contains("command not found") || e.contains("not recognized") ->
            "💡 Perintah tidak ditemukan. Coba 'help' untuk melihat daftar perintah yang tersedia."
        e.contains("permission denied") ->
            "💡 Tidak ada izin. Coba tambahkan 'chmod +x namafile' atau jalankan dengan izin yang tepat."
        e.contains("no such file") || e.contains("not found") ->
            "💡 File tidak ditemukan. Pastikan path benar, coba 'ls' untuk melihat isi folder."
        e.contains("modulenotfounderror") || e.contains("no module named") ->
            "💡 Library Python tidak ada. Jalankan: pip install <namalibrary>"
        e.contains("syntaxerror") ->
            "💡 Syntax Python salah. Periksa tanda kurung, titik dua (:), atau indentasi."
        e.contains("indentationerror") ->
            "💡 Indentasi Python salah. Gunakan 4 spasi atau Tab secara konsisten."
        e.contains("nameerror") ->
            "💡 Variabel belum didefinisikan. Pastikan nama variabel sudah benar dan dideklarasikan."
        e.contains("typeerror") ->
            "💡 Tipe data salah. Periksa apakah tipe data yang digunakan sudah sesuai."
        e.contains("connection refused") ->
            "💡 Koneksi ditolak. Pastikan server/host target aktif dan port-nya benar."
        e.contains("java.lang") ->
            "💡 Error Java/Kotlin. Periksa NullPointerException atau tipe data yang tidak sesuai."
        e.contains("oom") || e.contains("out of memory") ->
            "💡 Memori habis. Tutup aplikasi lain atau kurangi ukuran data yang diproses."
        else -> null
    }
}